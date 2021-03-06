// WARNING: DO NOT EDIT THIS FILE. THIS FILE IS MANAGED BY SPRING ROO.
// You may push code into the target .java compilation unit if you wish to edit any member(s).

package com.sinosoft.model;

import com.sinosoft.enumerator.UserStatusEnum;
import com.sinosoft.model.RoleDataOnDemand;
import com.sinosoft.model.User;
import com.sinosoft.model.UserDataOnDemand;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

privileged aspect UserDataOnDemand_Roo_DataOnDemand {
    
    declare @type: UserDataOnDemand: @Component;
    
    private Random UserDataOnDemand.rnd = new SecureRandom();
    
    private List<User> UserDataOnDemand.data;
    
    @Autowired
    RoleDataOnDemand UserDataOnDemand.roleDataOnDemand;
    
    public User UserDataOnDemand.getNewTransientUser(int index) {
        User obj = new User();
        setCreateTime(obj, index);
        setDeleteTime(obj, index);
        setDepartment(obj, index);
        setDisableTime(obj, index);
        setEmail(obj, index);
        setEnableTime(obj, index);
        setPassword(obj, index);
        setPhone(obj, index);
        setRealName(obj, index);
        setSessionId(obj, index);
        setSnapshotQuota(obj, index);
        setStatus(obj, index);
        setUsername(obj, index);
        return obj;
    }
    
    public void UserDataOnDemand.setCreateTime(User obj, int index) {
        Date createTime = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setCreateTime(createTime);
    }
    
    public void UserDataOnDemand.setDeleteTime(User obj, int index) {
        Date deleteTime = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setDeleteTime(deleteTime);
    }
    
    public void UserDataOnDemand.setDepartment(User obj, int index) {
        String department = "department_" + index;
        obj.setDepartment(department);
    }
    
    public void UserDataOnDemand.setDisableTime(User obj, int index) {
        Date disableTime = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setDisableTime(disableTime);
    }
    
    public void UserDataOnDemand.setEmail(User obj, int index) {
        String email = "foo" + index + "@bar.com";
        obj.setEmail(email);
    }
    
    public void UserDataOnDemand.setEnableTime(User obj, int index) {
        Date enableTime = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR), Calendar.getInstance().get(Calendar.MONTH), Calendar.getInstance().get(Calendar.DAY_OF_MONTH), Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND) + new Double(Math.random() * 1000).intValue()).getTime();
        obj.setEnableTime(enableTime);
    }
    
    public void UserDataOnDemand.setPassword(User obj, int index) {
        String password = "password_" + index;
        obj.setPassword(password);
    }
    
    public void UserDataOnDemand.setPhone(User obj, int index) {
        String phone = "phone_" + index;
        obj.setPhone(phone);
    }
    
    public void UserDataOnDemand.setRealName(User obj, int index) {
        String realName = "realName_" + index;
        obj.setRealName(realName);
    }
    
    public void UserDataOnDemand.setSessionId(User obj, int index) {
        String sessionId = "sessionId_" + index;
        obj.setSessionId(sessionId);
    }
    
    public void UserDataOnDemand.setSnapshotQuota(User obj, int index) {
        Integer snapshotQuota = new Integer(index);
        obj.setSnapshotQuota(snapshotQuota);
    }
    
    public void UserDataOnDemand.setStatus(User obj, int index) {
        UserStatusEnum status = UserStatusEnum.class.getEnumConstants()[0];
        obj.setStatus(status);
    }
    
    public void UserDataOnDemand.setUsername(User obj, int index) {
        String username = "username_" + index;
        obj.setUsername(username);
    }
    
    public User UserDataOnDemand.getSpecificUser(int index) {
        init();
        if (index < 0) {
            index = 0;
        }
        if (index > (data.size() - 1)) {
            index = data.size() - 1;
        }
        User obj = data.get(index);
        Long id = obj.getUserId();
        return User.findUser(id);
    }
    
    public User UserDataOnDemand.getRandomUser() {
        init();
        User obj = data.get(rnd.nextInt(data.size()));
        Long id = obj.getUserId();
        return User.findUser(id);
    }
    
    public boolean UserDataOnDemand.modifyUser(User obj) {
        return false;
    }
    
    public void UserDataOnDemand.init() {
        int from = 0;
        int to = 10;
        data = User.findUserEntries(from, to);
        if (data == null) {
            throw new IllegalStateException("Find entries implementation for 'User' illegally returned null");
        }
        if (!data.isEmpty()) {
            return;
        }
        
        data = new ArrayList<User>();
        for (int i = 0; i < 10; i++) {
            User obj = getNewTransientUser(i);
            try {
                obj.persist();
            } catch (final ConstraintViolationException e) {
                final StringBuilder msg = new StringBuilder();
                for (Iterator<ConstraintViolation<?>> iter = e.getConstraintViolations().iterator(); iter.hasNext();) {
                    final ConstraintViolation<?> cv = iter.next();
                    msg.append("[").append(cv.getRootBean().getClass().getName()).append(".").append(cv.getPropertyPath()).append(": ").append(cv.getMessage()).append(" (invalid value = ").append(cv.getInvalidValue()).append(")").append("]");
                }
                throw new IllegalStateException(msg.toString(), e);
            }
            obj.flush();
            data.add(obj);
        }
    }
    
}
