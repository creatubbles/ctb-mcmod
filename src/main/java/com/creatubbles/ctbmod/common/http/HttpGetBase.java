package com.creatubbles.ctbmod.common.http;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;

public abstract class HttpGetBase<SUCCESS, FAIL> extends HttpRequest<SUCCESS, FAIL>
{
    public HttpGetBase(String apiPath)
    {
        super(apiPath);
    }

    @Override
    protected HttpRequestBase getRequest(String url) throws HttpRequestException
    {
        return new HttpGet(url);
    }
}
