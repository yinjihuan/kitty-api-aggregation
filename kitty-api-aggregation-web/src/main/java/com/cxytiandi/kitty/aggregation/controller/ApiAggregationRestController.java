package com.cxytiandi.kitty.aggregation.controller;

import com.cxytiandi.kitty.aggregation.service.HttpApiAggregator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * API聚合入口
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-14 22:58
 */
@RestController
public class ApiAggregationRestController {

    @Autowired
    private HttpApiAggregator httpApiAggregator;

    @Autowired
    private HttpServletRequest request;

    @GetMapping("/api/aggregation")
    public Object aggregation(String apiName) {
        if (StringUtils.isEmpty(apiName)) {
            return "apiName not null";
        }
        return httpApiAggregator.apiAggregator(apiName, request);
    }

}