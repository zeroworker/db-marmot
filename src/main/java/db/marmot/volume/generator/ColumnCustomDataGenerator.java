package db.marmot.volume.generator;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.repository.DataSourceRepository;
import db.marmot.volume.ColumnVolume;

import java.util.List;

/**
 * @author shaokang
 */
public class ColumnCustomDataGenerator extends AbstractColumnDataGenerator {
	
	private DataSourceRepository dataSourceRepository;
	
	public ColumnCustomDataGenerator(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}
	
	@Override
	protected ColumnData generateData(ColumnVolume columnVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		ColumnData columnData = new ColumnData(columnVolume.getColumnValueCode(), columnVolume.getColumnShowCode());
		columnData.setData(dataSourceRepository.queryCustomData(columnVolume.getVolumeCode(), filterColumns, pageNum, pageSize));
		return columnData;
	}
	
	@Override
	public VolumeType volumeType() {
		return VolumeType.custom;
	}
}
