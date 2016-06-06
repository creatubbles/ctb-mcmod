package com.creatubbles.ctbmod.common.http;

import lombok.Getter;
import lombok.experimental.Delegate;

import com.creatubbles.api.core.Creation;
import com.creatubbles.api.response.relationships.Relationships;

@Getter
public class CreationRelations extends Creation {
	
	@Delegate
	private final Creation creation;
	private final Relationships relationships;
	
	public CreationRelations(Creation c, Relationships r) {
		creation = c;
		relationships = r;
	}
}
