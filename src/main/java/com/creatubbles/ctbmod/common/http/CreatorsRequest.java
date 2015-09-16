package com.creatubbles.ctbmod.common.http;

import lombok.Value;

import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.creatubbles.ctbmod.common.http.CreatorsRequest.CreatorsResponse;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class CreatorsRequest extends HttpGetBase<CreatorsResponse, String>
{
    @Value
    public static class CreatorsResponse
    {
        private Creator[] creators;
        private int page;
        @SerializedName("total_pages")
        private int totalPages;
    }
    
    public CreatorsRequest()
    {
        super("users/me/creators.json?access_token=" + CommandLogin.accessToken);
    }

    public CreatorsRequest(String userId)
    {
        super("users/" + userId + "/creators.json");
    }

    @Override
    protected CreatorsResponse getSuccessfulResult(JsonObject response)
    {
        return gson.fromJson(response, CreatorsResponse.class);
    }

    @Override
    protected String getFailedResult(JsonObject response)
    {
        return response.get("message").getAsString();
    }
}
