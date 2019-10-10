/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr.entity;

import java.util.HashMap;
import java.util.Map;

public class SubmitRequest extends BaseRequest {
	private String designFile;
	private String format;
	private boolean runThenRender;
	private Map<String, Object> parameters = new HashMap<>();
	private String nameForHumans;
	private Boolean sendEmailOnSuccess;
	private Boolean sendEmailOnFailure;
	private String mailTo;
	private String mailCc;
	private String mailBcc;
	private String mailSuccessSubject;
	private String mailFailureSubject;
	private String mailSuccessBody;
	private String mailFailureBody;
	private Boolean mailAttachReport;
	private Boolean mailHtml;

	public String getDesignFile() {
		return designFile;
	}

	public void setDesignFile(final String designFile) {
		this.designFile = designFile;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(final String format) {
		this.format = format;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public void setParameters(final Map<String, Object> parameters) {
		this.parameters = parameters;
	}

	public boolean isRunThenRender() {
		return runThenRender;
	}

	public void setRunThenRender(final boolean runThenRender) {
		this.runThenRender = runThenRender;
	}

	public String getNameForHumans() {
		return nameForHumans;
	}

	public void setNameForHumans(final String nameForHumans) {
		this.nameForHumans = nameForHumans;
	}

	public String getMailTo() {
		return this.mailTo;
	}

	public void setMailTo(final String mailTo) {
		this.mailTo = mailTo;
	}

	public String getMailCc() {
		return mailCc;
	}

	public void setMailCc(final String mailCc) {
		this.mailCc = mailCc;
	}

	public String getMailBcc() {
		return mailBcc;
	}

	public void setMailBcc(final String mailBcc) {
		this.mailBcc = mailBcc;
	}

	public String getMailSuccessSubject() {
		return mailSuccessSubject;
	}

	public void setMailSuccessSubject(final String mailSuccessSubject) {
		this.mailSuccessSubject = mailSuccessSubject;
	}

	public String getMailFailureSubject() {
		return mailFailureSubject;
	}

	public void setMailFailureSubject(final String mailFailureSubject) {
		this.mailFailureSubject = mailFailureSubject;
	}

	public String getMailSuccessBody() {
		return mailSuccessBody;
	}

	public void setMailSuccessBody(final String mailSuccessBody) {
		this.mailSuccessBody = mailSuccessBody;
	}

	public String getMailFailureBody() {
		return mailFailureBody;
	}

	public void setMailFailureBody(final String mailFailureBody) {
		this.mailFailureBody = mailFailureBody;
	}

	public Boolean isSendEmailOnSuccess() {
		return sendEmailOnSuccess;
	}

	public void setSendEmailOnSuccess(final Boolean sendEmailOnSuccess) {
		this.sendEmailOnSuccess = sendEmailOnSuccess;
	}

	public Boolean isSendEmailOnFailure() {
		return sendEmailOnFailure;
	}

	public void setSendEmailOnFailure(final Boolean sendEmailOnFailure) {
		this.sendEmailOnFailure = sendEmailOnFailure;
	}

	public Boolean getMailAttachReport() {
		return mailAttachReport;
	}

	public void setMailAttachReport(final Boolean mailAttachReport) {
		this.mailAttachReport = mailAttachReport;
	}

	public Boolean getSendEmailOnSuccess() {
		return sendEmailOnSuccess;
	}

	public Boolean getSendEmailOnFailure() {
		return sendEmailOnFailure;
	}

	public Boolean getMailHtml() {
		return mailHtml;
	}

	public void setMailHtml(final Boolean mailHtml) {
		this.mailHtml = mailHtml;
	}
}
