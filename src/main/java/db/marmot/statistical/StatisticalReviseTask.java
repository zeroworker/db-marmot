package db.marmot.statistical;

import db.marmot.enums.ReviseStatus;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 元数据出现数据错误处理步骤:
 * 1.指定元数据数据范围以及数据集->2.添加订正任务->3.指定统计回滚->4.订正元数据->5.执行统计订正
 * @author shaokang
 */
@Setter
@Getter
public class StatisticalReviseTask {
	
	/**
	 * 任务ID
	 */
	private long taskId;
	
	/**
	 * 数据集ID
	 */
	@NotBlank
	@Size(max = 512)
	private String volumeCode;
	
	/**
	 * 订正状态
	 */
	@NotNull
	private ReviseStatus reviseStatus = ReviseStatus.non_execute;
	
	/**
	 * 开始角标
	 */
	private long startIndex;
	
	/**
	 * 结束角标
	 */
	private long endIndex;
}
