package com.creatubbles.ctbmod.common.http;

import lombok.Value;

import com.google.gson.annotations.SerializedName;

@Value
public class Creation
{
    private int id;
    
    @SerializedName("user_id")
    private int userId;
    
    private String name;
    private Image image;
}
