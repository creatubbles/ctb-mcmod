package com.creatubbles.ctbmod.common.http;

import com.google.gson.JsonObject;

public class CreationsRequest extends HttpGetBase<Creation[], Void> {

	private static String createApiPath(int creatorId, String accessToken) {
		String ret = "creators/" + creatorId + "/creations.json";
		if (accessToken != null) {
			ret += "?access_token=" + accessToken;
		}
		return ret;
	}

	public CreationsRequest(int creatorId) {
		this(creatorId, null);
	}

	public CreationsRequest(int creatorId, String accessToken) {
		super(createApiPath(creatorId, accessToken));
	}

	@Override
	protected Creation[] getSuccessfulResult(JsonObject response) {
		return gson.fromJson(response.get("creations"), Creation[].class);
	}

	@Override
	protected Void getFailedResult(JsonObject response) {
		return null;
	}
}
