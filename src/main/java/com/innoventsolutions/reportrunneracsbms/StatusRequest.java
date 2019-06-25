package com.innoventsolutions.reportrunneracsbms;

public class StatusRequest {
	private String jobId;
	private String securityToken;

	public String getJobId() {
		return jobId;
	}

	public void setJobId(final String jobId) {
		this.jobId = jobId;
	}

	public String getSecurityToken() {
		return securityToken;
	}

	public void setSecurityToken(final String securityToken) {
		this.securityToken = securityToken;
	}
}
