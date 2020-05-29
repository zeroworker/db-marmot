package db.marmot.volume.generator;

import db.marmot.graphic.FilterColumn;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.ColumnVolume;

import java.util.List;

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
		return generateData(columnVolume, filterColumns, pageNum, pageSize);
	}
	
	protected abstract ColumnData generateData(ColumnVolume columnVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize);
}
