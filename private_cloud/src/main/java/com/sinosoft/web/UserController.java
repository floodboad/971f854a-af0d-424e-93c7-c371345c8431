package com.sinosoft.web;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.sinosoft.enumerator.OperationSeverityEnum;
import com.sinosoft.enumerator.OperationStatusEnum;
import com.sinosoft.enumerator.RoleNameEnum;
import com.sinosoft.enumerator.ServiceNameEnum;
import com.sinosoft.enumerator.UserStatusEnum;
import com.sinosoft.model.Role;
import com.sinosoft.model.User;
import com.sinosoft.service.OperationLogService;
import com.sinosoft.service.UserService;
import com.sinosoft.util.security.Principal;

@RequestMapping("/user")
@Controller
public class UserController {
	private static Logger logger = LoggerFactory.getLogger(UserController.class);

	@Autowired
	OperationLogService operationLogService;

	@Resource
	Principal principal;

	@Autowired
	UserService userService;

	/**
	 * 
	 * @param map
	 * @param pageNo
	 * @return
	 */
	@RequestMapping(value = "list", method = RequestMethod.GET)
	public String getList() {
		return "user_list_tile";
	}

	/**
	 * 
	 * @param pageNo
	 * @param pageSize
	 * @param username
	 * @param realName
	 * @param roleName
	 * @param status
	 * @param department
	 * @param beginningCreateTime
	 * @param endCreateTime
	 * @return
	 * @author xiangqian
	 */
	@RequestMapping(value = "pagination-json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getPaginationJsonList(int pageNo, int pageSize, String username, String realName, String roleName,
			String status, String department, String beginningCreateTime, String endCreateTime) {
		Map<String, Object> map = new HashMap<String, Object>();

		if (pageSize <= 0) {
			pageSize = 10;
		}

		List<RoleNameEnum> userRoles = new ArrayList<RoleNameEnum>();
		if ((true == roleName.isEmpty()) || (true == roleName.equalsIgnoreCase(RoleNameEnum.ROLE_NONE.name()))) {
			String myRoleName = principal.getRoleName();
			if (myRoleName.equalsIgnoreCase(RoleNameEnum.ROLE_ADMIN.name())) {
				userRoles.add(RoleNameEnum.ROLE_ADMIN);
				userRoles.add(RoleNameEnum.ROLE_MANAGER);
			} else if (myRoleName.equalsIgnoreCase(RoleNameEnum.ROLE_MANAGER.name())) {
				userRoles.add(RoleNameEnum.ROLE_USER);
			}
		} else {
			userRoles.add(RoleNameEnum.valueOf(roleName));
		}

		try {
			Map<String, Object> userMap = userService.getUserEntries(username, realName, userRoles, status, department,
					beginningCreateTime, endCreateTime, pageNo, pageSize);
			map.putAll(userMap);

			map.put("status", "ok");
			map.put("message", "");
		} catch (Exception e) {
			String message = "获取用户列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(value = "json-list", method = RequestMethod.GET)
	@ResponseBody
	public Map<String, Object> getJsonList() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<User> userList = User.findAllUsers();
			map.put("status", "ok");
			map.put("message", "");
			map.put("userList", userList);
		} catch (Exception e) {
			String message = "获取用户列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 
	 * @param realusername
	 * @return
	 * @author xuchen
	 */
	@RequestMapping(method = RequestMethod.GET, value = "list-by-real-name", produces = "application/json")
	@ResponseBody
	public Map<String, Object> getListByRealName(String realName) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<User> userList = User.getEnableUsersByRealName(realName);
			map.put("status", "ok");
			map.put("message", "");
			map.put("userList", userList);
		} catch (Exception e) {
			String message = "获取用户角色发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 
	 * @param username
	 * @return
	 * @author xiangqian
	 */
	@RequestMapping(method = RequestMethod.GET, value = "online-cloud-manager-list", produces = "application/json")
	@ResponseBody
	public Map<String, Object> getOnlineCloudManagerList() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			List<User> onlineCloudManagers = User.getOnlineUsersByRole(RoleNameEnum.ROLE_MANAGER.name());
			map.put("status", "ok");
			map.put("message", "");
			map.put("onlineCloudManagers", onlineCloudManagers);
		} catch (Exception e) {
			String message = "获取在线的云平台管理员发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 
	 * @param password
	 * @return
	 */
	@RequestMapping(value = "password-validation", method = RequestMethod.GET, produces = "application/json")
	@ResponseBody
	public Map<String, Object> checkPassword(@RequestParam("password") String password) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			User user = User.getUserByUsername(principal.getUsername());
			Boolean match = ((null != user) && (true == user.getPassword().equals(password)));
			map.put("status", "ok");
			map.put("message", "");
			map.put("match", match);
		} catch (Exception e) {
			String message = "匹配用户密码发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 用户修改个人密码
	 * 
	 * @param password
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "password", produces = "application/json")
	@ResponseBody
	public Map<String, Object> changePassword(String password) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String username = principal.getUsername();
			User user = User.getUserByUsername(username);
			if (null != user) {
				user.setPassword(password);
				// clear session so the user must re-login
				user.setSessionId("");
				user.merge();
				user.flush();
				user.clear();

				// save operation log
				String operator = username;
				Date operationTime = new Date();
				String operation = "修改密码";
				OperationStatusEnum operationStatus = OperationStatusEnum.OK;
				ServiceNameEnum serviceName = ServiceNameEnum.USER_MANAGEMENT;
				String objectId = username;
				String operationResult = "修改密码成功，用户名：" + username;
				OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId, operationResult,
						operationSeverity);

				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", "指定的用户不存在");
			}
		} catch (Exception e) {
			String message = "修改用户密码发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "logout", produces = "application/json")
	@ResponseBody
	public Map<String, Object> logout() {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			String username = principal.getUsername();
			if (username.equals("admin")) {
				map.put("status", "ok");
				map.put("message", "");
			} else {
				User user = User.getUserByUsername(username);
				if (null != user) {
					user.setSessionId("");
					user.merge();
					user.flush();
					user.clear();

					map.put("status", "ok");
					map.put("message", "");
				} else {
					map.put("status", "error");
					map.put("message", "指定的用户不存在");
				}
			}
		} catch (Exception e) {
			String message = "注销用户发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	private String getMd5String(String message) throws NoSuchAlgorithmException {
		MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(message.getBytes());

		byte byteData[] = md.digest();

		// convert the byte to hex format method 1
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < byteData.length; i++) {
			sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
		}

		return sb.toString();

		// // convert the byte to hex format method 2
		// StringBuffer hexString = new StringBuffer();
		// for (int i = 0; i < byteData.length; i++) {
		// String hex = Integer.toHexString(0xff & byteData[i]);
		// if (hex.length() == 1)
		// hexString.append('0');
		// hexString.append(hex);
		// }
		// return hexString.toString();
	}

	/**
	 * 
	 * @param username
	 * @param password
	 * @param roleName
	 * @param realName
	 * @param department
	 * @param phone
	 * @param email
	 * @param snapshotQuota
	 * @return
	 */
	@RequestMapping(method = RequestMethod.POST, value = "", produces = "application/json")
	@ResponseBody
	public Object addUser(String username, String roleName, String realName, String department, String phone,
			String email, int snapshotQuota) {
		Map<String, Object> map = new HashMap<String, Object>();
		final String password = "12345";

		try {
			User u = User.getUserByUsername(username);
			if (null == u) {
				User user = new User();
				user.setUsername(username);
				String md5Password = getMd5String(password);
				user.setPassword(md5Password);
				Role role = Role.getRole(roleName);
				user.setRole(role);
				user.setRealName(realName);
				user.setDepartment(department);
				user.setPhone(phone);
				user.setEmail(email);
				user.setSnapshotQuota(snapshotQuota);
				user.setStatus(UserStatusEnum.DISABLED);
				user.setCreateTime(new Date());
				user.setDisableTime(new Date());

				user.persist();
				user.flush();
				user.clear();

				// save operation log
				String operator = principal.getUsername();
				Date operationTime = new Date();
				String operation = "新建用户";
				OperationStatusEnum operationStatus = OperationStatusEnum.OK;
				ServiceNameEnum serviceName = ServiceNameEnum.USER_MANAGEMENT;
				String objectId = username;
				String operationResult = "新建用户成功，用户名：" + username + "，角色：" + role.getDescription() + "，快照数量："
						+ snapshotQuota + "，姓名：" + realName + "，电话：" + phone + "，邮箱：" + email + "，部门：" + department;
				OperationSeverityEnum operationSeverity = OperationSeverityEnum.LOW;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId, operationResult,
						operationSeverity);

				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", "指定的用户已存在");
			}
		} catch (Exception e) {
			String message = "新建用发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	/**
	 * 
	 * @param username
	 * @param roleName
	 * @param realName
	 * @param department
	 * @param phone
	 * @param email
	 * @param snapshotQuota
	 * @return
	 */
	@RequestMapping(method = RequestMethod.PUT, value = "{username}", produces = "application/json")
	@ResponseBody
	public Map<String, Object> changeUser(@PathVariable("username") String username, String roleName, String realName,
			String department, String phone, String email, int snapshotQuota) {
		Map<String, Object> map = new HashMap<String, Object>();

		try {
			User user = User.getUserByUsername(username);
			if (null != user) {
				Role role = Role.getRole(roleName);
				user.setRole(role);
				user.setRealName(realName);
				user.setDepartment(department);
				user.setPhone(phone);
				user.setEmail(email);
				user.setSnapshotQuota(snapshotQuota);
				user.merge();
				user.flush();
				user.clear();

				// save operation log
				String operator = principal.getUsername();
				Date operationTime = new Date();
				String operation = "修改用户信息";
				OperationStatusEnum operationStatus = OperationStatusEnum.OK;
				ServiceNameEnum serviceName = ServiceNameEnum.USER_MANAGEMENT;
				String objectId = username;
				String operationResult = "修改用户信息成功，用户名：" + username + "，角色：" + role.getDescription() + "，快照数量："
						+ snapshotQuota + "，姓名：" + realName + "，电话：" + phone + "，邮箱：" + email + "，部门：" + department;
				OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
				operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId, operationResult,
						operationSeverity);

				map.put("status", "ok");
				map.put("message", "");
			} else {
				map.put("status", "error");
				map.put("message", "指定的用户不存在");
			}
		} catch (Exception e) {
			String message = "修改用发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "status", produces = "application/json")
	@ResponseBody
	public Map<String, Object> changeUserStatus(String usernames, String status) {
		Map<String, Object> map = new HashMap<String, Object>();

		UserStatusEnum userSatus = null;
		try {
			userSatus = UserStatusEnum.valueOf(status);
		} catch (Exception e1) {
			String message = "无效的用户状态";
			map.put("status", "error");
			map.put("message", message);

			logger.error(message, e1);
			return map;
		}

		boolean result = true;
		String message = "";
		String[] usernameArray = usernames.split(",");
		for (int i = 0; i < usernameArray.length; i++) {
			String username = usernameArray[i].trim();
			if (username.length() == 0) {
				continue;
			}

			try {
				User user = User.getUserByUsername(username);
				if (null != user) {
					String operation = null;
					String operationResult = null;
					OperationSeverityEnum operationSeverity = null;
					
					switch (userSatus) {
					case ENABLED:
						user.setStatus(UserStatusEnum.ENABLED);
						user.setDeleteTime(null);
						user.setEnableTime(new Date());
						user.setDisableTime(null);

						operation = "启用用户";
						operationResult = "启用用户成功，用户名：" + username;
						operationSeverity = OperationSeverityEnum.MIDDLE;

						break;
					case DISABLED:
						user.setStatus(UserStatusEnum.DISABLED);
						user.setDeleteTime(null);
						user.setEnableTime(null);
						user.setDisableTime(new Date());

						operation = "停用用户";
						operationResult = "停用用户成功，用户名：" + username;
						operationSeverity = OperationSeverityEnum.MIDDLE;

						break;
					case DELETED:
						user.setStatus(UserStatusEnum.DELETED);
						user.setDeleteTime(new Date());
						user.setEnableTime(null);
						user.setDisableTime(null);

						operation = "删除用户";
						operationResult = "删除用户成功，用户名：" + username;
						operationSeverity = OperationSeverityEnum.HIGH;

						break;
					default:
						break;
					}

					user.merge();
					user.flush();
					user.clear();

					// save operation log
					String operator = principal.getUsername();
					Date operationTime = new Date();
					OperationStatusEnum operationStatus = OperationStatusEnum.OK;
					ServiceNameEnum serviceName = ServiceNameEnum.USER_MANAGEMENT;
					String objectId = username;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId, operationResult,
							operationSeverity);
				} else {
					result = false;
					message += "用户" + username + "不存在；";
				}
			} catch (Exception e) {
				String errorMessage = "设置" + username + "的用户状态发生错误；";
				result = false;
				message += "设置" + username + "的用户状态发生错误；";

				logger.error(errorMessage, e);
			}
		}

		if (true == result) {
			map.put("status", "ok");
			map.put("message", "");
		} else {
			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(method = RequestMethod.PUT, value = "user-password", produces = "application/json")
	@ResponseBody
	public Map<String, Object> changeUserPassword(String usernames, String password) {
		Map<String, Object> map = new HashMap<String, Object>();

		boolean result = true;
		String message = "";
		String[] usernameArray = usernames.split(",");
		for (int i = 0; i < usernameArray.length; i++) {
			String username = usernameArray[i].trim();
			if (username.length() == 0) {
				continue;
			}

			try {
				User user = User.getUserByUsername(username);
				if (null != user) {
					user.setPassword(password);
					user.merge();
					user.flush();
					user.clear();

					// save operation log
					String operator = principal.getUsername();
					Date operationTime = new Date();
					String operation = "修改密码";
					OperationStatusEnum operationStatus = OperationStatusEnum.OK;
					ServiceNameEnum serviceName = ServiceNameEnum.USER_MANAGEMENT;
					String objectId = username;
					String operationResult = "修改密码成功，用户名：" + username;
					OperationSeverityEnum operationSeverity = OperationSeverityEnum.MIDDLE;
					operationLogService.saveLog(operator, operationTime, operation, operationStatus, serviceName, objectId, operationResult,
							operationSeverity);
				} else {
					result = false;
					message += "用户" + username + "不存在；";
				}
			} catch (Exception e) {

				String errorMessage = "设置" + username + "的用户密码发生错误；";
				result = false;
				message += "设置" + username + "的用户状态发生错误；";

				logger.error(errorMessage, e);
			}
		}

		if (true == result) {
			map.put("status", "ok");
			map.put("message", "");
		} else {
			map.put("status", "error");
			map.put("message", message);
		}

		return map;
	}

	@RequestMapping(value = "virtual-machine-manager-list", method = RequestMethod.GET, produces = "application/json")
	public @ResponseBody Map<String, Object> getVirtualMachineManagerList() {
		Map<String, Object> map = new HashMap<String, Object>();
		
		List<User> managerList;
		try {
			managerList = User.getEnableUsersByRole("ROLE_USER");
			map.put("status", "ok");
			map.put("message", "");
			map.put("users", managerList);
		} catch (Exception e) {
			String message = "获取用户列表发生错误";
			logger.error(message, e);

			map.put("status", "error");
			map.put("message", message);
		}
		
		return map;
	}
}
