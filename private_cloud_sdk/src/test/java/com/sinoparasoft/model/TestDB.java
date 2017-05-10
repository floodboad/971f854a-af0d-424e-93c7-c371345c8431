package com.sinoparasoft.model;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.io.ClassPathResource;

import com.sinoparasoft.config.TestStaticApplicationConfiguration;
import com.sinoparasoft.enumerator.ExternalIpStatusEnum;

public class TestDB {
	public static void main(String[] args) {
				
		TestStaticApplicationConfiguration configuration=new TestStaticApplicationConfiguration();
		
		//Query				
		String username="admin001";
		Long id=2L;
		String jpaString = "SELECT a FROM ExternalIp a ";
		TypedQuery<ExternalIp> jpaQuery = ExternalIp.entityManager().createQuery(jpaString, ExternalIp.class);
		List<ExternalIp> ips = jpaQuery.getResultList();
		System.out.println(ips.size());
		for (ExternalIp externalIp : ips) {
			System.out.println(externalIp.getIp());
		}
		
		//Insert
		ExternalIp ip=new ExternalIp();
		ip.setIp("0.0.0.0");
		
//		ip.setStatus(ExternalIpStatusEnum.UNKNOWN);
//		System.out.println(ip.toString());
//		ip.persist();
//		ip.flush();
//		ip.clear();
		
		
		jpaString="SELECT a FROM ExternalIp a WHERE a.ip = '0.0.0.0'";
		jpaQuery=ExternalIp.entityManager().createQuery(jpaString, ExternalIp.class);
		List<ExternalIp> curr_ips = jpaQuery.getResultList();
		if (curr_ips.size()==1) {
			//Update
			ip=curr_ips.get(0);
			ip.setStatus(ExternalIpStatusEnum.AVAILABLE);
			ip.merge();
			ip.flush();
			ip.clear();
			
			//Delete
			System.out.println(curr_ips.get(0).toString());
			ip=curr_ips.get(0);
			
//			if (em.contains(ip)) {
//				em.remove(ip);
//			}else{
//				System.out.println("Delete Error!");
//			}
		}		
		
		
	}
}
