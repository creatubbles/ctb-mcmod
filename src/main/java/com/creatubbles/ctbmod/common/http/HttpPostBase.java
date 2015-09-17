package com.creatubbles.ctbmod.common.http;

import java.io.UnsupportedEncodingException;

import lombok.Getter;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;

import com.google.gson.JsonObject;

@Getter
public abstract class HttpPostBase<SUCCESS, FAIL> extends HttpRequest<SUCCESS, FAIL> {

	public HttpPostBase(String apiPath) {
		super(apiPath);
	}

	protected abstract JsonObject getObject();

	@Override
	protected HttpPost getRequest(String url) throws HttpRequestException {
		HttpPost post = new HttpPost(url);

		try {
			// Add the JSON as a param
			StringEntity params = new StringEntity(getObject().toString());
			post.setEntity(params);
			return post;
		} catch (UnsupportedEncodingException e) {
			throw new HttpRequestException("Illegal object encoding!");
		}
	}
}
