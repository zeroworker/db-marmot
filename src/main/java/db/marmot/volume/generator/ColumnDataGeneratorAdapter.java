package db.marmot.volume.generator;

import com.google.common.collect.Maps;
import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.generator.GraphicGeneratorException;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.ColumnVolume;
import db.marmot.volume.DataColumn;
import org.springframework.beans.factory.InitializingBean;

import java.util.List;
import java.util.Map;

/**
 * @author shaokang
 */
public class ColumnDataGeneratorAdapter implements ColumnGeneratorAdapter, InitializingBean {
	
	private DataSourceRepository dataSourceRepository;
	private Map<VolumeType, ColumnDataGenerator> columnDataGenerators = Maps.newHashMap();
	
	public ColumnDataGeneratorAdapter(DataSourceRepository dataSourceRepository) {
		this.dataSourceRepository = dataSourceRepository;
	}
	
	/**
	 * 注册字段数据生成器
	 * @param columnDataGenerator
	 */
	private void registerColumnDataGenerator(ColumnDataGenerator columnDataGenerator) {
		columnDataGenerators.put(columnDataGenerator.volumeType(), columnDataGenerator);
	}
	
	@Override
	public ColumnData generateColumnData(String volumeCode, String columnCode, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		
		Validators.notNull(columnCode, "columnCode 不能为空");
		if (filterColumns != null && filterColumns.size() > 0) {
			filterColumns.forEach(filterColumn -> Validators.assertJSR303(filterColumn));
		}
		
		DataColumn dataColumn = dataSourceRepository.findDataColumn(volumeCode, columnCode);
		ColumnVolume columnVolume = dataSourceRepository.findColumnVolume(dataColumn.getScreenColumn());
		ColumnDataGenerator columnDataGenerator = columnDataGenerators.get(columnVolume.getVolumeType());
		if (columnDataGenerator != null) {
			return columnDataGenerators.get(columnVolume.getVolumeType()).getColumnData(columnVolume, filterColumns, pageNum, pageSize);
		}
		throw new GraphicGeneratorException(String.format("字段数据集生成器未实现[%s]", columnVolume.getVolumeType().getMessage()));
	}
	
	@Override
	public void afterPropertiesSet() {
		Validators.notNull(dataSourceRepository, "dataSourceRepository 不能为空");
		registerColumnDataGenerator(new ColumnEnumDataGenerator());
		registerColumnDataGenerator(new ColumnCustomDataGenerator(dataSourceRepository));
		registerColumnDataGenerator(new ColumnSqlDataGenerator(dataSourceRepository));
	}
}
