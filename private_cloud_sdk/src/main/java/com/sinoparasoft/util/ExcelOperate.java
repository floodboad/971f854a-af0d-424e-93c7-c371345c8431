package com.sinoparasoft.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import com.sinoparasoft.type.MonitorLog;
import com.sinoparasoft.type.OperationLog;

/**
 * 创建excel
 * 
 * @author xc
 *
 */

public class ExcelOperate {
	public boolean createOperationLogsExcel(String filePath, List<OperationLog> operationLogList)
			throws IOException {
		File file = new File(filePath);
		if (file.getParentFile().exists() == false) {
			if (file.getParentFile().mkdirs() == false) {
				return false;
			}
		}

		// 创建webbook
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 在webbook中添加一个sheet
		HSSFSheet sheet = workbook.createSheet("操作日志");
		// 在sheet中添加表头第0行
		HSSFRow row = sheet.createRow((int) 0);
		// 创建单元格，并设置值表头 设置表头居中
		HSSFCellStyle style = workbook.createCellStyle();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER); // 创建一个居中格式

		HSSFCell cell = row.createCell(0);
		cell.setCellValue("ID");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("服务名称");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("操作者");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("所在部门");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("操作时间");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("操作");
		cell.setCellStyle(style);
		cell = row.createCell(6);
		cell.setCellValue("操作对象");
		cell.setCellStyle(style);
		cell = row.createCell(7);
		cell.setCellValue("操作状态");
		cell.setCellStyle(style);
		cell = row.createCell(8);
		cell.setCellValue("操作结果");
		cell.setCellStyle(style);
		cell = row.createCell(9);
		cell.setCellValue("重要性");
		cell.setCellStyle(style);

		for (int i = 0; i < operationLogList.size(); i++) {
			row = sheet.createRow((int) i + 1);

			// 第四步，创建单元格，并设置值
			OperationLog operationLog = operationLogList.get(i);
			row.createCell(0).setCellValue(operationLog.getId());
			row.createCell(1).setCellValue(operationLog.getServiceName());
			row.createCell(2).setCellValue(operationLog.getOperator());
			row.createCell(3).setCellValue(operationLog.getOperatorDepartment());
			row.createCell(4).setCellValue(operationLog.getOperationTime());
			row.createCell(5).setCellValue(operationLog.getOperation());
			String objectDescription = operationLog.getObjectName();
			if (true == operationLog.getServiceName().equalsIgnoreCase("虚拟机管理")) {
				objectDescription += "（所在域：" + operationLog.getVirtualMachineDomainName() + "，所在组：" + operationLog.getVirtualMachineGroupName() + "）";
			}
			row.createCell(6).setCellValue(objectDescription);
			row.createCell(7).setCellValue(operationLog.getOperationStatus());
			row.createCell(8).setCellValue(operationLog.getOperationResult());
			row.createCell(9).setCellValue(operationLog.getSeverityLevel());
			
			
//			if (operationLogList.get(i).get("operatorDepartment") != null) {
//				row.createCell(3).setCellValue(operationLogList.get(i).get("operatorDepartment").toString());
//			}
//			row.createCell(4).setCellValue(operationLogList.get(i).get("operationTime").toString());
//			if (operationLogList.get(i).get("object") != null) {
//				String object = operationLogList.get(i).get("object").toString();
//				if (serviceName == "虚拟机管理") {
//					object += "（域：";
//					if (operationLogList.get(i).get("virtualMachineDomain") != null) {
//						object += operationLogList.get(i).get("virtualMachineDomain").toString();
//					} else {
//						object += "空";
//					}
//					object += "，组：";
//					if (operationLogList.get(i).get("virtualMachineGroup") != null) {
//						object += operationLogList.get(i).get("virtualMachineGroup").toString();
//					} else {
//						object += "空";
//					}
//					object += "）";
//				}
//				row.createCell(5).setCellValue(object);
//			}

		}
		// 第六步，将文件存到指定位置

		FileOutputStream fout = new FileOutputStream(filePath);
		workbook.write(fout);
		fout.close();
		workbook.close();

		return true;
	}

	/**
	 * create excel file.
	 * 
	 * @param filePath
	 *            - output file path
	 * @param monitorLogs
	 *            - monitor logs
	 * @return true if created successfully, return false if otherwise.
	 * @throws IOException
	 * @author xuchen, xiangqian
	 */
	public boolean createMonitorLogExcel(String filePath, List<com.sinoparasoft.type.MonitorLog> monitorLogs)
			throws IOException {
		File file = new File(filePath);
		if (file.getParentFile().exists() == false) {
			if (file.getParentFile().mkdirs() == false) {
				return false;
			}
		}

		// 创建webbook
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 在webbook中添加一个sheet
		HSSFSheet sheet = workbook.createSheet("监控日志");
		// 在sheet中添加表头第0行
		HSSFRow row = sheet.createRow((int) 0);
		// 创建单元格，并设置值表头 设置表头居中
		HSSFCellStyle style = workbook.createCellStyle();
		// 创建一个居中格式
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

		HSSFCell cell = row.createCell(0);
		cell.setCellValue("监控ID");
		cell.setCellStyle(style);
		cell = row.createCell(1);
		cell.setCellValue("对象类别");
		cell.setCellStyle(style);
		cell = row.createCell(2);
		cell.setCellValue("对象名称");
		cell.setCellStyle(style);
		cell = row.createCell(3);
		cell.setCellValue("监控类别");
		cell.setCellStyle(style);
		cell = row.createCell(4);
		cell.setCellValue("监控名称");
		cell.setCellStyle(style);
		cell = row.createCell(5);
		cell.setCellValue("监控状态");
		cell.setCellStyle(style);
		cell = row.createCell(6);
		cell.setCellValue("监控消息");
		cell.setCellStyle(style);
		cell = row.createCell(7);
		cell.setCellValue("状态切换时间");
		cell.setCellStyle(style);
		cell = row.createCell(8);
		cell.setCellValue("检查时间");
		cell.setCellStyle(style);

		for (int i = 0; i < monitorLogs.size(); i++) {
			row = sheet.createRow((int) i + 1);

			// 第四步，创建单元格，并设置值
			MonitorLog monitorLog = monitorLogs.get(i);
			row.createCell(0).setCellValue(monitorLog.getId());
			row.createCell(1).setCellValue(monitorLog.getMonitorSource());
			row.createCell(2).setCellValue(monitorLog.getSourceName());
			row.createCell(3).setCellValue(monitorLog.getMonitorType());
			row.createCell(4).setCellValue(monitorLog.getMonitorName());
			row.createCell(5).setCellValue(monitorLog.getMonitorStatus());
			row.createCell(6).setCellValue(monitorLog.getMessage());
			row.createCell(7).setCellValue(monitorLog.getUpdateTime());
			row.createCell(8).setCellValue(monitorLog.getCheckTime());
		}

		// 第六步，将文件存到指定位置

		FileOutputStream fout = new FileOutputStream(filePath);
		workbook.write(fout);
		fout.close();
		workbook.close();

		return true;
	}
}
