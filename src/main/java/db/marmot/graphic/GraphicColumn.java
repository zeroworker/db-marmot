package db.marmot.graphic;

import com.google.common.collect.Lists;
import db.marmot.enums.ColumnType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Comparator;
import java.util.List;

/**
 * @author shaokang
 */
@Setter
@Getter
public class GraphicColumn implements Serializable {
	
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
	
	public void validateDimenColumn(DataVolume dataVolume) {
		dimenColumns.forEach(dimenColumn -> dimenColumn.validateDimenColumn(dataVolume));
		dimenColumns.sort(Comparator.comparingInt(DimenColumn::getOrder));
	}
	
	public void validateMeasureColumn(DataVolume dataVolume) {
		measureColumns.forEach(measureColumn -> measureColumn.validateMeasureColumn(dataVolume));
		measureColumns.sort(Comparator.comparingInt(MeasureColumn::getOrder));
	}
	
	public void validateFilterColumn(DataVolume dataVolume) {
		dataVolume.findFilterDataColumns()
				.forEach(dataColumn ->
						filterColumns.forEach(filterColumn -> {
							if (!filterColumn.getColumnCode().equals(dataColumn.getColumnCode())) {
								throw new ValidateException(String.format("字段%s过滤条件必填", dataColumn.getColumnName()));
							}
						}));
		filterColumns.forEach(filterColumn -> filterColumn.validateFilterColumn(dataVolume));
	}

	public DimenColumn findDateDimenColumn(){
		return dimenColumns
				.stream()
				.filter(column -> column.getColumnType() == ColumnType.date)
				.findFirst()
				.get();
	}
}
