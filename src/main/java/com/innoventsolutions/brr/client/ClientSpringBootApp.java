package com.innoventsolutions.brr.client;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
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
		System.out.println("****** Inserting more sample data in the table: Employees ******");
		String sqlStatements[] = {
				"insert into brrs.authorization values(1, 'test.rptdesign', 'test-token',  '2020-01-15 00:00:00')",
				"insert into brrs.authorization values(2, 'test2.rptdesign', 'test-token2', '2020-01-15 00:00:00')",
				"insert into brrs.authorization values(3, 'test2.rptdesign', 'test-token3', '2020-01-15 00:00:00')" };

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
				System.out.println(String.format("id:%s,token:%s,rpt:%s,time:%s",
				    rs.getString("id").toString(),
				    rs.getString("securityToken"),
				    rs.getString("designFile"),
				    rs.getString("submitTime")));
				*/
				return null;
			}
		});
	}
}
