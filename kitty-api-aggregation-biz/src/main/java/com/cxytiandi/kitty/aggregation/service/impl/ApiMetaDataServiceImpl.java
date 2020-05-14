package com.cxytiandi.kitty.aggregation.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.cxytiandi.kitty.aggregation.dao.ApiMetaDataDao;
import com.cxytiandi.kitty.aggregation.dataobject.ApiMetaDataDO;
import com.cxytiandi.kitty.aggregation.request.HttpAggregationRequest;
import com.cxytiandi.kitty.aggregation.request.HttpRequest;
import com.cxytiandi.kitty.aggregation.service.ApiMetaDataService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * API 元数据业务接口
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-12 22:52
 */
@Service
public class ApiMetaDataServiceImpl implements ApiMetaDataService {

    private static Map<String, ApiMetaDataDO> apiMetaDataDOCacheMap = new ConcurrentHashMap<>();

    @Autowired
    private ApiMetaDataDao apiMetaDataDao;

    @PostConstruct
    private void init() {
        List<ApiMetaDataDO> apiMetaDatas = apiMetaDataDao.list();
        apiMetaDataDOCacheMap = apiMetaDatas.stream().collect(Collectors.toMap(ApiMetaDataDO::getApiName, Function.identity()));
    }

    @Override
    public HttpAggregationRequest getHttpAggregationRequest(String api) {
        ApiMetaDataDO apiMetaDataDO = apiMetaDataDOCacheMap.get(api);
        if (apiMetaDataDO == null) {
            throw new RuntimeException("API 不存在");
        }

        String metadata = apiMetaDataDO.getMetadata();
        JSONObject metadataJson = JSONObject.parseObject(metadata);
        JSONArray requests = metadataJson.getJSONArray("request");
        List<HttpRequest> httpRequests = requests.stream().map(r -> {
            JSONObject req = (JSONObject) r;
            return req.toJavaObject(HttpRequest.class);
        }).collect(Collectors.toList());

        HttpAggregationRequest httpAggregationRequest = new HttpAggregationRequest();
        httpAggregationRequest.setHttpRequests(httpRequests);
        httpAggregationRequest.setResponseMetadata(metadataJson.getString("response"));
        return httpAggregationRequest;
    }

}