package db.marmot.graphic.generator;

import java.util.Map;

import org.springframework.beans.factory.InitializingBean;

import com.google.common.collect.Maps;
import db.marmot.enums.GraphicType;
import db.marmot.enums.RepositoryType;
import db.marmot.enums.VolumeType;
import db.marmot.graphic.Dashboard;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.GraphicDesign;
import db.marmot.graphic.GraphicRepository;
import db.marmot.repository.RepositoryAdapter;
import db.marmot.repository.validate.Validators;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;
import db.marmot.volume.VolumeRepository;
import db.marmot.volume.generator.ColumnGeneratorAdapter;

/**
 * 图表生成适配器
 * @author shaokang
 */
public class GraphicDataGeneratorAdapter implements GraphicGeneratorAdapter, InitializingBean {
	
	private RepositoryAdapter repositoryAdapter;
	private GraphicRepository graphicRepository;
	private VolumeRepository volumeRepository;
	private ColumnGeneratorAdapter columnGeneratorAdapter;
	private StatisticalGenerateAdapter statisticalGenerateAdapter;
	private Map<GraphicType, GraphicDataGenerator> graphicDataGenerators = Maps.newHashMap();

	@Override
	public void setRepositoryAdapter(RepositoryAdapter repositoryAdapter) {
		this.repositoryAdapter = repositoryAdapter;
		this.graphicRepository = repositoryAdapter.getRepository(RepositoryType.graphic);
		this.volumeRepository = repositoryAdapter.getRepository(RepositoryType.volume);
	}
	
	@Override
	public void setColumnGeneratorAdapter(ColumnGeneratorAdapter columnGeneratorAdapter) {
		this.columnGeneratorAdapter = columnGeneratorAdapter;
	}
	
	@Override
	public void setStatisticalGenerateAdapter(StatisticalGenerateAdapter statisticalGenerateAdapter) {
		this.statisticalGenerateAdapter = statisticalGenerateAdapter;
	}
	
	@Override
	public GraphicData generateGraphicData(long graphicId, boolean graphicFormat) {
		GraphicDesign graphicDesign = graphicRepository.findGraphicDesign(graphicId);
		Dashboard dashboard = graphicRepository.findDashboard(graphicDesign.getBoardId());
		DataVolume dataVolume = volumeRepository.findDataVolume(dashboard.getVolumeId());
		
		//-模型统计非实时生成图表,sql统计实时生成图表
		Graphic graphic = graphicDesign.getGraphic();
		graphic.setGraphicFormat(graphicFormat);
		graphic.setGraphicInstant(dataVolume.getVolumeType() == VolumeType.model ? false : true);
		
		return generateGraphicData(graphicDesign.getGraphicId(), graphicDesign.getGraphicType(), graphic);
	}
	
	@Override
	public GraphicData generateGraphicData(long volumeId, GraphicType graphicType, Graphic graphic) {
		Validators.notNull(graphic, "graphic 不能为空");
		Validators.notNull(graphicType, "graphicType 不能为空");
		//-实时生成图表	
		graphic.setGraphicInstant(Boolean.TRUE.booleanValue());
		DataVolume dataVolume = volumeRepository.findDataVolume(volumeId);
		
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
		registerGraphicDataGenerator(new TabGraphicDataGenerator(repositoryAdapter, columnGeneratorAdapter, statisticalGenerateAdapter));
	}
	
	/**
	 * 注册图表数据生成器
	 * @param graphicDataGenerator
	 */
	private void registerGraphicDataGenerator(GraphicDataGenerator graphicDataGenerator) {
		graphicDataGenerators.put(graphicDataGenerator.graphicType(), graphicDataGenerator);
	}
}
