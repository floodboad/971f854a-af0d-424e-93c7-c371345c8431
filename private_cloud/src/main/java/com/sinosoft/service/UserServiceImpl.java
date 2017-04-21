package com.sinosoft.service;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.sinosoft.enumerator.RoleNameEnum;
import com.sinosoft.model.User;

@Service
public class UserServiceImpl implements UserService {
	/**
	 * 
	 * @param username
	 * @param realName
	 * @param userRoles
	 * @param status
	 * @param department
	 * @param beginningCreateTime
	 * @param endCreateTime
	 * @param pageNo
	 * @param pageSize
	 * @return
	 * @author xiangqian
	 * @throws ParseException
	 */
	public Map<String, Object> getUserEntries(String username, String realName, List<RoleNameEnum> userRoles,
			String status, String department, String beginningCreateTime, String endCreateTime, int pageNo, int pageSize)
			throws ParseException {
		Map<String, Object> map = new HashMap<String, Object>();
		
		if (pageNo <= 0) {
			pageNo = 1;
		}

		int userCount = 0;
		int recordCount = (int) User.countUserEntries(username, realName, userRoles, status, department,
				beginningCreateTime, endCreateTime);
		userCount = recordCount;
		recordCount -= 1;
		if (recordCount < 0) {
			recordCount = 0;
		}
		int pageTotal = recordCount / pageSize + 1;

		List<User> userList = User.getUserEntries(username, realName, userRoles, status, department,
				beginningCreateTime, endCreateTime, (pageNo - 1) * pageSize, pageSize, "createTime", "DESC");

		map.put("user_count", userCount);
		map.put("page_no", pageNo);
		map.put("page_count", pageTotal);
		map.put("users", userList);

		return map;
	}
}
