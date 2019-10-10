package com.innoventsolutions.brr.entity;

public class GetJobRequest extends BaseRequest {
	private String name;
	private String group;

	public String getName() {
		return name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(final String group) {
		this.group = group;
	}

	@Override
	public String toString() {
		return name + "/" + group;
	}
}
