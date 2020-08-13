package com.bai.elasticsearchjd.utils;

import com.bai.elasticsearchjd.dto.ContentDTO;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Component
public class HtmlParseUtil {

    public static void main(String[] args) throws Exception {
        new HtmlParseUtil().parseJD("心理学").forEach(System.out::println);
    }

    public List<ContentDTO> parseJD(String keywords) throws Exception {

        List<ContentDTO> goodsList = new ArrayList<>();

        String url = "https://search.jd.com/Search?keyword=" + keywords;
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        System.out.println(element.html());
        Elements li = element.getElementsByTag("li");
        for (Element element1 : li) {
            String image = element1.getElementsByTag("img").get(0).attr("src");
            String price = element1.getElementsByClass("p-price").eq(0).text();
            String title = element1.getElementsByClass("p-name").eq(0).text();

            goodsList.add(new ContentDTO(title, image, price));
        }
        return goodsList;
    }

}
