package com.innoventsolutions.reportrunneracsbms;

public class WaitforRequest extends StatusRequest {
	private Long timeout;

	public Long getTimeout() {
		return timeout;
	}

	public void setTimeout(final Long timeout) {
		this.timeout = timeout;
	}
}
