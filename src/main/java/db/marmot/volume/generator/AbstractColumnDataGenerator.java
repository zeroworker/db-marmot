package db.marmot.volume.generator;

import java.util.List;

import db.marmot.graphic.FilterColumn;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.ColumnVolume;

/**
 * @author shaokang
 */
public abstract class AbstractColumnDataGenerator implements ColumnDataGenerator {
	
	@Override
	public ColumnData getColumnData(ColumnVolume columnVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		
		Validators.notNull(columnVolume, "columnVolume 不能为空");
		if (filterColumns != null && filterColumns.size() > 0) {
			filterColumns.forEach(filterColumn -> Validators.assertJSR303(filterColumn));
		}
		
		ColumnData columnData = new ColumnData(columnVolume.getScript(), columnVolume.getDbName(), columnVolume.getColumnValueCode(), columnVolume.getColumnShowCode());
		generateData(columnData, filterColumns, pageNum, pageSize);
		return columnData;
	}
	
	/**
	 * 生成数据
	 * @param columnData
	 * @return
	 */
	protected abstract void generateData(ColumnData columnData, List<FilterColumn> filterColumns, int pageNum, int pageSize);
}
