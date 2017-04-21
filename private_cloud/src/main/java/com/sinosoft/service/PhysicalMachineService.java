package com.sinosoft.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.sinosoft.common.AlarmParameter;
import com.sinosoft.enumerator.PhysicalMachineTypeNameEnum;
import com.sinosoft.type.PhysicalMachine;
import com.sinosoft.type.VirtualMachine;

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
	public List<com.sinosoft.type.PhysicalMachine> getList(PhysicalMachineTypeNameEnum typeName);

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
