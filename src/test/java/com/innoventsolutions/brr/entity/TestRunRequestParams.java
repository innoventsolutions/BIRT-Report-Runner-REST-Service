package com.innoventsolutions.brr.entity;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestRunRequestParams extends TestRunRequestNoparams {
	Map<String, Object> parameters;

}