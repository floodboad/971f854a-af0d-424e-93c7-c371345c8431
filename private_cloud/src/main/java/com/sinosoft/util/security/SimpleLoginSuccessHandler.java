package com.sinosoft.util.security;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.sinosoft.model.User;

public class SimpleLoginSuccessHandler implements AuthenticationSuccessHandler, InitializingBean {
    private static Logger logger = LoggerFactory.getLogger(SimpleLoginSuccessHandler.class);
    
	@Resource
	Principal principal;

	private String defaultTargetUrl_User;
	private String defaultTargetUrl_Manager;

	private boolean forwardToDestination = false;
	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();


	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub

	}

	public String getDefaultTargetUrl_User() {
		return defaultTargetUrl_User;
	}

	public void setDefaultTargetUrl_User(String defaultTargetUrl_User) {
		this.defaultTargetUrl_User = defaultTargetUrl_User;
	}

	public String getDefaultTargetUrl_Manager() {
		return defaultTargetUrl_Manager;
	}

	public void setDefaultTargetUrl_Manager(String defaultTargetUrl_Manager) {
		this.defaultTargetUrl_Manager = defaultTargetUrl_Manager;
	}

	public void setForwardToDestination(boolean forwardToDestination) {
		this.forwardToDestination = forwardToDestination;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) {
		
		try {
			this.saveUserSessionId(request, authentication);
			String defaultTargetUrl = this.getUserTargetUrl(request, authentication);
			
			if (this.forwardToDestination) {
				request.getRequestDispatcher(defaultTargetUrl).forward(request, response);
			} else {
				this.redirectStrategy.sendRedirect(request, response, defaultTargetUrl);
			}
		} catch (Exception e) {
            logger.error("认证发生错误", e);
		}
		
		return;
	}

	/**
	 * 
	 * @param request
	 * @param authentication
	 * @author xuchen
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	private void saveUserSessionId(HttpServletRequest request, Authentication authentication) {
		String username = principal.getUsername();// 获取登录的用户名
		if (username.equals("admin")) {

		} else {
			String sessionid = request.getSession().getId();// 获取登录的SESSIONID
			User user = User.getUserByUsername(username);
			user.setSessionId(sessionid);
			user.merge();
			user.flush();
			user.clear();
		}
		
		return;
	}	

	/**
	 * 
	 * @param request
	 * @param authentication
	 * @return
	 * @author xuchen
	 */
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED, rollbackFor = { Exception.class })
	private String getUserTargetUrl(HttpServletRequest request, Authentication authentication) {
		String defaultTargetUrl = "";
		String role = principal.getRoleName();

		if (role.equals("ROLE_USER")) {
			defaultTargetUrl = this.defaultTargetUrl_User;
		} else {
			defaultTargetUrl = this.defaultTargetUrl_Manager;
		}
		
		return defaultTargetUrl;
	}
}