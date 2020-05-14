package db.marmot.graphic;

import com.alibaba.fastjson.JSONObject;
import com.google.common.base.Splitter;
import db.marmot.converter.ColumnConverter;
import db.marmot.converter.ConverterAdapter;
import db.marmot.converter.SelectSqlBuilderConverter;
import db.marmot.enums.*;
import db.marmot.statistical.ConditionColumn;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.parser.SelectColumn;
import db.marmot.volume.parser.SelectCondition;
import db.marmot.volume.parser.SelectTable;
import db.marmot.volume.parser.SqlSelectQueryParser;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphic extends Graphic {
	
	private static final long serialVersionUID = 5362635861125108894L;
	
	/**
	 * 表格类型
	 */
	@NotNull
	private TabGraphicType tabType = TabGraphicType.detail;
	
	/**
	 * 表格列
	 */
	@Valid
	@NotNull
	private TabGraphicColumn graphicColumn = new TabGraphicColumn();
	
	/**
	 * 表格样式
	 */
	@Valid
	@NotNull
	private TabGraphicStyle graphicStyle = new TabGraphicStyle();
	
	@Override
	public List<String> getModelNames() {
		List<String> models = new ArrayList<>();
		
		graphicColumn.getMeasureColumns().forEach(measureColumn -> {
			if (measureColumn.getAggregates() != Aggregates.calculate) {
				models.add(measureColumn.getModelName());
				return;
			}
			models.addAll(Splitter.on(",").splitToList(measureColumn.getModelName()));
		});
		
		return models;
	}
	
	@Override
	public List<StatisticalModel> createStatisticalModels(DataVolume dataVolume,String dbType, String graphicName) {
		
		List<StatisticalModel> statisticalModels = new ArrayList<>();
		ConverterAdapter converterAdapter = ConverterAdapter.getInstance();
		SqlSelectQueryParser parser = new SqlSelectQueryParser(dbType, dataVolume.getSqlScript()).parse();
		
		for (SelectTable selectTable : parser.getSelectTables()) {
			
			List<SelectColumn> selectColumns = parser.getSelectColumns(selectTable);
			List<SelectCondition> selectConditions = parser.getSelectConditions(selectTable);
			SelectSqlBuilderConverter selectSqlBuilder = converterAdapter.newInstanceSqlBuilder(dbType, null);
			selectSqlBuilder.addSelectTable(selectTable.getTableName(), selectTable.getTableAlias());
			selectSqlBuilder.addSelectItem("id");
			
			StatisticalModel statisticalModel = new StatisticalModel();
			statisticalModel.setDbName(dataVolume.getDbName());
			statisticalModel.setModelName(DigestUtils.md5Hex(graphicName));

			for (SelectColumn selectColumn : selectColumns) {
				//-添加聚合统计字段
				graphicColumn.addAggregateColumns(statisticalModel, dataVolume, selectColumn);
				//-添加条件字段
				graphicColumn.addConditionColumns(statisticalModel, selectColumn);
				//-添加分组字段
				graphicColumn.addGroupColumns(statisticalModel, selectColumn);
				//-添加时间字段
				dataVolume.addTimeColumn(statisticalModel, selectColumn);
				//-添加sql item
				String expr = StringUtils.join(selectColumn.getTableAlias(), ".", selectColumn.getColumnCode());
				DataColumn dataColumn = dataVolume.findDataColumn(selectColumn.getColumnAlias());
				if (dataColumn.getColumnType() == ColumnType.number && dataColumn.getUnitValue() > 0) {
					expr = StringUtils.join(selectColumn.getTableAlias(), ".", selectColumn.getColumnCode(), "*", dataColumn.getUnitValue());
				}
				selectSqlBuilder.addSelectExprItem(expr, selectColumn.getColumnAlias());
			}
			
			for (SelectCondition condition : selectConditions) {
				ColumnConverter converter = converterAdapter.getColumnConverter(condition.getRightValue().getClass());
				statisticalModel.getConditionColumns().add(new ConditionColumn(condition.getColumnCode(), converter.columnType(), condition.getOperators(), condition.getRightValue()));
				selectSqlBuilder.addSelectExprItem(StringUtils.join(condition.getTableAlias(), ".", condition.getColumnCode()), condition.getColumnAlias());
			}
			
			statisticalModel.setWindowUnit(WindowUnit.DAY);
			statisticalModel.addOffsetExpr(getOffsetExpr());
			statisticalModel.setWindowType(WindowType.SIMPLE_TIME);
			statisticalModel.setVolumeId(dataVolume.getVolumeId());
			statisticalModel.setFetchSql(selectSqlBuilder.toSql());
			statisticalModel.setFetchStep(dataVolume.getVolumeLimit());
			statisticalModel.setMemo(StringUtils.join("tab图表(", graphicName, ")"));
			statisticalModels.add(statisticalModel);
		}
		return statisticalModels;
	}
	
	@Override
	public String toJSONGraphic() {
		return JSONObject.toJSONString(this);
	}
	
	@Override
	public void validateGraphic(DataVolume dataVolume) {
		super.validateGraphic(dataVolume);
		graphicStyle.validateGraphicStyle();
		graphicColumn.validateFilterColumn(dataVolume);
		graphicColumn.validateDimenColumn(dataVolume, tabType == TabGraphicType.aggregate ? true : false);
		graphicColumn.validateMeasureColumn(dataVolume, tabType == TabGraphicType.aggregate ? true : false);
	}
}
