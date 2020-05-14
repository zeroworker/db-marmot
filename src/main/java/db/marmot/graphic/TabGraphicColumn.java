package db.marmot.graphic;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.google.common.collect.Lists;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;
import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.statistical.StatisticalModel;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.parser.SelectColumn;

import lombok.Getter;
import lombok.Setter;

/**
 * @author shaokang
 */
@Setter
@Getter
public class TabGraphicColumn implements GraphicColumn {
	
	private static final long serialVersionUID = 3304893174977034424L;
	
	/**
	 * 数据列-维度
	 */
	@Valid
	@NotNull
	private List<DimenColumn> dimenColumns = Lists.newArrayList();
	
	/**
	 * 数据列-度量
	 */
	@Valid
	@NotNull
	private List<MeasureColumn> measureColumns = Lists.newArrayList();
	
	/**
	 * 数据过滤
	 */
	@Valid
	@NotNull
	private List<FilterColumn> filterColumns = Lists.newArrayList();
	
	@Override
	public void validateDimenColumn(DataVolume dataVolume, boolean aggregateDimen) {
		for (DimenColumn dimenColumn : dimenColumns) {
			dimenColumn.validateDimenColumn(dataVolume, aggregateDimen);
		}
		dimenColumns.sort(Comparator.comparingInt(DimenColumn::getOrder));
	}
	
	@Override
	public void validateMeasureColumn(DataVolume dataVolume, boolean aggregateMeasure) {
		for (MeasureColumn measureColumn : measureColumns) {
			measureColumn.validateMeasureColumn(dataVolume, aggregateMeasure);
		}
		measureColumns.sort(Comparator.comparingInt(MeasureColumn::getOrder));
	}
	
	@Override
	public void validateFilterColumn(DataVolume dataVolume) {
		
		//验证过滤字段正确性
		for (FilterColumn filterColumn : filterColumns) {
			filterColumn.validateFilterColumn(dataVolume);
		}
		
		//验证必须过滤字段是否存在过滤条件
		dataVolume.getDataColumns().stream().filter(DataColumn::isColumnFilter).forEach(dataColumn -> {
			if (findFilterColumn(dataColumn.getColumnCode()) == null) {
				throw new ValidateException(String.format("字段%s过滤条件必填", dataColumn.getColumnName()));
			}
		});
		
		//-模型统计数据源时间必须做区间选择
		if (dataVolume.getVolumeType() == VolumeType.model) {
			if (findFilterDateColumns().size() != 2) {
				throw new ValidateException("模型统计数据源图表数据必须做时间过滤区间选择");
			}
		}
	}
	
	/**
	 * 设置统计字段
	 * @param statisticalModel
	 * @param selectColumn
	 */
	public void addAggregateColumns(StatisticalModel statisticalModel, DataVolume dataVolume, SelectColumn selectColumn) {
		measureColumns.forEach(measureColumn -> measureColumn.addAggregateColumn(statisticalModel, dataVolume, selectColumn));
	}
	
	/**
	 * 添加条件字段
	 * @param statisticalModel
	 * @param selectColumn
	 */
	public void addConditionColumns(StatisticalModel statisticalModel, SelectColumn selectColumn) {
		filterColumns.forEach(filterColumn -> filterColumn.addConditionColumn(statisticalModel, selectColumn));
	}
	
	/**
	 * 添加分组字段
	 * @param statisticalModel
	 * @param selectColumn
	 */
	public void addGroupColumns(StatisticalModel statisticalModel, SelectColumn selectColumn) {
		dimenColumns.forEach(dimenColumn -> dimenColumn.addGroupColumn(statisticalModel, selectColumn));
	}
	
	/**
	 * 根据字段编码获取过滤字段
	 * @param columnCode
	 * @return
	 */
	public FilterColumn findFilterColumn(String columnCode) {
		for (FilterColumn filterColumn : filterColumns) {
			if (filterColumn.getColumnCode().equals(columnCode)) {
				return filterColumn;
			}
		}
		return null;
	}
	
	/**
	 * 获取周期字段 只有时间字段才存在周期字段并且数据集只有一个时间字段
	 * @return
	 */
	public DimenColumn findDimenCycleColumn() {
		for (DimenColumn dimenColumn : dimenColumns) {
			if (dimenColumn.getColumnType() == ColumnType.date) {
				return dimenColumn;
			}
		}
		return null;
	}
	
	/**
	 * 获取过滤时间字段 数据集限制时间字段唯一,不存在多时间字段
	 * @return
	 */
	public Map<Operators, FilterColumn> findFilterDateColumns() {
		Map<Operators, FilterColumn> operatorsFilters = new HashMap<>();
		for (FilterColumn filterColumn : filterColumns) {
			if (filterColumn.getColumnType() == ColumnType.date) {
				operatorsFilters.put(filterColumn.getOperators(), filterColumn);
			}
		}
		return operatorsFilters;
	}
}
