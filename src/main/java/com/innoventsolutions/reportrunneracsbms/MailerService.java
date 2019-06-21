/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.reportrunneracsbms;

import java.io.File;
import java.io.FileInputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.innoventsolutions.birt.runner.ReportEmail;
import com.innoventsolutions.birt.runner.ReportRunStatus;

@Service
public class MailerService {
	Logger logger = LoggerFactory.getLogger(MailerService.class);
	private final ConfigService configuration;

	public MailerService(@Autowired final ConfigService configuration) {
		this.configuration = configuration;
	}

	public void send(final ReportRunStatus status) {
		try {
			if (configuration.mailPropertiesFile == null) {
				logger.info("Email not sent because smtp properties file not specified");
				status.finishEmail(null);
				return;
			}
			if (!configuration.mailPropertiesFile.exists()) {
				logger.info("Email not sent because smtp properties file "
					+ configuration.mailPropertiesFile + " does not exist");
				status.finishEmail(null);
				return;
			}
			if (configuration.mailFrom == null) {
				logger.info("Email not sent because email-to not specified");
				status.finishEmail(null);
				return;
			}
			final ReportEmail email = status.email;
			if (email == null) {
				logger.info("Email not sent because email request is missing");
				status.finishEmail(null);
				return;
			}
			final boolean success = status.getErrors().isEmpty();
			final boolean sendOnSuccess = email.mailSuccess != null
				? email.mailSuccess.booleanValue()
				: configuration.mailSuccess;
			if (!sendOnSuccess) {
				if (success) {
					logger.info("Email not sent because success emails not wanted");
					status.finishEmail(null);
					return;
				}
			}
			final boolean sendOnFailure = email.mailFailure != null
				? email.mailFailure.booleanValue()
				: configuration.mailFailure;
			if (!sendOnFailure) {
				if (!success) {
					logger.info("Email not sent because failure emails not wanted");
					status.finishEmail(null);
					return;
				}
			}
			logger.info("Mailer is sending email");
			final Properties emailProperties = new Properties();
			emailProperties.load(new FileInputStream(configuration.mailPropertiesFile));
			logger.info("emailPropertiesFile = " + configuration.mailPropertiesFile);
			final Session session;
			if (configuration.mailUsername != null && configuration.mailPassword != null) {
				logger.info("Creating email session with authentication");
				final PasswordAuthentication pa = new PasswordAuthentication(
						configuration.mailUsername, configuration.mailPassword);
				session = Session.getDefaultInstance(emailProperties, new Authenticator() {
					@Override
					protected PasswordAuthentication getPasswordAuthentication() {
						return pa;
					}
				});
			}
			else {
				logger.info("Creating email session without authentication");
				session = Session.getDefaultInstance(emailProperties);
			}
			session.setDebug(true);
			final String mailTo = join(configuration.mailTo, email.mailTo);
			final String[] recipients = mailTo.split(", *");
			final Map<String, Exception> exceptions = new HashMap<>();
			for (final String recipient : recipients) {
				final MimeMessage mimeMessage = new MimeMessage(session);
				try {
					mimeMessage.setFrom(new InternetAddress(configuration.mailFrom));
					mimeMessage.addRecipient(Message.RecipientType.TO,
						new InternetAddress(recipient));
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
					final Multipart mp = new MimeMultipart();
					if (body != null) {
						final MimeBodyPart mbp = new MimeBodyPart();
						final boolean html = email.mailHtml != null ? email.mailHtml.booleanValue()
							: configuration.mailHtml;
						mbp.setContent(body, html ? "text/html;charset=utf-8" : "text/plain");
						mp.addBodyPart(mbp);
					}
					final boolean attach = email.mailAttachReport != null
						? email.mailAttachReport.booleanValue()
						: configuration.mailAttachReport;
					final File outputFile = new File(configuration.outputDirectory,
							status.reportRun.outputFile);
					if (attach && success && outputFile.exists()) {
						final MimeBodyPart mbp = new MimeBodyPart();
						final DataSource dataSource = new FileDataSource(outputFile);
						mbp.setDataHandler(new DataHandler(dataSource));
						mbp.setFileName(status.reportRun.outputFile);
						mp.addBodyPart(mbp);
					}
					mimeMessage.setContent(mp);
					mimeMessage.setSentDate(new Date());
					Transport.send(mimeMessage);
				}
				catch (final MessagingException e) {
					logger.error("Failed to send email to " + recipient, e);
					exceptions.put(recipient, e);
				}
			}
			status.finishEmail(exceptions);
		}
		catch (final Exception e) {
			logger.error("Failed to send email", e);
			final Map<String, Exception> exceptions = new HashMap<>();
			exceptions.put("", e);
			status.finishEmail(exceptions);
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
