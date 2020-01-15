package com.innoventsolutions.brr.jpa.dao;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.innoventsolutions.brr.jpa.model.Authorization;

public interface AuthorizationRepository extends CrudRepository<Authorization, Integer> {
    List<Authorization> findAllBySecurityToken(String securityToken);

	
}