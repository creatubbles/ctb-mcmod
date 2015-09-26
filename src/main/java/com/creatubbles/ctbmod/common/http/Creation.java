package com.creatubbles.ctbmod.common.http;

import lombok.ToString;
import lombok.Value;

import com.google.gson.annotations.SerializedName;

@Value
@ToString(exclude = "image")
public class Creation {

	private int id;

	@SerializedName("user_id")
	private int userId;
	
	private Creator[] creators;

	private String name;
	private Image image;
}
