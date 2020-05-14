package db.marmot.graphic.generator;

import java.io.Serializable;
import java.util.List;

import db.marmot.enums.GraphicType;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public abstract class GraphicData implements Serializable {
	
	private static final long serialVersionUID = -2086080708445869859L;
	
	/**
	 * 图表sql
	 */
	private String graphicSql;
	
	/**
	 * 图表类型
	 */
	private GraphicType graphicType;
	
	/**
	 * 图表描述 图表结果描述
	 */
	private String graphicMemo = "生成成功";
	
	/**
	 * 图表数据是否为空
	 * @return
	 */
	public abstract boolean emptyData();
	
	/**
	 * 构建文件表头
	 * @return
	 */
	public abstract List<List<String>> buildFileHead();
	
	/**
	 * 构建文件数据
	 * @return
	 */
	public abstract List<List<Object>> buildFileData();
	
}
