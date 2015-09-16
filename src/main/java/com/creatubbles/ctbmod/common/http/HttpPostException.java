package com.creatubbles.ctbmod.common.http;

import javax.annotation.Nullable;

import lombok.Getter;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

public class HttpPostException extends HttpException
{
    private static final long serialVersionUID = 5970128938694742779L;
    
    @Nullable
    @Getter
    private HttpResponse response;
    
    public HttpPostException(HttpResponse response)
    {
        this(null, response);
    }
    
    public HttpPostException(String message)
    {
        this(message, null);
    }
    
    public HttpPostException(String message, HttpResponse response)
    {
        super(message);
        this.response = response;
    }
}
