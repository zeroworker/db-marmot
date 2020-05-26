package db.marmot.graphic.generator;

import db.marmot.enums.VolumeType;
import db.marmot.graphic.Graphic;
import db.marmot.graphic.generator.procedure.GraphicProcedure;
import db.marmot.repository.DataSourceRepository;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.DataVolume;
import db.marmot.volume.generator.ColumnGeneratorAdapter;
import org.springframework.core.Ordered;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * @author shaokang
 */
public abstract class AbstractGraphicDataGenerator<G extends Graphic, D extends GraphicData> implements GraphicDataGenerator<G, D> {
	
	private List<GraphicProcedure> graphicProcedures;
	
	public AbstractGraphicDataGenerator(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		graphicProcedures = getGraphicProcedure(dataSourceRepository, columnGeneratorAdapter, statisticalGenerateAdapter);
		graphicProcedures.sort(Comparator.comparingInt(Ordered::getOrder));
	}
	
	@Override
	public D getGraphicData(G graphic, DataVolume dataVolume) {
		D graphicData = newInstanceGraphicData(graphic, dataVolume);
		graphicData.setGraphicType(graphicType());
		processedProcedure(graphicProcedures.iterator(), graphic, dataVolume, graphicData);
		return graphicData;
	}
	
	/**
	 * 执行图表数据生成过程
	 * @param procedures
	 * @param graphic
	 * @param dataVolume
	 * @param graphicData
	 */
	private void processedProcedure(Iterator<GraphicProcedure> procedures, G graphic, DataVolume dataVolume, D graphicData) {
		if (procedures.hasNext()) {
			GraphicProcedure procedure = procedures.next();
			if (procedure.match(graphic, dataVolume)) {
				procedure.processed(graphic, dataVolume, graphicData);
			}
			if (graphicData.emptyData()) {
				graphicData.setGraphicMemo(dataVolume.getVolumeType() == VolumeType.model ? "数据预热中 建议保存仪表盘,因需要对历史数据统计,相对耗时很长 在此期间所有统计结果为零,请耐心等待" : "未获取到数据");
				return;
			}
			processedProcedure(procedures, graphic, dataVolume, graphicData);
		}
	}
	
	/**
	 * 初始化图表数据
	 * @param graphic 图表
	 * @param dataVolume 数据集
	 * @return
	 */
	protected abstract D newInstanceGraphicData(G graphic, DataVolume dataVolume);
	
	/**
	 * 注册图表生成器
	 * @param dataSourceRepository 数据源仓储
	 * @param columnGeneratorAdapter 字段数据生成适配器
	 * @param statisticalGenerateAdapter 字段数据生成适配器
	 */
	protected abstract List<GraphicProcedure> getGraphicProcedure(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter, StatisticalGenerateAdapter statisticalGenerateAdapter);
	
}
