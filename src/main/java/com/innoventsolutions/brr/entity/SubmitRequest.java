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

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
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

}
