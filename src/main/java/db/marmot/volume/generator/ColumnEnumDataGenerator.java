package db.marmot.volume.generator;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.generator.GraphicGeneratorException;
import db.marmot.repository.validate.Validators;
import db.marmot.volume.ColumnVolume;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 枚举类型不做分页处理
 * @author shaokang
 */
public class ColumnEnumDataGenerator extends AbstractColumnDataGenerator {
	
	private Map<String, Class> enumClassCache = new HashMap<>();
	
	@Override
	protected ColumnData generateData(ColumnVolume columnVolume, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
		ColumnData columnData = new ColumnData(columnVolume.getColumnValueCode(), columnVolume.getColumnShowCode());
		List<Map<String, Object>> enumData = new ArrayList<>();
		Class enumClass = loadEnumClass(columnData.getScript());
		for (Object enumObject : enumClass.getEnumConstants()) {
			Map<String, Object> rowData = new HashMap<>();
			ColumnEnum columnEnum = (ColumnEnum) enumObject;
			rowData.put(columnData.getColumnValueCode(), columnEnum.code());
			rowData.put(columnData.getColumnShowCode(), columnEnum.message());
			enumData.add(rowData);
		}
		columnData.setData(enumData);
		return columnData;
	}
	
	@Override
	public VolumeType volumeType() {
		return VolumeType.enums;
	}
	
	private Class loadEnumClass(String enumClassPath) {
		Class enumClass = enumClassCache.get(enumClassPath);
		if (enumClass != null) {
			return enumClass;
		}
		try {
			enumClass = Class.forName(enumClassPath);
			Validators.isTrue(enumClass.isEnum() && ColumnEnum.class.isAssignableFrom(enumClass), "class 必须是枚举并且必须实现 ColumnEnum");
			enumClassCache.put(enumClassPath, enumClass);
			return enumClass;
		} catch (ClassNotFoundException e) {
			throw new GraphicGeneratorException("加载枚举类异常", e);
		}
	}
}
