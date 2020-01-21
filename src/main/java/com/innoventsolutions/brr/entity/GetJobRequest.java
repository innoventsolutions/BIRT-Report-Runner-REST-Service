package com.innoventsolutions.brr.entity;

import lombok.Data;

@Data
public class GetJobRequest extends BaseRequest {
	private String name;
	private String group;

}
