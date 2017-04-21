package com.sinosoft.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Aspect
public class DebugAspect {
	private static Logger logger = LoggerFactory.getLogger(DebugAspect.class);

//	@Before("execution(public * com.sinosoft.service.VirtualMachineServiceImpl.*(..))")
//	public void logBefore(JoinPoint point) {
//		logger.trace(point.getSignature().getDeclaringTypeName() + " - " + point.getSignature().getName() + " - "
//				+ Arrays.toString(point.getArgs()));
//	}
//
//	@AfterThrowing(pointcut="execution(public * com.sinosoft.service.VirtualMachineServiceImpl.*(..))", throwing="ex")
//	public void logAfterThrown(Throwable ex) {
//		logger.error("Catch Exception.", ex);
//	}
}
