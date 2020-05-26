package db.marmot.graphic.generator;

import com.google.common.collect.Maps;
import db.marmot.enums.GraphicType;
import db.marmot.enums.VolumeType;
import db.marmot.graphic.Dashboard;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.GraphicDesign;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;
import db.marmot.volume.generator.ColumnGeneratorAdapter;
import org.springframework.beans.factory.InitializingBean;

import java.util.Map;

/**
 * 图表生成适配器
 * @author shaokang
 */
public class GraphicDataGeneratorAdapter implements GraphicGeneratorAdapter, InitializingBean {
	
	private DataSourceRepository dataSourceRepository;
	private ColumnGeneratorAdapter columnGeneratorAdapter;
	private StatisticalGenerateAdapter statisticalGenerateAdapter;
	private Map<GraphicType, GraphicDataGenerator> graphicDataGenerators = Maps.newHashMap();
	
	public GraphicDataGeneratorAdapter(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		this.dataSourceRepository = dataSourceRepository;
		this.columnGeneratorAdapter = columnGeneratorAdapter;
		this.statisticalGenerateAdapter = statisticalGenerateAdapter;
	}
	
	@Override
	public GraphicData generateGraphicData(String graphicCode, boolean graphicFormat) {
		GraphicDesign graphicDesign = dataSourceRepository.findGraphicDesign(graphicCode);
		Dashboard dashboard = dataSourceRepository.findDashboard(graphicDesign.getBoardId());
		DataVolume dataVolume = dataSourceRepository.findDataVolume(dashboard.getVolumeCode());
		
		//-模型统计非实时生成图表,sql统计实时生成图表
		Graphic graphic = graphicDesign.getGraphic();
		graphic.setGraphicFormat(graphicFormat);
		graphic.setGraphicInstant(dataVolume.getVolumeType() == VolumeType.model ? false : true);
		
		return generateGraphicData(dataVolume, graphicDesign.getGraphicType(), graphic);
	}
	
	@Override
	public GraphicData generateGraphicData(String volumeCode, GraphicType graphicType, Graphic graphic) {
		Validators.notNull(graphic, "graphic 不能为空");
		Validators.notNull(graphicType, "graphicType 不能为空");
		//-实时生成图表	
		graphic.setGraphicInstant(Boolean.TRUE.booleanValue());
		DataVolume dataVolume = dataSourceRepository.findDataVolume(volumeCode);
		
		return generateGraphicData(dataVolume, graphicType, graphic);
	}
	
	private GraphicData generateGraphicData(DataVolume dataVolume, GraphicType graphicType, Graphic graphic) {
		graphic.validateGraphic(dataVolume);
		
		GraphicDataGenerator graphicDataGenerator = graphicDataGenerators.get(graphicType);
		if (graphicDataGenerator != null) {
			return graphicDataGenerator.getGraphicData(graphic, dataVolume);
		}
		
		throw new GraphicGeneratorException(String.format("图表生成器未实现[%s]", graphicType.getMessage()));
	}
	
	@Override
	public void afterPropertiesSet() {
		registerGraphicDataGenerator(new TabGraphicDataGenerator(dataSourceRepository, columnGeneratorAdapter, statisticalGenerateAdapter));
	}
	
	/**
	 * 注册图表数据生成器
	 * @param graphicDataGenerator
	 */
	private void registerGraphicDataGenerator(GraphicDataGenerator graphicDataGenerator) {
		graphicDataGenerators.put(graphicDataGenerator.graphicType(), graphicDataGenerator);
	}
}
