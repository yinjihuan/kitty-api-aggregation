package com.cxytiandi.kitty.aggregation.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import com.cxytiandi.kitty.aggregation.constant.AggregationConstant;
import com.cxytiandi.kitty.aggregation.enums.FieldTypeEnum;
import com.cxytiandi.kitty.aggregation.request.HttpAggregationRequest;
import com.cxytiandi.kitty.aggregation.request.HttpRequest;
import com.cxytiandi.kitty.aggregation.worker.HttpWorker;
import com.jd.platform.async.executor.Async;
import com.jd.platform.async.worker.WorkResult;
import com.jd.platform.async.wrapper.WorkerWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-12 22:41
 */
@Slf4j
@Component
public class HttpApiAggregator {

    @Autowired
    private ApiMetaDataService apiMetaDataService;

    public Object apiAggregator(String apiName, HttpServletRequest request) {
        // 获取API 元数据
        HttpAggregationRequest httpAggregationRequest = apiMetaDataService.getHttpAggregationRequest(apiName);
        List<HttpRequest> httpRequests = httpAggregationRequest.getHttpRequests();

        //  构建API请求参数
        buildApiRequest(request, httpRequests);

        // API对应的执行结果映射
        Map<String, WorkResult<JSONObject>> apiWorkResultMapping = new HashMap<>();

        // API对应的WorkerWrapper映射
        Map<String, WorkerWrapper> apiWorkerWrapperMapping = new HashMap<>();

        // WorkerWrapper对应的API映射
        Map<WorkerWrapper, String> workerWrapperApiMapping = new HashMap<>();

        // Ref为空的，优先执行
        List<HttpRequest> highLevelRequests = httpRequests.stream().filter(hr -> StringUtils.isEmpty(hr.getRef())).collect(Collectors.toList());

        // 构建WorkerWrapper
        List<WorkerWrapper<HttpRequest, JSONObject>> workerWrappers = buildWorkerWrapper(highLevelRequests, httpRequests, apiWorkerWrapperMapping);

        // 设置Worker参数，有的Worker参数依赖于上一个Worker的结果
        setApiWorkerWrapperParam(httpRequests, apiWorkerWrapperMapping);

        try {
            Async.beginWork(httpAggregationRequest.getRequestTimeout(), workerWrappers.toArray(new WorkerWrapper[workerWrappers.size()]));
            workerWrappers.stream().forEach(wrapper -> {
                WorkResult<JSONObject> workResult = wrapper.getWorkResult();
                String api = workerWrapperApiMapping.get(wrapper);
                apiWorkResultMapping.put(api, workResult);
            });

            String responseMetadata = "{\"$code\":{\"type\":\"int\",\"path\":\"getArticles#code\"},\"$message\":{\"type\":\"String\",\"path\":\"getArticles#message\"},\"$data\":{\"$articleResp\":{\"type\":\"Entity\",\"path\":\"getArticles#data\",\"fields\":[\"start\",\"limit\",\"list\"],\"$articleResp3\":{\"type\":\"Entity\",\"path\":\"getArticles#data\",\"fields\":[\"start\"]},\"$myName\":{\"type\":\"int\",\"path\":\"getArticles#code\"}},\"$articleResp2\":{\"type\":\"Entity\",\"path\":\"getArticles#data\",\"fields\":[\"start\"]}}}";
            return formatResult(responseMetadata, apiWorkResultMapping);

        } catch (Exception e) {
            log.error("API聚合异常", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 构建API请求参数
     * @param request
     * @param httpRequests
     */
    private void buildApiRequest(HttpServletRequest request, List<HttpRequest> httpRequests) {
        httpRequests.forEach( req -> {
            Map paramsMap = new HashMap<>();
            String params = req.getParams();
            if (StringUtils.hasText(params)) {
                paramsMap = JSONObject.parseObject(params, Map.class);
            }
            Set<String> paramKeys = paramsMap.keySet();
            for(String key : paramKeys) {
                Object value = paramsMap.get(key);
                if (value.toString().startsWith(AggregationConstant.REQUEST)) {
                    String param = value.toString().split("\\.")[1];
                    String parameter = request.getParameter(param);
                    paramsMap.put(key, parameter);
                }
            }
            req.setParamsValueMap(paramsMap);
        });
    }

    /**
     * 构建WorkerWrapper
     * @param highLevelRequests
     * @param allHttpRequests
     * @param apiWorkerWrapperMapping
     * @return
     */
    private List<WorkerWrapper<HttpRequest, JSONObject>> buildWorkerWrapper(List<HttpRequest> highLevelRequests, List<HttpRequest> allHttpRequests,  Map<String, WorkerWrapper> apiWorkerWrapperMapping) {
        List<WorkerWrapper<HttpRequest, JSONObject>> list = highLevelRequests.stream().map(req -> {
            List<HttpRequest> refHttpRequests = allHttpRequests.stream().filter(h -> req.getName().equals(h.getRef())).collect(Collectors.toList());
            HttpWorker httpWorker = new HttpWorker();
            if (CollectionUtils.isEmpty(refHttpRequests)) {
                WorkerWrapper<HttpRequest, JSONObject> build = new WorkerWrapper.Builder<HttpRequest, JSONObject>()
                        .worker(httpWorker)
                        .param(req)
                        .build();
                apiWorkerWrapperMapping.put(req.getName(), build);
                return build;
            } else {
                List<WorkerWrapper<HttpRequest, JSONObject>> workerWrappers = buildWorkerWrapper(refHttpRequests, allHttpRequests, apiWorkerWrapperMapping);
                WorkerWrapper<HttpRequest, JSONObject> build = new WorkerWrapper.Builder<HttpRequest, JSONObject>()
                        .worker(httpWorker)
                        .next(workerWrappers.toArray(new WorkerWrapper[workerWrappers.size()]))
                        .param(req)
                        .build();
                apiWorkerWrapperMapping.put(req.getName(), build);
                return build;
            }

        }).collect(Collectors.toList());

        return list;
    }

    /**
     * 设置Worker参数，有的Worker参数依赖于上一个Worker的结果
     * @param httpRequests
     * @param apiWorkerWrapperMapping
     */
    private void setApiWorkerWrapperParam(List<HttpRequest> httpRequests, Map<String, WorkerWrapper> apiWorkerWrapperMapping) {
        for (HttpRequest httpRequest : httpRequests) {
            WorkerWrapper<HttpRequest, JSONObject> apiWorkerWrapper = apiWorkerWrapperMapping.get(httpRequest.getName());

            WorkerWrapper<HttpRequest, JSONObject> apiRefWorkerWrapper = apiWorkerWrapperMapping.get(httpRequest.getRef());
            if (apiRefWorkerWrapper != null) {
                httpRequest.setWorkResult(apiRefWorkerWrapper.getWorkResult());
            }

            apiWorkerWrapper.setParam(httpRequest);
        }
    }

    /**
     * 格式化结果
     * @param responseMetadata
     * @param apiWorkResultMapping
     * @return
     */
    private JSONObject formatResult(String responseMetadata, Map<String, WorkResult<JSONObject>> apiWorkResultMapping) {
        // 响应格式元数据
        JSONObject responseMetadataJson = JSONObject.parseObject(responseMetadata);
        Set<String> metaDataKeys = responseMetadataJson.keySet();
        JSONObject resultJson = new JSONObject();
        doFormatResult(metaDataKeys, responseMetadataJson, apiWorkResultMapping, resultJson);
        return resultJson;

    }

    private void doFormatResult(Set<String> metaDataKeys, JSONObject responseMetadataJson, Map<String, WorkResult<JSONObject>> workResultMap, JSONObject resultJson) {
        for (String metaDataKey : metaDataKeys) {
            JSONObject metadataJson = responseMetadataJson.getJSONObject(metaDataKey);
            if (metadataJson.containsKey(AggregationConstant.PATH) && metadataJson.containsKey(AggregationConstant.TYPE)) {
                String type = metadataJson.getString(AggregationConstant.TYPE);
                String path = metadataJson.getString(AggregationConstant.PATH);
                String[] paths = path.split(AggregationConstant.POUND);
                String apiName = paths[0];
                String pathValue = paths[1];
                WorkResult workResult = workResultMap.get(apiName);
                JSONObject workResultResultJson = (JSONObject)workResult.getResult();
                if (FieldTypeEnum.INTEGER.getType().equals(type)) {
                    Integer value = workResultResultJson.getInteger(pathValue);
                    resultJson.put(formatMetaDataKey(metaDataKey), value);
                }

                if (FieldTypeEnum.STRING.getType().equals(type)) {
                    Integer value = workResultResultJson.getInteger(pathValue);
                    resultJson.put(formatMetaDataKey(metaDataKey), value);
                }

                if (FieldTypeEnum.ENTITY.getType().equals(type)) {
                    doFormatResultByEntity(metadataJson, workResultResultJson, resultJson, pathValue, metaDataKey, workResultMap);
                }
            } else {
                JSONObject newJsonObject = new JSONObject();
                Set<String> childMetaDataKeys = metadataJson.keySet();
                doFormatResult(childMetaDataKeys, metadataJson, workResultMap, newJsonObject);
                resultJson.put(formatMetaDataKey(metaDataKey), newJsonObject);
            }

        }
    }

    private void doFormatResultByEntity(JSONObject metadataJson, JSONObject workResultResultJson, JSONObject resultJson, String pathValue, String metaDataKey, Map<String, WorkResult<JSONObject>> workResultMap) {
        JSONArray fields = new JSONArray();
        if (metadataJson.containsKey(AggregationConstant.FIELDS)) {
            fields = metadataJson.getJSONArray(AggregationConstant.FIELDS);
        }

        JSONObject newJsonObject = new JSONObject();
        JSONObject jsonObject = workResultResultJson.getJSONObject(pathValue);
        Set<String> keySets = jsonObject.keySet();
        for (String key : keySets) {
            if (fields.contains(key)) {
                newJsonObject.put(key, jsonObject.get(key));
            }
        }
        resultJson.put(formatMetaDataKey(metaDataKey), newJsonObject);

        Set<String> keySet = metadataJson.keySet();
        for (String key : keySet) {
            if (key.startsWith(AggregationConstant.CURRENCY_SYMBOL)) {
                Set<String> newKeys = new HashSet<>();
                newKeys.add(key);
                doFormatResult(newKeys, metadataJson, workResultMap, newJsonObject);
                resultJson.put(formatMetaDataKey(metaDataKey), newJsonObject);
            }
        }
    }

    /**
     * 格式化元数据Key
     * @param key
     * @return
     */
    private String formatMetaDataKey(String key) {
        if (key.startsWith(AggregationConstant.CURRENCY_SYMBOL)) {
            key = key.replace(AggregationConstant.CURRENCY_SYMBOL, AggregationConstant.DEFAULT_EMPTY_STR).trim();
        }
        return key;
    }
}