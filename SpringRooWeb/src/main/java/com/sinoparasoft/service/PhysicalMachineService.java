package com.sinoparasoft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinoparasoft.common.AlarmParameter;
import com.sinoparasoft.enumerator.PhysicalMachineTypeNameEnum;
import com.sinoparasoft.type.PhysicalMachine;
import com.sinoparasoft.type.VirtualMachine;

@Service
public interface PhysicalMachineService {
	/**
	 * get physical machine list by type.
	 * 
	 * @param typeName
	 *            - type name
	 * @return physical machine list
	 * @author xiangqian
	 */
	public List<com.sinoparasoft.type.PhysicalMachine> getList(PhysicalMachineTypeNameEnum typeName);

	/**
	 * get physical machine by id.
	 * 
	 * @param hostId
	 *            - host id
	 * @return physical machine
	 * @author xiangqian
	 */
	public PhysicalMachine getPhysicalMachine(String hostId);

	public void setAlarmSettings(String hostId, List<AlarmParameter> alarmParameters);

	/**
	 * get virtual machines on host by id.
	 * 
	 * @param hostId
	 *            - host id
	 * @return virtual machine list
	 * @author xiangqian
	 */
	public List<VirtualMachine> getVirtualMachines(String hostId);
}
