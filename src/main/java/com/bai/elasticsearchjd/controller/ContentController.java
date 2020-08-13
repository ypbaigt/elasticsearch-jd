package com.bai.elasticsearchjd.controller;

import com.bai.elasticsearchjd.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ContentController {

    @Autowired
    private ContentService contentService;

    @GetMapping("/parse/{keywords}")
    public Boolean parse(@PathVariable("keywords") String keywords) throws Exception {
        return contentService.parseContent(keywords);
    }

    @GetMapping("/search/{keywords}/{pageNo}/{pageSize}")
    public List<Map> search(@PathVariable("keywords") String keywords,
                            @PathVariable("pageNo") Integer pageNo,
                            @PathVariable("pageSize") Integer pageSize) throws Exception {
        return contentService.searchPageHighLight(keywords, pageNo, pageSize);
    }
}
