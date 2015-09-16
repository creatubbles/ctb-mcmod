package com.creatubbles.ctbmod.common.http;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RequiredArgsConstructor
@Getter
public abstract class HttpPostBase<SUCCESS, FAIL>
{
    public static final String URL_BASE = "https://www.creatubbles.com/api/v1/";

    protected final Gson gson = new Gson();

    protected final String apiPath;
    
    private SUCCESS successfulResult;
    private FAIL failedResult;

    protected abstract JsonObject getObject();

    protected abstract SUCCESS getSuccessfulResult(JsonObject response);
    
    protected abstract FAIL getFailedResult(JsonObject response);

    public final void post() throws HttpPostException
    {
        String url = URL_BASE.concat(apiPath);

        HttpClient client = HttpClientBuilder.create().build();
        HttpPost post = new HttpPost(url);

        // Set content headers
        post.setHeader("content-type", "application/json");
        post.setHeader("accept", "application/json");

        try
        {
            // Add the JSON as a param
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
            // Send POST
            response = client.execute(post);
        }
        catch (IOException e)
        {
            throw new HttpPostException("Error sending POST!");
        }

        try
        {
            // Parse the result into a JsonObject
            JsonObject res = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
            StatusLine status = response.getStatusLine();
            int code = status.getStatusCode();
            if (code != 200)
            {
                // If response is not 200, the POST has failed, so read the JSON into the failed object
                failedResult = getFailedResult(res);
            }
            else
            {
                // If response is 200 continue as normal
                successfulResult = getSuccessfulResult(res);
            }
        }
        catch (IOException e)
        {
            throw new HttpPostException("Error parsing response!", response);
        }
    }
    
    public final boolean failed()
    {
        return failedResult != null;
    }
}
