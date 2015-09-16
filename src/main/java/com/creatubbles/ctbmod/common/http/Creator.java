package com.creatubbles.ctbmod.common.http;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@ToString
public class Creator
{
    private int id;
    private String name, age;
    
    @Setter
    private transient String accessToken;
}
