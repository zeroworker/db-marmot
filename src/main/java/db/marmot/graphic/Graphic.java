package db.marmot.graphic;

import db.marmot.enums.ColumnType;
import db.marmot.enums.GraphicCycle;
import db.marmot.enums.GraphicLayout;
import db.marmot.enums.VolumeType;
import db.marmot.repository.validate.ValidateException;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.StatisticalModel;
import db.marmot.statistical.StatisticalModelBuilder;
import db.marmot.volume.DataVolume;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * @author shaokang
 */
@Setter
@Getter
public abstract class Graphic implements Serializable {
	
	/**
	 * 图表数据预览页数
	 */
	@Size(min = 1)
	private int graphicPage = 1;
	
	/**
	 * 表格图表预览行数
	 */
	@Size(max = 10000)
	private int graphicLimit = 10000;
	
	/**
	 * 是否格式化图表数据 数据展示需要格式化,数据导出无需格式化
	 */
	private boolean graphicFormat = Boolean.TRUE;
	
	/**
	 * 是否实时图表,数据源为模型统计时,不支持实时图表,实时数据为mock数据
	 */
	private boolean graphicInstant = Boolean.TRUE;
	
	/**
	 * 图表周期
	 */
	@NotNull
	private GraphicCycle graphicCycle = GraphicCycle.non;
	
	/**
	 * 图表格式
	 */
	@NotNull
	private GraphicLayout graphicLayout = GraphicLayout.detail;
	
	/**
	 * 图表模型
	 */
	@NotNull
	private GraphicModel graphicModel = new GraphicModel();
	
	/**
	 * 图表数据列
	 */
	@Valid
	@NotNull
	private GraphicColumn graphicColumn = new GraphicColumn();
	
	public void nextGraphicPage() {
		this.graphicPage = this.graphicPage + 1;
	}
	
	public String getModelName() {
		return graphicModel.getModelName();
	}
	
	public StatisticalModel configurationModel(StatisticalModelBuilder builder) {
		builder.addOffsetExpr(graphicModel.getModelName());
		StatisticalModel statisticalModel = builder.builder();
		graphicModel.setModelName(statisticalModel.getModelName());
		return statisticalModel;
	}
	
	public void validateGraphic(DataVolume dataVolume) {
		Validators.assertJSR303(this);
		validateModelGraphic(dataVolume);
		dataVolume.validateVolumeLimit(graphicLimit);
		graphicColumn.validateDimenColumn(dataVolume);
		graphicModel.validateGraphicModel(dataVolume);
		graphicColumn.validateFilterColumn(dataVolume);
		graphicColumn.validateMeasureColumn(dataVolume);
	}

	private void validateModelGraphic(DataVolume dataVolume) {
		List<DimenColumn> dimenColumns = graphicColumn.getDimenColumns();
		if (graphicLayout == GraphicLayout.aggregate
				&& graphicCycle == GraphicCycle.non
				&& dataVolume.getVolumeType() == VolumeType.model) {
			if (dimenColumns
					.stream()
					.filter(dimenColumn -> dimenColumn.getColumnType() == ColumnType.date)
					.count() > 0) {
				throw new ValidateException("无周期模型聚合图表不允许存在时间维度字段");
			}
			if (graphicLayout == GraphicLayout.aggregate
					&& graphicCycle != GraphicCycle.non
					&& dataVolume.getVolumeType() == VolumeType.model) {
				List<FilterColumn> filterColumns = graphicColumn.getFilterColumns();
				if (filterColumns
						.stream()
						.filter(filterColumn -> filterColumn.getColumnType() == ColumnType.date)
						.count() !=2) {
					throw new ValidateException("周期模型聚合图表必须存在唯一时间区间过滤条件");
				}
			}
		}
	}
}
