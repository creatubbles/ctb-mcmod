package com.creatubbles.ctbmod.common.http;

import org.apache.http.client.methods.HttpGet;

public abstract class HttpGetBase<SUCCESS, FAIL> extends HttpRequest<SUCCESS, FAIL>
{
    public HttpGetBase(String apiPath)
    {
        super(apiPath);
    }

    @Override
    protected HttpGet getRequest(String url) throws HttpRequestException
    {
        return new HttpGet(url);
    }
}
