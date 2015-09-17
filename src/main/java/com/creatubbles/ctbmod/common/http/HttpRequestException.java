package com.creatubbles.ctbmod.common.http;

import javax.annotation.Nullable;

import lombok.Getter;

import org.apache.http.HttpException;
import org.apache.http.HttpResponse;

public class HttpRequestException extends HttpException {

	private static final long serialVersionUID = 5970128938694742779L;

	@Nullable
	@Getter
	private HttpResponse response;

	public HttpRequestException(HttpResponse response) {
		this(null, response);
	}

	public HttpRequestException(String message) {
		this(message, null);
	}

	public HttpRequestException(String message, HttpResponse response) {
		super(message);
		this.response = response;
	}
}
