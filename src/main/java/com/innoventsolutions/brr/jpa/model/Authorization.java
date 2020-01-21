package com.innoventsolutions.brr.jpa.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity	@Getter @Setter
@Table(name = "Authorization")
public class Authorization {
	@Id
	private Integer Id;
	private String securityToken;
	private String designFile;
	private Timestamp submitTime;
	
}
