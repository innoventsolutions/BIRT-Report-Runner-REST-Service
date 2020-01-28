package com.innoventsolutions.brr.entity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class TestScheduleSimpleRequest extends TestScheduleRequest {
	Long intervalInMilliseconds;
	Integer repeatCount;
	String misfireInstruction;

}
