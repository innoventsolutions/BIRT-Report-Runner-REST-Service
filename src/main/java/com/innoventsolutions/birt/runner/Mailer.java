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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Mailer {
	Logger logger = LoggerFactory.getLogger(Mailer.class);
	private final Configuration configuration;

	public Mailer(final Configuration configuration) {
		if (configuration == null) {
			throw new NullPointerException("configuration may not be null");
		}
		this.configuration = configuration;
	}

	public void send(final ReportRunStatus status) throws FileNotFoundException, IOException {
		if (configuration.mailPropertiesFile == null) {
			return;
		}
		if (configuration.mailFrom == null) {
			return;
		}
		final ReportEmail email = status.email;
		if (email == null) {
			return;
		}
		final boolean success = status.getErrors().isEmpty();
		if (!email.sendOnSuccess) {
			if (success) {
				return;
			}
		}
		if (!email.sendOnFailure) {
			if (!success) {
				return;
			}
		}
		final Properties emailProperties = new Properties();
		emailProperties.load(new FileInputStream(configuration.mailPropertiesFile));
		final PasswordAuthentication pa = new PasswordAuthentication(configuration.mailUsername,
				configuration.mailPassword);
		final Session session = Session.getDefaultInstance(emailProperties, new Authenticator() {
			@Override
			protected PasswordAuthentication getPasswordAuthentication() {
				return pa;
			}
		});
		session.setDebug(true);
		final String mailTo = join(configuration.mailTo, email.mailTo);
		final String[] recipients = mailTo.split(", *");
		for (final String recipient : recipients) {
			final MimeMessage mimeMessage = new MimeMessage(session);
			try {
				mimeMessage.setFrom(new InternetAddress(configuration.mailFrom));
				mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recipient));
				final String mailCc = join(configuration.mailCc, email.mailCc);
				if (!mailCc.trim().isEmpty()) {
					final String[] ccArray = mailCc.split(", *");
					for (final String cc : ccArray) {
						if (cc.trim().length() > 0) {
							mimeMessage.addRecipient(Message.RecipientType.CC,
								new InternetAddress(cc));
						}
					}
				}
				final String mailBcc = join(configuration.mailBcc, email.mailBcc);
				if (!mailBcc.trim().isEmpty()) {
					final String[] ccArray = mailBcc.split(", *");
					for (final String cc : ccArray) {
						if (cc.trim().length() > 0) {
							mimeMessage.addRecipient(Message.RecipientType.BCC,
								new InternetAddress(cc));
						}
					}
				}
				final String mailSuccessSubject = supercede(configuration.mailSuccessSubject,
					email.mailSuccessSubject);
				final String mailFailureSubject = supercede(configuration.mailFailureSubject,
					email.mailFailureSubject);
				final String subject = success
					? mailSuccessSubject.trim().isEmpty() ? "Success"
						: status.replace(mailSuccessSubject)
					: mailFailureSubject.trim().isEmpty() ? "Failure"
						: status.replace(mailFailureSubject);
				mimeMessage.setSubject(subject);
				final String mailSuccessBody = supercede(configuration.mailSuccessBody,
					email.mailSuccessBody);
				final String mailFailureBody = supercede(configuration.mailFailureBody,
					email.mailFailureBody);
				final String body = success ? status.replace(mailSuccessBody)
					: mailFailureBody.trim().isEmpty() ? null : status.replace(mailFailureBody);
				if (body != null) {
					// mimeMessage.setText(body);
					final Multipart mp = new MimeMultipart();
					final MimeBodyPart mbp = new MimeBodyPart();
					mbp.setContent(body,
						configuration.mailHtml ? "text/html;charset=utf-8" : "text/plain");
					mp.addBodyPart(mbp);
					mimeMessage.setContent(mp);
					if (configuration.mailAttachReport && success) {
						final MimeBodyPart attachmentMbp = new MimeBodyPart();
						final File outputDir = configuration.outputDirectory;
						final File outputFile = new File(outputDir, status.reportRun.outputFile);
						final DataSource dataSource = new FileDataSource(outputFile);
						attachmentMbp.setDataHandler(new DataHandler(dataSource));
						attachmentMbp.setFileName(status.reportRun.outputFile);
						mp.addBodyPart(attachmentMbp);
					}
				}
				mimeMessage.setSentDate(new Date());
				Transport.send(mimeMessage);
			}
			catch (final MessagingException e) {
				logger.error("Failed to send email", e);
			}
		}
	}

	private static String join(String s1, String s2) {
		if (s1 == null) {
			s1 = "";
		}
		if (s2 == null) {
			s2 = "";
		}
		if (!s2.trim().isEmpty()) {
			if (!s1.trim().isEmpty()) {
				s1 += ", ";
			}
			s1 += s2;
		}
		return s1;
	}

	private static String supercede(String s1, String s2) {
		if (s1 == null) {
			s1 = "";
		}
		if (s2 == null) {
			s2 = "";
		}
		if (!s2.trim().isEmpty()) {
			return s2;
		}
		return s1;
	}
}
