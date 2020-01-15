/*******************************************************************************
 * Copyright (C) 2019 Innovent Solutions
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package com.innoventsolutions.brr;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.innoventsolutions.brr.jpa.dao.AuthorizationRepository;
import com.innoventsolutions.brr.jpa.model.Authorization;


@RunWith(SpringRunner.class)
@SpringBootTest(classes = ReportRunnerApplication.class)
@WebAppConfiguration
public class DaoTester {
	
	@Autowired
	private AuthorizationRepository authRepository;

	@Test
	public void authorizationTest() {
		List<Authorization> badAuths = authRepository.findAllBySecurityToken("fud");
		Assert.assertNotNull(badAuths);
		Assert.assertTrue (0 == badAuths.size());
		
		Authorization a = new Authorization();
		a.setId(1);
		a.setSecurityToken("fud");
		a.setDesignFile("report");
		a.setSubmitTime(null);
		authRepository.save(a);
		List<Authorization> allAuths = (List<Authorization>)authRepository.findAll();
		Assert.assertFalse(allAuths.isEmpty());
		
		List<Authorization> goodAuths = authRepository.findAllBySecurityToken("fud");
		Assert.assertNotNull(goodAuths);
		Assert.assertTrue (1 == goodAuths.size());
		
	}
}
