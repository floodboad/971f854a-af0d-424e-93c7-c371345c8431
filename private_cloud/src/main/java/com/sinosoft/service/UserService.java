package com.sinosoft.service;

import java.text.ParseException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.RoleNameEnum;

@Service
public interface UserService {
	public Map<String, Object> getUserEntries(String username, String realName, List<RoleNameEnum> userRoles,
			String status, String department, String beginningCreateTime, String endCreateTime, int pageNo, int pageSize)
			throws ParseException;
}
