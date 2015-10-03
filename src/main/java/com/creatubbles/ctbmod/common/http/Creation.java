package com.creatubbles.ctbmod.common.http;

import lombok.ToString;
import lombok.Value;

import com.google.gson.annotations.SerializedName;

@Value
@ToString(exclude = "image")
public class Creation implements Comparable<Creation> {

	private int id;

	@SerializedName("user_id")
	private int userId;
	
	@SerializedName("created_at")
	private String createdDate;
	
	private Creator[] creators;

	private String name;
	private Image image;

	@Override
	public int compareTo(Creation o) {
		return getCreatedDate().compareTo(o.getCreatedDate());
	}
}
