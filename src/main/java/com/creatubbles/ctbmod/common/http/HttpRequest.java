package com.creatubbles.ctbmod.common.http;

import java.io.IOException;
import java.io.InputStreamReader;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@RequiredArgsConstructor
@Getter
public abstract class HttpRequest<SUCCESS, FAIL> implements Runnable {

	public static final String URL_BASE = "https://www.creatubbles.com/api/v1/";

	protected final Gson gson = new Gson();

	protected final String apiPath;

	private SUCCESS successfulResult;
	private FAIL failedResult;
	private HttpRequestException exception;
	private boolean complete;

	/**
	 * @param url
	 *            The url assembled from the {@link URL_BASE} and the passed apiPath
	 * @return the HTTP request you need to send. The headers for JSON content-type and accept will be set automatically.
	 */
	protected abstract HttpUriRequest getRequest(String url) throws HttpRequestException;

	/**
	 * Create a successful result from the received JSON data
	 * 
	 * @param response
	 *            The JSON data from the request parsed into an object
	 * @return An object representing a successful result
	 */
	protected abstract SUCCESS getSuccessfulResult(JsonObject response);

	/**
	 * Create a failed result from the received JSON data
	 * 
	 * @param response
	 *            The JSON data from the request parsed into an object
	 * @return An object representing a failed result
	 */
	protected abstract FAIL getFailedResult(JsonObject response);

	public final void post() throws HttpRequestException {
		String url = URL_BASE.concat(apiPath);

		HttpClient client = HttpClientBuilder.create().build();
		HttpUriRequest req = getRequest(url);

		// Set content headers
		req.setHeader("content-type", "application/json");
		req.setHeader("accept", "application/json");

		HttpResponse response;

		try {
			// Send POST
			response = client.execute(req);
		} catch (IOException e) {
			complete = true;
			throw new HttpRequestException("Error sending POST!");
		}

		try {
			// Parse the result into a JsonObject
			JsonObject res = new JsonParser().parse(new InputStreamReader(response.getEntity().getContent())).getAsJsonObject();
			StatusLine status = response.getStatusLine();
			int code = status.getStatusCode();
			if (code != 200) {
				// If response is not 200, the POST has failed, so read the JSON into the failed object
				failedResult = getFailedResult(res);
			} else {
				// If response is 200 continue as normal
				successfulResult = getSuccessfulResult(res);
			}
		} catch (IOException e) {
			complete = true;
			throw new HttpRequestException("Error parsing response!", response);
		}
		complete = true;
	}

	@Override
	public final void run() {
		try {
			post();
		} catch (HttpRequestException e) {
			this.exception = e;
		}
	}

	public boolean failed() {
		return successfulResult == null;
	}
}
