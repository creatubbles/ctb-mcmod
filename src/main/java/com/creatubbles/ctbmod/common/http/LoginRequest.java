package com.creatubbles.ctbmod.common.http;

import lombok.Getter;
import lombok.ToString;

import com.creatubbles.ctbmod.common.http.LoginRequest.LoginResponse;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

public class LoginRequest extends POSTBase<LoginResponse>
{
    @Getter
    @ToString
    public static class LoginResponse
    {
        private String message;
        
        @SerializedName("access_token")
        private String accessToken;
    }
    
    private User user;
    
    public LoginRequest(User user)
    {
        super("users/sign_in.json");
        this.user = user;
    }
    
    @Override
    protected JsonObject getObject()
    {
        JsonObject ret = new JsonObject();
        ret.add("user", gson.toJsonTree(user));
        return ret;
    }

    @Override
    protected LoginResponse getResult(JsonObject response)
    {
        return gson.fromJson(response, LoginResponse.class);
    }
}
