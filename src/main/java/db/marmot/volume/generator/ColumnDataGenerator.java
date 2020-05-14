package db.marmot.volume.generator;

import java.util.List;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.volume.ColumnVolume;

/**
 * 字段数据生成器
 * @author shaokang
 */
public interface ColumnDataGenerator {
	
	/**
	 * 数据集类型
	 * @return
	 */
	VolumeType volumeType();
	
	/**
	 * 获取字段数据
	 * @param columnVolume 字段编码
	 * @param filterColumns 过滤条件
	 * @param pageNum 页数
	 * @param pageSize 每页大小
	 * @return 字段数据
	 */
	ColumnData getColumnData(ColumnVolume columnVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize);
}
