package com.innoventsolutions.brr.client;

import java.io.File;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@SpringBootApplication
@ComponentScan("com.innoventsolutions.brr.client")
public class ClientSpringBootApp {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ClientSpringBootApp.class, args);
	}

	@PostConstruct
	private void initDb() {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateString = df.format(new Date());
		// get full path to report design file (which is in a different package)
		final URL placeholderFileURL = this.getClass().getResource("placeholder.txt");
		final File file = new File(new File(placeholderFileURL.getPath()).getParentFile().getParentFile(), "test.rptdesign");
		System.out.println("****** Inserting test data in the table: Authorization ******");
		String sqlStatements[] = { "truncate table brrs.authorization",
				"insert into brrs.authorization (id, security_token, design_file, submit_time) values(1, 'test-token-report', '" + file.getAbsolutePath() + "', '"
						+ dateString + "')",
				"insert into brrs.authorization (id, security_token, design_file, submit_time) values(2, 'test-token-noreport', null, '" + dateString + "')" };

		Arrays.asList(sqlStatements).stream().forEach(sql -> {
			System.out.println(sql);
			jdbcTemplate.execute(sql);
		});
		System.out.println("****** Fetching from table: Authorization ******");
		jdbcTemplate.query("select * from authorization", new RowMapper<Object>() {
			@Override
			public Object mapRow(ResultSet rs, int i) throws SQLException {
				System.out.println(rs.toString());
				/*
				 * System.out.println(String.format("id:%s,token:%s,rpt:%s,time:%s",
				 * rs.getString("id").toString(), rs.getString("securityToken"),
				 * rs.getString("designFile"), rs.getString("submitTime")));
				 */
				return null;
			}
		});
	}
}
