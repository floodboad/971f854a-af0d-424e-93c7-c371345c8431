package com.sinoparasoft.util;

import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import com.sinoparasoft.common.ActionResult;
import com.sinoparasoft.common.ActionResultLogLevelEnum;
@Component
public class ActionResultHelper {
	/**
	 * create action result and log the message.
	 * 
	 * @param success
	 *            - success or not
	 * @param action
	 *            - action
	 * @param message
	 *            - message
	 * @param retry
	 *            - retry or not
	 * @param logger
	 *            - logger
	 * @param logLevel
	 *            - log level
	 * @return action result
	 * @author xiangqian
	 */
	public ActionResult createActionResult(boolean success, String action, String message, boolean retry, Logger logger,
			ActionResultLogLevelEnum logLevel) {
		ActionResult result = new ActionResult();
		result.setSuccess(success);
		result.setAction(action);
		result.setMessage(message);
		result.setRetry(retry);

		switch (logLevel) {
		case ERROR:
			logger.error(action + " - " + message);
			break;
		case WARN:
			logger.warn(action + " - " + message);
			break;
		default:
			break;
		}
		return result;
	}
}
