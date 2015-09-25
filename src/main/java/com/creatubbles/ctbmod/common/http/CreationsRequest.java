package com.creatubbles.ctbmod.common.http;

import java.lang.reflect.Type;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class CreationsRequest extends HttpGetBase<Creation[], Void> {

	private static class ImageDeserializer implements JsonDeserializer<Image> {

		@Override
		public Image deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return new Image(json.getAsJsonObject().get("url").getAsString());
		}
	}

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
	protected Gson createGson() {
		return new GsonBuilder().registerTypeAdapter(Image.class, new ImageDeserializer()).create();
	}

	@Override
	protected Creation[] getSuccessfulResult(JsonObject response) {
		Creation[] ret = gson.fromJson(response.get("creations"), Creation[].class);
		for (Creation c : ret) {
			c.getImage().setOwner(c);
		}
		return ret;
	}

	@Override
	protected Void getFailedResult(JsonObject response) {
		return null;
	}
}
