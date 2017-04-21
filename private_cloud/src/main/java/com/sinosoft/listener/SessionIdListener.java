package com.sinosoft.listener;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import com.sinosoft.model.User;

public class SessionIdListener implements HttpSessionListener {

	@Override
	public void sessionCreated(HttpSessionEvent arg0) {
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
