package db.marmot.volume.controller.request;

import db.marmot.graphic.FilterColumn;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.Size;
import java.util.List;
import java.util.StringJoiner;

/**
 * @author shaokang
 */
@Setter
@Getter
public class ColumnDataRequest {
	
	/**
	 * 数据集ID
	 */
	private long volumeId;
	
	/**
	 * 字段编码
	 */
	@NotBlank
	private String columnCode;
	
	/**
	 * 过滤条件
	 */
	private List<FilterColumn> filterColumns;
	
	/**
	 * 分页数
	 */
	@Size(min = 1)
	private int pageNum;
	
	/**
	 * 分页大小
	 */
	@Size(min = 1, max = 10)
	private int pageSize;

	@Override
	public String toString() {
		return new StringJoiner(", ", ColumnDataRequest.class.getSimpleName() + "[", "]")
				.add("volumeId=" + volumeId)
				.add("columnCode='" + columnCode + "'")
				.add("filterColumns=" + filterColumns)
				.add("pageNum=" + pageNum)
				.add("pageSize=" + pageSize)
				.toString();
	}
}
