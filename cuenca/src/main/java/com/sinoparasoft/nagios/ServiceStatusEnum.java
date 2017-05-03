package com.sinoparasoft.nagios;

import java.util.HashMap;
import java.util.Map;

/**
 * service status
 * 
 * http://stackoverflow.com/questions/11047756/getting-enum-associated-with-int-value
 * 
 * @author xiangqian
 *
 */
public enum ServiceStatusEnum {
	PENDING(1), OK(2), WARNING(4), UNKNOWN(8), CRITICAL(16);

	private int statusCode;
	private static Map<Integer, ServiceStatusEnum> map = new HashMap<Integer, ServiceStatusEnum>();

	/**
	 * http://docs.oracle.com/javase/tutorial/java/javaOO/enum.html
	 * The constructor for an enum type must be package-private or private access. It automatically creates the
	 * constants that are defined at the beginning of the enum body. You cannot invoke an enum constructor yourself.
	 * 
	 * @param statusCode
	 */
	private ServiceStatusEnum(final int statusCode) {
		this.statusCode = statusCode;
	}

	/**
	 * invoke once to initialize map
	 */
	static {
		for (ServiceStatusEnum status : ServiceStatusEnum.values()) {
			map.put(status.statusCode, status);
		}
	}

	/**
	 * assignment with int value
	 * 
	 * @param statusCode
	 * @return
	 */
	public static ServiceStatusEnum valueOf(int statusCode) {
		return map.get(statusCode);
	}
}
