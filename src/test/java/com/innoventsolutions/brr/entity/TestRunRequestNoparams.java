package com.innoventsolutions.brr.entity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestRunRequestNoparams {
	String designFile;
	String format;
	String securityToken;
	Boolean runThenRender;

}

