package com.sinoparasoft.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.sinoparasoft.config.ApplicationConfiguration;
import com.sinoparasoft.model.User;

public class SessionIdListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
		System.out.println("Session Created!");				
		return;
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent arg0) {
		HttpSession session = arg0.getSession();
		User user = User.getUserBySessionId(session.getId());
		if (null != user) {
			user.setSessionId("");
			user.merge();
			user.flush();
			user.clear();
		}
	}

}