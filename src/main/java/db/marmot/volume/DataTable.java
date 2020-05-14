package db.marmot.volume;

import org.hibernate.validator.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

/**
 * 数据表
 * @author shaokang
 */
@Setter
@Getter
public class DataTable {
	
	/**
	 * 表名
	 */
	@NotBlank
	private String tableName;
	
	/**
	 * 描述
	 */
	private String content;
	
	public DataTable(String tableName, String content) {
		this.tableName = tableName;
		this.content = content;
	}
}
