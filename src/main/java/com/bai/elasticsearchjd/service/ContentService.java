package com.bai.elasticsearchjd.service;

import com.alibaba.fastjson.JSON;
import com.bai.elasticsearchjd.dto.ContentDTO;
import com.bai.elasticsearchjd.utils.HtmlParseUtil;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private HtmlParseUtil htmlParseUtil;

    /**
     * 解析数据并放入ES
     * @param keywords
     * @return
     * @throws Exception
     */
    public Boolean parseContent(String keywords) throws Exception {
        List<ContentDTO> list = htmlParseUtil.parseJD(keywords);

        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout("2m");

        for (int i = 0; i < list.size(); i++) {
            bulkRequest.add(new IndexRequest("jd_goods").source(JSON.toJSONString(list.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    /**
     * 搜索ES库
     */
    public List<Map> searchPage(String keywords, int pageNo, int pageSize) throws IOException {
        pageNo = pageNo < 1 ? 1 : pageNo;

        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(termQueryBuilder);

        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Map> resultList = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            resultList.add(documentFields.getSourceAsMap());
        }
        return resultList;
    }


    /**
     * 搜索ES库 并高亮显示
     */
    public List<Map> searchPageHighLight(String keywords, int pageNo, int pageSize) throws IOException {
        pageNo = pageNo < 1 ? 1 : pageNo;

        SearchRequest request = new SearchRequest("jd_goods");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();

        searchSourceBuilder.from(pageNo);
        searchSourceBuilder.size(pageSize);

        //精准匹配
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keywords);
        searchSourceBuilder.query(termQueryBuilder);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        //是否需要多个高亮显示，false不需要
        //highlightBuilder.requireFieldMatch(false);
        highlightBuilder.preTags("<span style='color:red'>");
        highlightBuilder.postTags("</span>");
        searchSourceBuilder.highlighter(highlightBuilder);

        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS));

        //执行搜索
        request.source(searchSourceBuilder);
        SearchResponse searchResponse = restHighLevelClient.search(request, RequestOptions.DEFAULT);

        List<Map> resultList = new ArrayList<>();
        for (SearchHit documentFields : searchResponse.getHits().getHits()) {
            Map<String, Object> sourceAsMap = documentFields.getSourceAsMap();
            HighlightField title = documentFields.getHighlightFields().get("title");
            if (title != null) {
                String highTitle = String.join("", Arrays.stream(title.getFragments()).map(text -> text.toString()).collect(Collectors.toList()));
                sourceAsMap.put("title", highTitle);
            }
            resultList.add(sourceAsMap);
        }
        return resultList;
    }


}
