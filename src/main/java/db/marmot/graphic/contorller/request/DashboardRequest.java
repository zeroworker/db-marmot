package db.marmot.graphic.contorller.request;

import db.marmot.enums.BoardType;
import lombok.Getter;
import lombok.Setter;

import java.util.StringJoiner;

/**
 * @author shaokang
 */
@Setter
@Getter
public class DashboardRequest {
	
	/**
	 * 仪表盘ID
	 */
	private long dashboardId;
	
	/**
	 * 创建人ID
	 */
	private String founderId;
	
	/**
	 * 仪表盘名称
	 */
	private String boardName;
	
	/**
	 * 仪表盘类型
	 */
	private BoardType boardType;
	
	/**
	 * 分页数
	 */
	private int pageNum;
	
	/**
	 * 分页大小
	 */
	private int pageSize;

	@Override
	public String toString() {
		return new StringJoiner(", ", DashboardRequest.class.getSimpleName() + "[", "]")
				.add("dashboardId=" + dashboardId)
				.add("founderId='" + founderId + "'")
				.add("boardName='" + boardName + "'")
				.add("boardType=" + boardType)
				.add("pageNum=" + pageNum)
				.add("pageSize=" + pageSize)
				.toString();
	}
}
