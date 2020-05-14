package db.marmot.graphic.generator.procedure;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import db.marmot.converter.ConverterAdapter;
import db.marmot.enums.ColumnType;
import db.marmot.enums.DataColor;
import db.marmot.enums.TotalType;
import db.marmot.graphic.*;
import db.marmot.graphic.converter.TotalConverter;
import db.marmot.graphic.generator.TabGraphicData;
import db.marmot.graphic.generator.TabGraphicDataColumn;
import db.marmot.volume.DataColumn;
import db.marmot.volume.DataVolume;
import db.marmot.volume.generator.ColumnGeneratorAdapter;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 表格数据结构处理-创建表格字段以及样式、数据结构调整(字段横向)、以及字段对应值处理
 * @author shaokang
 */
public class TabGraphicStructureProcedure extends GraphicStructureProcedure<TabGraphic, TabGraphicData> {
	
	public TabGraphicStructureProcedure(ColumnGeneratorAdapter columnGeneratorFactory) {
		super(columnGeneratorFactory);
	}
	
	@Override
	public void processed(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData) {
		if (graphic.getGraphicColumn().getDimenColumns().stream().anyMatch(DimenColumn::isColumnToRow)) {
			new ColumnToRowTabGraphicStructure(graphic, dataVolume, graphicData, columnGeneratorFactory).structureBuild();
			return;
		}
		new NormalTabGraphicStructure(graphic, dataVolume, graphicData, columnGeneratorFactory).structureBuild();
	}
	
	public abstract class TabGraphicStructure extends AbstractGraphicStructure<TabGraphic, TabGraphicData> {
		
		public TabGraphicStructure(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData, ColumnGeneratorAdapter columnGeneratorFactory) {
			super(graphic, dataVolume, graphicData, columnGeneratorFactory);
		}
		
		@Override
		public void structureBuild() {
			
			//-1.维度字段值转义以及掩码处理以及无值填充
			for (Map<String, Object> rowData : graphicData.getTabData()) {
				graphic.getGraphicColumn().getDimenColumns().forEach(dimenColumn -> {
					Object value = rowData.get(dimenColumn.getColumnCode());
					if (value == null || StringUtils.isBlank(value.toString())) {
						if (StringUtils.isNotBlank(graphic.getGraphicStyle().getPaddedValue())) {
							rowData.put(dimenColumn.getColumnCode(), graphic.getGraphicStyle().getPaddedValue());
						}
						return;
					}
					if (dimenColumn.isColumnEscape()) {
						Object escapeValue = getEscapeValue(dataVolume.getVolumeId(), dimenColumn.getColumnCode(),dimenColumn.getColumnType(), value);
						rowData.put(dimenColumn.getColumnCode(), escapeValue);
					}
					if (dimenColumn.isColumnMask() && dimenColumn.getColumnType() == ColumnType.string) {
						rowData.put(dimenColumn.getColumnCode(), ConverterAdapter.Mask.mask((String) value));
					}
				});
			}
			
			//-2.表格数据结构处理
			tabStructureBuild();
			
			//-3.计算字段样式列是否固定
			for (int index = 0; index < graphicData.getTabColumns().size(); index++) {
				TabGraphicDataColumn tabGraphicColumn = graphicData.getTabColumns().get(index);
				tabGraphicColumn.setFreezeColumn(graphic.getGraphicStyle().calculateFreeze(index + 1, graphicData.getTabColumns().size()));
			}
		}
		
		/**
		 * 表格数据格式构建
		 */
		protected abstract void tabStructureBuild();
	}
	
	/**
	 * 正常表格结构
	 */
	public class NormalTabGraphicStructure extends TabGraphicStructure {
		
		public NormalTabGraphicStructure(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData, ColumnGeneratorAdapter columnGeneratorFactory) {
			super(graphic, dataVolume, graphicData, columnGeneratorFactory);
		}
		
		@Override
		public void tabStructureBuild() {
			
			//-1.维度字段样式创建
			graphic.getGraphicColumn().getDimenColumns().forEach(dimenColumn -> {
				TabGraphicDataColumn tabGraphicColumn = new TabGraphicDataColumn();
				tabGraphicColumn.setDimenColumn(Boolean.TRUE);
				tabGraphicColumn.setColumnCode(dimenColumn.getColumnCode());
				tabGraphicColumn.setMergeColumn(graphic.getGraphicStyle().isMergeColumn());
				//-时间周期字段时间已经做了格式化位字符串处理,顾此处重新定义字段类型以及格式化方式
				tabGraphicColumn.setDataFormat(dimenColumn.mathCycleColumn() ? "TEXT" : dimenColumn.getDataFormat());
				tabGraphicColumn.setColumnType(dimenColumn.mathCycleColumn() ? ColumnType.string : dimenColumn.getColumnType());
				
				//-列小计针对的是维度
				tabGraphicColumn.setColumnSubtotal(tabGraphicColumn.isColumnSubtotal());
				
				//-根据自定义样式设置字段名称以及颜色
				TabColumnStyle tabColumnStyle = graphic.getGraphicStyle().findTabColumnStyle(dimenColumn.getColumnCode());
				if (tabColumnStyle != null) {
					tabGraphicColumn.addRowColumn(dimenColumn.getColumnCode(), tabColumnStyle.getColumnName());
					tabGraphicColumn.setDataColor(tabColumnStyle.getDataColor());
					graphicData.addTabColumn(tabGraphicColumn);
					return;
				}
				
				//-设置默认字段字段名以及颜色
				DataColumn dataColumn = dataVolume.findDataColumn(dimenColumn.getColumnCode());
				tabGraphicColumn.addRowColumn(dimenColumn.getColumnCode(), dataColumn.getColumnName());
				tabGraphicColumn.setDataColor(DataColor.black);
				graphicData.addTabColumn(tabGraphicColumn);
			});
			
			//-2.度量字段样式创建
			graphic.getGraphicColumn().getMeasureColumns().forEach(measureColumn -> {
				TabGraphicDataColumn tabGraphicColumn = new TabGraphicDataColumn();
				tabGraphicColumn.setDimenColumn(Boolean.FALSE);
				tabGraphicColumn.setMergeColumn(Boolean.FALSE);
				tabGraphicColumn.setColumnCode(measureColumn.getColumnCode());
				//-度量字段类型重置,无论原始字段是什么类型,在获取数据后度量字段永远是数字
				tabGraphicColumn.setColumnType(ColumnType.number);
				tabGraphicColumn.setDataFormat(measureColumn.getDataFormat());
				
				//-设置需要合计的列的合计方式 若没有指定默认所有度量字段列合计sum
				if (graphic.getGraphicStyle().isColumnTotal()) {
					tabGraphicColumn.setColumnTotal(graphic.getGraphicStyle().isColumnTotal());
					TotalColumn totalColumn = graphic.getGraphicStyle().findTotalColumn(measureColumn.getColumnCode());
					tabGraphicColumn.setColumnTotalType(totalColumn != null ? totalColumn.getTotalType() : TotalType.sum);
				}
				
				//-根据自定义样式设置字段名称以及颜色
				TabColumnStyle tabColumnStyle = graphic.getGraphicStyle().findTabColumnStyle(measureColumn.getColumnCode());
				if (tabColumnStyle != null) {
					tabGraphicColumn.setDataColor(tabColumnStyle.getDataColor());
					tabGraphicColumn.addRowColumn(measureColumn.getColumnCode(), tabColumnStyle.getColumnName());
					graphicData.addTabColumn(tabGraphicColumn);
					return;
				}
				
				//-设置默认字段字段名以及颜色
				DataColumn dataColumn = dataVolume.findDataColumn(measureColumn.getColumnCode());
				tabGraphicColumn.setDataColor(DataColor.black);
				tabGraphicColumn.addRowColumn(measureColumn.getColumnCode(), dataColumn.getColumnName());
				graphicData.addTabColumn(tabGraphicColumn);
			});
		}
	}
	
	/**
	 * 列转行表格结构
	 */
	public class ColumnToRowTabGraphicStructure extends TabGraphicStructure {
		
		/**
		 * 列计数器
		 */
		private int columnNum = 0;
		
		/**
		 * 正常维度字段
		 */
		private List<DimenColumn> normalDimens = new ArrayList<>();
		
		/**
		 * 列转行维度字段
		 */
		private List<DimenColumn> columnToRowDimens = new ArrayList<>();
		
		/**
		 * 列转行数据
		 */
		private List<Map<String, Map<String, Object>>> columnToRowData = new ArrayList<>();
		
		public ColumnToRowTabGraphicStructure(TabGraphic graphic, DataVolume dataVolume, TabGraphicData graphicData, ColumnGeneratorAdapter columnGeneratorFactory) {
			super(graphic, dataVolume, graphicData, columnGeneratorFactory);
			
			//-拆分维度 转为正常维度和列转行维度
			graphic.getGraphicColumn().getDimenColumns().forEach(dimenColumn -> {
				if (dimenColumn.isColumnToRow()) {
					columnToRowDimens.add(dimenColumn);
					return;
				}
				normalDimens.add(dimenColumn);
			});
			
			normalDimens.forEach(dimenColumn -> {
				TabGraphicDataColumn tabGraphicColumn = new TabGraphicDataColumn();
				tabGraphicColumn.setDimenColumn(Boolean.TRUE);
				tabGraphicColumn.setColumnCode(dimenColumn.getColumnCode());
				//-时间周期字段时间已经做了格式化位字符串处理,顾此处重新定义字段类型以及格式化方式
				tabGraphicColumn.setDataFormat(dimenColumn.mathCycleColumn() ? "TEXT" : dimenColumn.getDataFormat());
				tabGraphicColumn.setColumnType(dimenColumn.mathCycleColumn() ? ColumnType.string : dimenColumn.getColumnType());
				tabGraphicColumn.setMergeColumn(graphic.getGraphicStyle().isMergeColumn());
				tabGraphicColumn.setFreezeColumn(graphic.getGraphicStyle().isFreezeColumn());
				
				//-列小计针对的是维度
				tabGraphicColumn.setColumnSubtotal(tabGraphicColumn.isColumnSubtotal());
				
				//-根据自定义样式设置字段名称以及颜色
				TabColumnStyle tabColumnStyle = graphic.getGraphicStyle().findTabColumnStyle(dimenColumn.getColumnCode());
				if (tabColumnStyle != null) {
					tabGraphicColumn.setDataColor(tabColumnStyle.getDataColor());
					//-列转行 根据列转行的字段数据确定每列行数
					for (int rowIndex = 0; rowIndex < columnToRowDimens.size(); rowIndex++) {
						tabGraphicColumn.addRowColumn(dimenColumn.getColumnCode(), tabColumnStyle.getColumnName());
					}
					graphicData.addTabColumn(tabGraphicColumn);
					return;
				}
				
				//-设置默认字段字段名以及颜色
				DataColumn dataColumn = dataVolume.findDataColumn(dimenColumn.getColumnCode());
				tabGraphicColumn.setDataColor(DataColor.black);
				//-列转行 根据列转行的字段数据确定每列行数
				for (int rowIndex = 0; rowIndex < columnToRowDimens.size(); rowIndex++) {
					tabGraphicColumn.addRowColumn(dimenColumn.getColumnCode(), dataColumn.getColumnName());
				}
				graphicData.addTabColumn(tabGraphicColumn);
			});
		}
		
		@Override
		public void tabStructureBuild() {
			
			//-1.单行数据处理 生成列转行数据
			graphicData.getTabData().forEach(this::columnToRowByRowData);
			
			//-2.将列转行数据转成标准数据结构
			columnToRowDataStructureBuild();
		}
		
		private void columnToRowByRowData(Map<String, Object> rowData) {
			//-所有维度都列转行处理
			if (normalDimens.size() == columnToRowDimens.size()) {
				createColumnToRowData(rowData);
				return;
			}
			//-部分维度列转行处理
			StringBuilder builder = new StringBuilder();
			normalDimens.forEach(dimenColumn -> builder.append(rowData.get(dimenColumn.getColumnCode())));
			createColumnToRowData(builder.toString(), rowData);
		}
		
		/**
		 * 创建列转行数据-处理所有维度都需要转成行的情况
		 * @param rowData
		 */
		private void createColumnToRowData(Map<String, Object> rowData) {
			if (columnToRowData.isEmpty()) {
				Map<String, Map<String, Object>> columnToRowKeyData = Maps.newLinkedHashMap();
				columnToRowData.add(columnToRowKeyData);
			}
			Map<String, Object> columnToRowRowData = columnToRowData.stream().findFirst().get().get("column_to_row");
			if (columnToRowRowData == null) {
				columnToRowRowData = Maps.newLinkedHashMap();
				columnToRowData.stream().findFirst().get().put("column_to_row", columnToRowRowData);
			}
			for (MeasureColumn measureColumn : graphic.getGraphicColumn().getMeasureColumns()) {
				String columnCode = addColumnToRowData(measureColumn, columnToRowRowData, rowData);
				addColumnToRowColumnStyle(measureColumn, rowData, columnCode);
			}
		}
		
		/**
		 * 创建列转行数据-根据列转行key创建
		 * @param columnToRowKey
		 * @param rowData
		 */
		private void createColumnToRowData(String columnToRowKey, Map<String, Object> rowData) {
			Map<String, Object> columnToRowRowData = null;
			for (Map<String, Map<String, Object>> columnToRowKeyData : columnToRowData) {
				columnToRowRowData = columnToRowKeyData.get(columnToRowKey);
				if (columnToRowRowData != null) {
					break;
				}
			}
			if (columnToRowRowData == null) {
				columnNum = 0;
				columnToRowRowData = Maps.newLinkedHashMap();
				Map<String, Map<String, Object>> columnToRowKeyData = Maps.newLinkedHashMap();
				columnToRowKeyData.put(columnToRowKey, columnToRowRowData);
				columnToRowData.add(columnToRowKeyData);
			}
			for (DimenColumn dimenColumn : normalDimens) {
				columnToRowRowData.put(dimenColumn.getColumnCode(), rowData.get(dimenColumn.getColumnCode()));
			}
			for (MeasureColumn measureColumn : graphic.getGraphicColumn().getMeasureColumns()) {
				String columnCode = addColumnToRowData(measureColumn, columnToRowRowData, rowData);
				addColumnToRowColumnStyle(measureColumn, rowData, columnCode);
			}
		}
		
		/**
		 * 添加列转行数据
		 * @param measureColumn
		 * @param columnToRowRowData
		 * @param rowData
		 * @return
		 */
		private String addColumnToRowData(MeasureColumn measureColumn, Map<String, Object> columnToRowRowData, Map<String, Object> rowData) {
			String columnCode = createColumnToRowMeasureColumnCode(measureColumn, columnToRowRowData);
			columnToRowRowData.put(columnCode, rowData.get(measureColumn.getColumnCode()));
			return columnCode;
		}
		
		/**
		 * 计算列度量行字段编码
		 * @param measureColumn
		 * @param columnToRowRowData
		 * @return
		 */
		private String createColumnToRowMeasureColumnCode(MeasureColumn measureColumn, Map<String, Object> columnToRowRowData) {
			String columnCode = StringUtils.join(measureColumn.getColumnCode(), "_", columnNum);
			if (columnToRowRowData.get(columnCode) == null) {
				return columnCode;
			}
			columnNum++;
			return createColumnToRowMeasureColumnCode(measureColumn, columnToRowRowData);
		}
		
		/**
		 * 添加列转行字段样式
		 * @param measureColumn
		 * @param rowData
		 * @param columnCode
		 */
		private void addColumnToRowColumnStyle(MeasureColumn measureColumn, Map<String, Object> rowData, String columnCode) {
			TabGraphicDataColumn tabGraphicColumn = new TabGraphicDataColumn();
			
			tabGraphicColumn.setDataFormat(measureColumn.getDataFormat());
			//-度量字段类型重置,无论原始字段是什么类型,在获取数据后度量字段永远是数字
			tabGraphicColumn.setColumnCode(columnCode);
			tabGraphicColumn.setColumnType(ColumnType.number);
			tabGraphicColumn.setMergeColumn(Boolean.FALSE);
			tabGraphicColumn.setDimenColumn(Boolean.FALSE);
			
			//-设置需要合计的列的合计方式 若没有指定默认所有度量字段列合计sum
			if (graphic.getGraphicStyle().isColumnTotal()) {
				tabGraphicColumn.setColumnTotal(graphic.getGraphicStyle().isColumnTotal());
				TotalColumn totalColumn = graphic.getGraphicStyle().findTotalColumn(measureColumn.getColumnCode());
				tabGraphicColumn.setColumnTotalType(totalColumn != null ? totalColumn.getTotalType() : TotalType.sum);
			}
			
			//-创建列的维度行字段
			for (int i = 0; i < columnToRowDimens.size(); i++) {
				DimenColumn dimenColumn = columnToRowDimens.get(i);
				tabGraphicColumn.addRowColumn(StringUtils.join(dimenColumn.getColumnCode(), "_", columnNum), rowData.get(dimenColumn.getColumnCode()).toString());
			}
			
			//-创建列度量行字段颜色以及字段名
			TabColumnStyle tabColumnStyle = graphic.getGraphicStyle().findTabColumnStyle(measureColumn.getColumnCode());
			if (tabColumnStyle == null) {
				DataColumn dataColumn = dataVolume.findDataColumn(measureColumn.getColumnCode());
				tabGraphicColumn.setDataColor(DataColor.black);
				tabGraphicColumn.addRowColumn(columnCode, dataColumn.getColumnName());
				graphicData.addTabColumn(tabGraphicColumn);
				return;
			}
			
			tabGraphicColumn.setDataColor(tabColumnStyle.getDataColor());
			tabGraphicColumn.addRowColumn(columnCode, tabColumnStyle.getColumnName());
			graphicData.addTabColumn(tabGraphicColumn);
		}
		
		/**
		 * 列转行数据结构构建
		 */
		private void columnToRowDataStructureBuild() {
			//-1.添加行合计字段以及样式
			addRowTotalColumnStyle();
			//-2.将列转行数据转换成表格数据格式
			createTabDataByColumnToRowData();
		}
		
		/**
		 * 创建行合计字段样式
		 */
		private void addRowTotalColumnStyle() {
			if (graphicData.isRowTotal()) {
				columnNum++;
				for (MeasureColumn measureColumn : graphic.getGraphicColumn().getMeasureColumns()) {
					TabGraphicDataColumn tabGraphicColumn = new TabGraphicDataColumn();
					tabGraphicColumn.setMergeColumn(Boolean.FALSE);
					tabGraphicColumn.setDimenColumn(Boolean.FALSE);
					//-合计行字段不做列合计
					tabGraphicColumn.setColumnTotalType(null);
					//-合计行字段默认为黑色
					tabGraphicColumn.setDataColor(DataColor.black);
					//-度量字段类型重置,无论原始字段是什么类型,在获取数据后度量字段永远是数字
					tabGraphicColumn.setColumnType(ColumnType.number);
					tabGraphicColumn.setDataFormat(measureColumn.getDataFormat());
					//-创建列的维度行合计字段
					for (int i = 0; i < columnToRowDimens.size(); i++) {
						tabGraphicColumn.addRowColumn("row_total", graphic.getGraphicStyle().getRowTotalAlias());
					}
					//-获取自定义度量字段名
					String measureRowTotalColumnCode = StringUtils.join(measureColumn.getColumnCode(), "_", columnNum);
					tabGraphicColumn.setColumnCode(measureRowTotalColumnCode);
					TabColumnStyle tabColumnStyle = graphic.getGraphicStyle().findTabColumnStyle(measureColumn.getColumnCode());
					if (tabColumnStyle != null) {
						tabGraphicColumn.addRowColumn(measureRowTotalColumnCode, tabColumnStyle.getColumnName());
						graphicData.addTabColumn(tabGraphicColumn);
						return;
					}
					//-获取默认度量字段名
					DataColumn dataColumn = dataVolume.findDataColumn(measureColumn.getColumnCode());
					tabGraphicColumn.addRowColumn(measureRowTotalColumnCode, dataColumn.getColumnName());
					graphicData.addTabColumn(tabGraphicColumn);
				}
			}
		}
		
		/**
		 * 将列转行数据转换成表格数据
		 * @return
		 */
		private void createTabDataByColumnToRowData() {
			List<Map<String, Object>> tabData = Lists.newArrayList();
			columnToRowData.forEach(columnToRowKeyData -> {
				//-获取列转行每个key对应的数据
				Map<String, Object> columnToRowRowData = columnToRowKeyData.values().stream().findFirst().get();
				//-存在合计时,增加每行的行合计数据
				if (graphicData.isColumnTotal()) {
					graphic.getGraphicColumn().getMeasureColumns().forEach(measureColumn -> {
						Set<String> columnCodes = columnToRowRowData.keySet();
						List<BigDecimal> rowTotalMeasureColumnValues = new ArrayList<>();
						//-获取行数据字段名包含维度字段的所有字段
						List<String> rowTotalMeasureColumnCodes = columnCodes.stream().filter(columnCode -> columnCode.contains(measureColumn.getColumnCode())).collect(Collectors.toList());
						rowTotalMeasureColumnCodes.forEach(rowTotalMeasureColumnCode -> rowTotalMeasureColumnValues.add((BigDecimal) columnToRowRowData.get(rowTotalMeasureColumnCode)));
						TotalConverter totalConverter = ConverterAdapter.getInstance().getTotalConverter(graphic.getGraphicStyle().getRowTotalType());
						columnToRowRowData.put(StringUtils.join(measureColumn.getColumnCode(), "_", columnNum), totalConverter.calculateTotalValue(rowTotalMeasureColumnValues));
					});
				}
				tabData.add(columnToRowRowData);
			});
			graphicData.setTabData(tabData);
		}
	}
}
