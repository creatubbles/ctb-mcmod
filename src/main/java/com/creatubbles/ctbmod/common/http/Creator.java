package com.creatubbles.ctbmod.common.http;

import lombok.Setter;
import lombok.Value;
import lombok.experimental.NonFinal;

@Value
public class Creator {

	private int id;
	private String name, age;

	@Setter
	@NonFinal
	private transient String accessToken;
}
