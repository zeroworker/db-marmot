package db.marmot.graphic.generator.procedure;

import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.OperatorsConverter;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.volume.DataVolume;

import java.util.List;
import java.util.Map;

/**
 * 表格数据过滤(针对模型统计的数据-聚合值数据过滤)
 * @author shaokang
 */
public class TabGraphicFilterProcedure extends GraphicFilterProcedure<TabGraphic, TabGraphicData> {
	
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		List<FilterColumn> filterColumns = graphic.getGraphicColumn().getFilterColumns();
		return super.match(graphic, dataVolume) && !filterColumns.isEmpty();
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		List<FilterColumn> filterColumns = graphic.getGraphicColumn().getFilterColumns();
		graphicData.getData().removeIf(rowData -> filterRowData(rowData, filterColumns));
	}
	
	/**
	 * 过滤表格数据
	 * @param rowData
	 * @param filterColumns
	 * @return
	 */
	private boolean filterRowData(Map<String, Object> rowData, List<FilterColumn> filterColumns) {
		for (FilterColumn filterColumn : filterColumns) {
			OperatorsConverter operatorsConverter = converterAdapter.getOperatorsConverter(filterColumn.getOperators());
			if (!operatorsConverter.compareValue(filterColumn.getColumnType(), rowData.get(filterColumn.getColumnCode()), filterColumn.getRightValue())) {
				return true;
			}
		}
		return false;
	}
}
