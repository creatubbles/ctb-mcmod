package com.creatubbles.ctbmod.common.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import lombok.AllArgsConstructor;
import lombok.Getter;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@AllArgsConstructor
@Getter
public abstract class POSTBase<T>
{
    public static final String URL_BASE = "https://www.creatubbles.com/api/v1/";

    protected final Gson gson = new Gson();

    protected final String apiPath;

    protected abstract JsonObject getObject();

    protected abstract T getResult(JsonObject response);

    public final T post() throws HttpPostException
    {
        String url = URL_BASE.concat(apiPath);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        post.setHeader("content-type", "application/json");
        post.setHeader("accept", "application/json");

        try
        {
            StringEntity params = new StringEntity(getObject().toString());
            post.setEntity(params);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new HttpPostException("Illegal object encoding!");
        }

        HttpResponse response;

        try
        {
            response = client.execute(post);
        }
        catch (IOException e)
        {
            throw new HttpPostException("Error sending POST!");
        }

        try
        {
            return getResult(new JsonParser().parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject());
        }
        catch (IOException e)
        {
            throw new HttpPostException("Error parsing response!", response);
        }
    }
}
