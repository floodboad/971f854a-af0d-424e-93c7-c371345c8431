package com.sinoparasoft.util.security;

import java.util.Iterator;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class Principal {
	public String getUsername(){
		
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String username;
		if (principal instanceof UserDetails) {
		   username = ((UserDetails)principal).getUsername();
		} else {
		   username = principal.toString();
		}
		return username;		
	}
	
	public String getRoleName(){
		Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (principal instanceof UserDetails) {
		   Iterator<? extends GrantedAuthority> it = ((UserDetails)principal).getAuthorities().iterator();
		   while(it.hasNext()){
			   GrantedAuthority g =  (GrantedAuthority)it.next();
			   return g.getAuthority();
		   }
		} else {
			
		}
		return null;		
	}
}
