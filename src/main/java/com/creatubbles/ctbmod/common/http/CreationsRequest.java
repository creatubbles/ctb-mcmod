package com.creatubbles.ctbmod.common.http;

import com.creatubbles.ctbmod.common.command.CommandLogin;
import com.google.gson.JsonObject;

public class CreationsRequest extends HttpGetBase<Creation[], Void>
{
    public CreationsRequest(int creatorId)
    {
        super("creators/" + creatorId + "/creations.json?access_token=" + CommandLogin.accessToken);
    }

    @Override
    protected Creation[] getSuccessfulResult(JsonObject response)
    {
        return gson.fromJson(response.get("creations"), Creation[].class);
    }

    @Override
    protected Void getFailedResult(JsonObject response)
    {
        return null;
    }
}
