package com.creatubbles.ctbmod.common.http;

import com.google.gson.JsonObject;

public class UserRequest extends HttpGetBase<User, String> {

	public UserRequest(String accessToken) {
		super("users/me.json?access_token=" + accessToken);
	}

	@Override
	protected User getSuccessfulResult(JsonObject response) {
		return gson.fromJson(response, User.class);
	}

	@Override
	protected String getFailedResult(JsonObject response) {
		return response.get("message").getAsString();
	}
}
