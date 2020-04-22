package com.cxytiandi.kitty.aggregation.worker;

import com.alibaba.fastjson.JSONObject;

import com.cxytiandi.kitty.aggregation.helper.ApplicationContextHelper;
import com.cxytiandi.kitty.aggregation.invoker.HttpApiInvoker;
import com.cxytiandi.kitty.aggregation.request.HttpRequest;
import com.jd.platform.async.callback.IWorker;

/**
 * Http 请求任务
 *
 * @作者 尹吉欢
 * @个人微信 jihuan900
 * @微信公众号 猿天地
 * @GitHub https://github.com/yinjihuan
 * @作者介绍 http://cxytiandi.com/about
 * @时间 2020-04-12 21:57
 */
public class HttpWorker implements IWorker<HttpRequest, JSONObject> {

    private HttpApiInvoker httpApiInvoker;

    @Override
    public JSONObject action(HttpRequest httpRequest) {
        httpApiInvoker = ApplicationContextHelper.getBean(HttpApiInvoker.class);
        return httpApiInvoker.invoke(httpRequest);
    }

    @Override
    public JSONObject defaultValue() {
        return new JSONObject();
    }
}