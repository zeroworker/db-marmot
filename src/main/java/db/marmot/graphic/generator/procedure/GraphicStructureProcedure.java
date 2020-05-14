package db.marmot.graphic.generator.procedure;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.Lists;
import db.marmot.enums.ColumnType;
import db.marmot.enums.Operators;
import db.marmot.graphic.FilterColumn;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.GraphicData;
import db.marmot.graphic.generator.GraphicGeneratorException;
import db.marmot.repository.RepositoryException;
import db.marmot.volume.DataVolume;
import db.marmot.volume.generator.ColumnData;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

import lombok.extern.slf4j.Slf4j;

/**
 * 数据结构处理
 * @author shaokang
 */
@Slf4j
public abstract class GraphicStructureProcedure<G extends Graphic, D extends GraphicData> implements GraphicProcedure<G, D> {
	
	protected ColumnGeneratorAdapter columnGeneratorFactory;
	
	public GraphicStructureProcedure(ColumnGeneratorAdapter columnGeneratorFactory) {
		this.columnGeneratorFactory = columnGeneratorFactory;
	}
	
	@Override
	public int getOrder() {
		return 4;
	}
	
	@Override
	public boolean match(G graphic, DataVolume dataVolume) {
		return true;
	}
	
	protected interface GraphicStructure {
		
		/**
		 * 图表结构构建
		 */
		void structureBuild();
		
	}
	
	protected abstract class AbstractGraphicStructure<G extends Graphic, D extends GraphicData> implements GraphicStructure {
		
		/**
		 * 图表
		 */
		protected G graphic;
		
		/**
		 * 数据集
		 */
		protected DataVolume dataVolume;
		
		/**
		 * 图表数据
		 */
		protected D graphicData;
		
		/**
		 * 字段数据集数据生成适配器
		 */
		protected ColumnGeneratorAdapter columnGeneratorFactory;
		
		/**
		 * 字段值转义缓存
		 */
		private Map<Object, Object> columnValueEscapeCache = new HashMap<>();
		
		public AbstractGraphicStructure(G graphic, DataVolume dataVolume, D graphicData, ColumnGeneratorAdapter columnGeneratorFactory) {
			this.graphic = graphic;
			this.dataVolume = dataVolume;
			this.graphicData = graphicData;
			this.columnGeneratorFactory = columnGeneratorFactory;
		}
		
		/**
		 * 获取字段转义值
		 * @param volumeId 数据集ID
		 * @param columnCode 字段编码
		 * @param columnType 字段类型
		 * @param originalValue 原始值
		 * @return
		 */
		protected Object getEscapeValue(long volumeId, String columnCode, ColumnType columnType, Object originalValue) {
			
			if (columnValueEscapeCache.containsKey(columnCode + "_" + originalValue)) {
				return columnValueEscapeCache.get(columnCode + "_" + originalValue);
			}
			
			try {
				FilterColumn filterColumn = new FilterColumn(columnCode, columnType, Operators.equals, originalValue);
				ColumnData columnData = columnGeneratorFactory.generateColumnData(volumeId, columnCode, Lists.newArrayList(filterColumn), 0, Integer.MAX_VALUE);
				if (columnData.getData() != null && !columnData.getData().isEmpty()) {
					for (Map<String, Object> rowData : columnData.getData()) {
						Object columnValue = rowData.get(columnData.getColumnValueCode());
						if (originalValue.equals(columnValue)) {
							Object escapeValue = rowData.get(columnData.getColumnShowCode());
							columnValueEscapeCache.put(columnCode + "_" + originalValue, escapeValue);
							return escapeValue;
						}
					}
				}
				return originalValue;
				
			} catch (GraphicGeneratorException | RepositoryException e) {
				log.warn("图表数据字段值转义失败,使用原始值:%s", columnCode, e.getMessage());
				return originalValue;
			}
		}
	}
}
