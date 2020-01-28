package com.innoventsolutions.brr.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestScheduleRequest {
	String group;
	String name;
	@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
	Date startDate;
	String securityToken;
	TestSubmitRequestNoparams submit;
}

