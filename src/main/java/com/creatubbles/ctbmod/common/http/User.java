package com.creatubbles.ctbmod.common.http;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

import com.google.gson.annotations.SerializedName;

@Value
@RequiredArgsConstructor
public class User {

	private int id;
	private String username, email, country, role;
	@SerializedName("is_teacher")
	private boolean teacher;
	@SerializedName("is_loggable")
	private boolean loggable;

	@Setter
	@NonFinal
	private String accessToken;
}
