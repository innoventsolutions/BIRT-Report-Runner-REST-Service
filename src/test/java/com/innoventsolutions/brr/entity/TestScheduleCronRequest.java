package com.innoventsolutions.brr.entity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestScheduleCronRequest extends TestScheduleRequest {
	String cronString;
	String misfireInstruction;

}