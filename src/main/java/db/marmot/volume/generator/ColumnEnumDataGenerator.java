package db.marmot.volume.generator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.generator.GraphicGeneratorException;

/**
 * 枚举类型不做分页处理
 * @author shaokang
 */
public class ColumnEnumDataGenerator extends AbstractColumnDataGenerator {
	
	private Map<String, Class> enumClassCache = new HashMap<>();

	@Override
	protected void generateData(ColumnData columnData, List<FilterColumn> filterColumns, int pageNum, int pageSize) {
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
	}
	
	@Override
	public VolumeType volumeType() {
		return VolumeType.enums;
	}
	
	/**
	 * 加载枚举类
	 * @param enumClassPath
	 * @return
	 */
	private Class loadEnumClass(String enumClassPath) {
		Class enumClass = enumClassCache.get(enumClassPath);
		if (enumClass != null) {
			return enumClass;
		}
		try {
			enumClass = Class.forName(enumClassPath);
			if (!enumClass.isEnum()) {
				throw new GraphicGeneratorException("该类非枚举类型");
			}
			if (!ColumnEnum.class.isAssignableFrom(enumClass)) {
				throw new GraphicGeneratorException("枚举必须实现 ColumnEnum");
			}
			enumClassCache.put(enumClassPath, enumClass);
			return enumClass;
		} catch (Exception e) {
			throw new GraphicGeneratorException("加载枚举类异常", e);
		}
	}
}
