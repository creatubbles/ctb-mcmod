package com.creatubbles.ctbmod.client.gui;

import javax.annotation.Nullable;

import com.creatubbles.ctbmod.common.http.Creation;

public interface ISelectionCallback {

	void callback(@Nullable Creation selected);

}
