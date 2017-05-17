package com.sinoparasoft.interceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import com.sinoparasoft.enumerator.UserStatusEnum;
import com.sinoparasoft.model.User;
import com.sinoparasoft.util.security.Principal;

public class RequestInterceptor extends HandlerInterceptorAdapter {
	private static Logger logger = LoggerFactory.getLogger(RequestInterceptor.class);

	@Resource
	Principal principal;

	public String[] allowUrls;// 还没发现可以直接配置不拦截的资源，所以在代码里面来排除
	private String loginUrl;
	private String userTargetUrl;
	private String managerTargetUrl;
	private int sessionTimedoutCode;

	public void setAllowUrls(String[] allowUrls) {
		this.allowUrls = allowUrls;
	}

	public String getLoginUrl() {
		return loginUrl;
	}

	public void setLoginUrl(String loginUrl) {
		this.loginUrl = loginUrl;
	}

	public String getUserTargetUrl() {
		return userTargetUrl;
	}

	public void setUserTargetUrl(String userTargetUrl) {
		this.userTargetUrl = userTargetUrl;
	}

	public String getManagerTargetUrl() {
		return managerTargetUrl;
	}

	public void setManagerTargetUrl(String managerTargetUrl) {
		this.managerTargetUrl = managerTargetUrl;
	}

	public int getSessionTimedoutCode() {
		return sessionTimedoutCode;
	}

	public void setSessionTimedoutCode(int sessionTimedoutCode) {
		this.sessionTimedoutCode = sessionTimedoutCode;
	}

	/**
	 * Called before the handler execution, returns a boolean value, “true” : continue the handler execution chain;
	 * “false”, stop the execution chain and return it.
	 */
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
		try {
			/*
			 * after session timeout, when user visits any url, the requestPath will redirect to "/login" automatically.
			 */

			request.setCharacterEncoding("UTF8");

			String requestPath = request.getServletPath();
			String queryString = request.getQueryString();

			/*
			 * allow to visit public url.
			 */
			for (String url : allowUrls) {
				if (requestPath.contains(url)) {
					return true;
				}
			}

			/*
			 * get session, do not create a new one, so the return value will be null if it's the first time to visit,
			 * or session.invalidate() is called.
			 */
			HttpSession session = request.getSession(false);

			/*
			 * the first time to visit, or session.invalidate().
			 */
			if (null == session) {
				/*
				 * show login page.
				 */
				if (requestPath.contains(loginUrl)) {
					return true;
				}

				/*
				 * redirect to login page.
				 */
				response.sendRedirect(request.getContextPath() + loginUrl);
				return false;
			}

			// logger.info("### isNew ### " + session.isNew());

			String sessionid = session.getId();
			String username = principal.getUsername();

			/*
			 * allow built-in admin user to visit anywhere.
			 */
			if (username.equals("admin")) {
				return true;
			}

			/*
			 * visit login page.
			 */
			if (requestPath.contains(loginUrl)) {
				/*
				 * authentication failed.
				 */
				if ((null != queryString) && (queryString.equalsIgnoreCase("login_error=t"))) {
					// session.invalidate();
					return true;
				}

				User user = User.getUserByUsername(username);
				if (null != user) {
					/*
					 * redirect to default page.
					 */
					String role = principal.getRoleName();

					if (role.equals("ROLE_USER")) {
						response.sendRedirect(request.getContextPath() + userTargetUrl);
					} else {
						response.sendRedirect(request.getContextPath() + managerTargetUrl);
					}

					return false;
				} else {
					/*
					 * show login page for non-exist user, SESSION TIMEOUT logic is here, username is now anonymousUser.
					 */

					String requestedWith = request.getHeader("X-Requested-With");
					if ((null != requestedWith) && (true == requestedWith.equalsIgnoreCase("XMLHttpRequest"))) {
						/*
						 * https://doanduyhai.wordpress.com/2012/04/21/spring-security-part-vi-session-timeout-handling-for
						 * -ajax-calls/. However, at the XMLHttpRequest level, it is not possible to detect this
						 * redirection. According to the W3C specs, the redirection is taken care at browser level and
						 * should be transparent for the user (so transparent for the XMLHttpRequest protocol too). What
						 * happens it that the Ajax layer receives an HTTP 200 code after the redirection.
						 */
						response.sendError(sessionTimedoutCode);
						return false;
					} else {
						return true;
					}
				}
			}

			// re-login: disabled, deleted, kicked off, or logout
			User user = User.getUserByUsername(username);
			/*
			 * show login page for non-exist user.
			 */
			if (null == user) {
				session.invalidate();
				response.sendRedirect(request.getContextPath() + loginUrl);
				return false;
			}

			/*
			 * show login page for disabled or deleted user.
			 */
			if (UserStatusEnum.ENABLED != user.getStatus()) {
				session.invalidate();
				response.sendRedirect(request.getContextPath() + loginUrl);
				return false;
			}

			/*
			 * show login page if kick-off by other login
			 */
			if (false == user.getSessionId().equals(sessionid)) {
				session.invalidate();
				response.sendRedirect(request.getContextPath() + loginUrl);
				return false;
			}

			return true;
		} catch (Exception e) {
			logger.error("预处理请求发生错误", e);

			return false;
		}
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
	}
}
