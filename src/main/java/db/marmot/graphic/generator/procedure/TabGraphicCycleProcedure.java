package db.marmot.graphic.generator.procedure;

import com.google.common.collect.Maps;
import db.marmot.converter.ConverterAdapter;
import db.marmot.graphic.DimenColumn;
import db.marmot.graphic.MeasureColumn;
import db.marmot.graphic.TabGraphic;
import db.marmot.graphic.converter.DateCycleConverter;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.volume.DataVolume;
import org.apache.commons.codec.digest.DigestUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * 表格数据周期换算
 * @author shaokang
 */
public class TabGraphicCycleProcedure extends GraphicCycleProcedure<TabGraphic, TabGraphicData> {
	
	private ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
	
	@Override
	public boolean match(TabGraphic graphic, DataVolume dataVolume) {
		DimenColumn dimenColumn = graphic.getGraphicColumn().findDimenCycleColumn();
		return dimenColumn != null && super.match(graphic, dataVolume);
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		DimenColumn dimenColumn = graphic.getGraphicColumn().findDimenCycleColumn();
		Map<String, Map<String, Object>> cycleData = Maps.newLinkedHashMap();
		DateCycleConverter dateCycleConverter = converterAdapter.getDateCycleConverter(dimenColumn.getDateCycle());
		for (Map<String, Object> rowData : graphicData.getTabData()) {
			createRowCycleData(rowData, cycleData, dateCycleConverter, graphic, dimenColumn);
		}
	}

	/**
	 * 行周期数据处理
	 * @param rowData 行数据
	 * @param cycleData 周期数据
	 * @param dateCycleConverter 周期转换器
	 * @param graphic 图表
	 * @param dimenColumn 周期维度字段
	 */
	private void createRowCycleData(Map<String, Object> rowData, Map<String, Map<String, Object>> cycleData, DateCycleConverter dateCycleConverter, TabGraphic graphic, DimenColumn dimenColumn) {
		//-若数据不存在 无法做周期处理,该条数据排除掉
		if (rowData.get(dimenColumn.getColumnCode()) != null) {
			String cycleDate = dateCycleConverter.convertValue((Date) rowData.get(dimenColumn.getColumnCode()), dimenColumn.getDataFormat());
			rowData.put(dimenColumn.getColumnCode(), cycleDate);
			String cycleKey = createCycleKey(rowData, graphic);
			Map<String, Object> rowCycleData = cycleData.get(cycleKey);
			if (rowCycleData == null) {
				cycleData.put(cycleKey, rowData);
				return;
			}
			mergeRowCycleData(rowCycleData, rowData, graphic);
		}
	}
	
	/**
	 * 创建周期唯一key
	 * @param rowData
	 * @param graphic
	 * @return
	 */
	private String createCycleKey(Map<String, Object> rowData, TabGraphic graphic) {
		StringBuilder builder = new StringBuilder();
		for (DimenColumn dimenColumn : graphic.getGraphicColumn().getDimenColumns()) {
			builder.append(rowData.get(dimenColumn.getColumnCode()));
		}
		return DigestUtils.md5Hex(builder.toString());
	}
	
	/**
	 * 合并相同周期key数据
	 * @param rowCycleData
	 * @param rowData
	 * @param graphic
	 */
	private void mergeRowCycleData(Map<String, Object> rowCycleData, Map<String, Object> rowData, TabGraphic graphic) {
		for (MeasureColumn measureColumn : graphic.getGraphicColumn().getMeasureColumns()) {
			BigDecimal rowValue = (BigDecimal) rowData.get(measureColumn.getColumnCode());
			BigDecimal cycleValue = (BigDecimal) rowCycleData.get(measureColumn.getColumnCode());
			rowCycleData.put(measureColumn.getColumnCode(), rowValue.add(cycleValue));
		}
	}
}
