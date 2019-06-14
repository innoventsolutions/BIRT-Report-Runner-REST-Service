/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.birt.runner;

public class ReportEmail {
	public final boolean sendOnSuccess;
	public final boolean sendOnFailure;
	public final String mailTo;
	public final String mailCc;
	public final String mailBcc;
	public final String mailSuccessSubject;
	public final String mailFailureSubject;
	public final String mailSuccessBody;
	public final String mailFailureBody;

	public ReportEmail(final boolean sendOnSuccess, final boolean sendOnFailure,
			final String mailTo, final String mailCc, final String mailBcc,
			final String mailSuccessSubject, final String mailFailureSubject,
			final String mailSuccessBody, final String mailFailureBody) {
		this.sendOnSuccess = sendOnSuccess;
		this.sendOnFailure = sendOnFailure;
		this.mailTo = mailTo;
		this.mailCc = mailCc;
		this.mailBcc = mailBcc;
		this.mailSuccessSubject = mailSuccessSubject;
		this.mailFailureSubject = mailFailureSubject;
		this.mailSuccessBody = mailSuccessBody;
		this.mailFailureBody = mailFailureBody;
	}
}
