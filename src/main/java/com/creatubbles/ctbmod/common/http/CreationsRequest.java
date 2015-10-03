package com.creatubbles.ctbmod.common.http;

import java.lang.reflect.Type;

import lombok.Value;

import com.creatubbles.ctbmod.common.http.CreationsRequest.CreationsResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.SerializedName;

public class CreationsRequest extends HttpGetBase<CreationsResponse, Void> {

	private static class ImageDeserializer implements JsonDeserializer<Image> {

		@Override
		public Image deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
			return new Image(json.getAsJsonObject().get("url").getAsString());
		}
	}
	
	@Value
	public static class CreationsResponse {

		@SerializedName("total_entries")
		private int totalEntries;
		@SerializedName("total_pages")
		private int totalPages;
		private int page;
		private Creation[] creations;
	}

	private static String createApiPath(int creatorId, int page, String accessToken) {
		String ret = "creators/" + creatorId + "/creations.json?page=" + page;
		if (accessToken != null) {
			ret += "&access_token=" + accessToken;
		}
		return ret;
	}

	public CreationsRequest(int creatorId) {
		this(creatorId, 1, null);
	}
	
	public CreationsRequest(int creatorId, int page) {
		this(creatorId, page, null);
	}

	public CreationsRequest(int creatorId, int page, String accessToken) {
		super(createApiPath(creatorId, page, accessToken));
	}

	@Override
	protected Gson createGson() {
		return new GsonBuilder().registerTypeAdapter(Image.class, new ImageDeserializer()).create();
	}

	@Override
	protected CreationsResponse getSuccessfulResult(JsonObject response) {
		CreationsResponse ret = gson.fromJson(response, CreationsResponse.class);
		for (Creation c : ret.getCreations()) {
			c.getImage().setOwner(c);
		}
		return ret;
	}

	@Override
	protected Void getFailedResult(JsonObject response) {
		return null;
	}
}
