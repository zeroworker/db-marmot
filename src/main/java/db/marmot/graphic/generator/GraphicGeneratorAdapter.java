package db.marmot.graphic.generator;

import db.marmot.enums.GraphicType;
import db.marmot.graphic.Graphic;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

/**
 * 图表生成适配器
 * @author shaokang
 */
public interface GraphicGeneratorAdapter {
	
	void setRepositoryAdapter(RepositoryAdapter repositoryAdapter);
	
	void setColumnGeneratorAdapter(ColumnGeneratorAdapter columnGeneratorAdapter);
	
	void setStatisticalGenerateAdapter(StatisticalGenerateAdapter statisticalGenerateAdapter);
	
	/**
	 * 生成图表数据
	 * @param graphicName 图表名称
	 * @param graphicFormat 格式化图表数据
	 * @return
	 */
	<D extends GraphicData> D generateGraphicData(String graphicName, boolean graphicFormat);
	
	/**
	 * 生成图表数据 图表格式化以及实时图表设置可以直接设置graphic
	 * @param volumeCode 数据集编码
	 * @param graphicType 图表类型
	 * @param graphic 图表
	 * @return
	 */
	<D extends GraphicData> D generateGraphicData(String volumeCode, GraphicType graphicType, Graphic graphic);
}
