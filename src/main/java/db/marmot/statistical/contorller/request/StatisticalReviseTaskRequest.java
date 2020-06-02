package db.marmot.statistical.contorller.request;

import db.marmot.enums.ReviseStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class StatisticalReviseTaskRequest {
	
	/**
	 * 仪表盘ID
	 */
	private long dashboardId;
	
	/**
	 * 数据集ID
	 */
	private String volumeCode;
	
	/**
	 * 订正状态
	 */
	private ReviseStatus reviseStatus;
	
	/**
	 * 分页数
	 */
	private int pageNum;
	
	/**
	 * 分页大小
	 */
	private int pageSize;
}
