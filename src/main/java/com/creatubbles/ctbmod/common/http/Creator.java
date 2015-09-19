package com.creatubbles.ctbmod.common.http;

import com.google.gson.annotations.SerializedName;

import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class Creator {

	private int id;
	@SerializedName("creator_user_id")
	private int userId;
	private String name, age;

	@Setter
	@NonFinal
	private transient String accessToken;
}
