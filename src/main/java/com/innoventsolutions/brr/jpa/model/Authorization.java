package com.innoventsolutions.brr.jpa.model;

import java.sql.Timestamp;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity	
@Table(name = "Authorization")
public class Authorization {
	@Id
	private Integer Id;
	private String securityToken;
	private String designFile;
	private Timestamp submitTime;
	
	public String getDesignFile() {
		return designFile;
	}
	public void setDesignFile(String designFile) {
		this.designFile = designFile;
	}
	public Timestamp getSubmitTime() {
		return submitTime;
	}
	public void setSubmitTime(Timestamp submitTime) {
		this.submitTime = submitTime;
	}
	public String getSecurityToken() {
		return securityToken;
	}
	public void setSecurityToken(String securityToken) {
		this.securityToken = securityToken;
	}
	public Integer getId() {
		return Id;
	}
	public void setId(Integer id) {
		Id = id;
	}
	

}
