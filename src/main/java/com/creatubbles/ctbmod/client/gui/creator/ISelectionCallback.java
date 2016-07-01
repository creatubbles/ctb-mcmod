package com.creatubbles.ctbmod.client.gui.creator;

import javax.annotation.Nullable;

import com.creatubbles.ctbmod.common.http.CreationRelations;

public interface ISelectionCallback {

    void callback(@Nullable CreationRelations selected);

}
