package db.marmot.statistical;

import com.alibaba.druid.sql.builder.SQLBuilderFactory;
import com.alibaba.druid.sql.builder.SQLSelectBuilder;
import db.marmot.graphic.GraphicRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.volume.Database;
import org.springframework.dao.DuplicateKeyException;

import java.util.List;

/**
 * @author shaokang
 */
public class StatisticalRepository extends GraphicRepository {
	
	public StatisticalRepository(DataSourceTemplate dataSourceTemplate) {
		super(dataSourceTemplate);
	}
	
	/**
	 * 保存统计模型
	 */
	public void storeStatisticalModel(StatisticalModel statisticalModel) {
		if (statisticalModel == null) {
			throw new RepositoryException("统计模型不能为空");
		}
		
		Database database = dataSourceTemplate.findDatabase(statisticalModel.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%不存在", statisticalModel.getModelName()));
		}
		statisticalModel.validateStatisticalModel(database);
		
		try {
			SQLSelectBuilder sqlSelectBuilder = SQLBuilderFactory.createSelectSQLBuilder(statisticalModel.getFetchSql(), database.getDbType()).limit(1);
			dataSourceTemplate.queryData(statisticalModel.getDbName(), sqlSelectBuilder.toString());
		} catch (Exception e) {
			throw new RepositoryException(String.format("统计模型%s fetch sql 验证异常", statisticalModel.getModelName(), e));
		}
		
		try {
			dataSourceTemplate.storeStatisticalModel(statisticalModel);
		} catch (DuplicateKeyException e) {
			throw new RepositoryException(String.format("重复统计模型%s", statisticalModel.getModelName()));
		}
	}
	
	/**
	 * 获取模型
	 * @param modelName
	 * @return
	 */
	public StatisticalModel findStatisticalModel(String modelName) {
		StatisticalModel statisticalModel = dataSourceTemplate.findStatisticalModel(modelName);
		if (statisticalModel == null) {
			throw new RepositoryException(String.format("统计模型%s不存在", statisticalModel.getModelName()));
		}
		return statisticalModel;
	}
	
	/**
	 * 获取可做统计计算的模型
	 * @return
	 */
	public List<StatisticalModel> findNormalStatisticalModels() {
		return dataSourceTemplate.findStatisticalModelByStatus(true, false);
	}
	
	/**
	 * 更新统计模型为计算中状态
	 * @param statisticalModel
	 */
	public void updateStatisticalModelCalculateIng(StatisticalModel statisticalModel) {
		StatisticalModel originalStatisticalModel = dataSourceTemplate.loadStatisticalModel(statisticalModel.getModelId(), true);
		if (originalStatisticalModel == null) {
			throw new RepositoryException(String.format("统计模型%s不存在或者未计算完成", statisticalModel.getModelName()));
		}
		statisticalModel.setCalculated(false);
		dataSourceTemplate.updateStatisticalModel(statisticalModel);
	}
	
	/**
	 * 更新统计模型为计算中状态
	 * @param statisticalModel
	 */
	public void updateStatisticalModelCalculated(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		StatisticalModel originalStatisticalModel = dataSourceTemplate.loadStatisticalModel(statisticalModel.getModelId(), false);
		if (originalStatisticalModel == null) {
			throw new RepositoryException(String.format("未获取到计算中模型%s", statisticalModel.getModelName()));
		}
		
		if (!originalStatisticalModel.isCalculated()) {
			statisticalModel.setCalculated(true);
			dataSourceTemplate.updateStatisticalModel(statisticalModel);
			
		}
		if (temporaryMemory.hashMemoryStatistics()) {
			temporaryMemory.getMemoryStatistics().values().forEach(data -> {
				if (dataSourceTemplate.findStatisticalData(data.getModelName(), data.getRowKey()) == null) {
					dataSourceTemplate.storeStatisticalData(data);
					return;
				}
				dataSourceTemplate.updateStatisticalData(data);
			});
		}
		
		if (temporaryMemory.hashMemoryDistinct()) {
			temporaryMemory.getMemoryDistinct().values().forEach(distinct -> {
				if (dataSourceTemplate.findStatisticalDistinct(distinct.getRowKey(), distinct.getDistinctColumn()) == null) {
					dataSourceTemplate.storeStatisticalDistinct(distinct);
					return;
				}
				dataSourceTemplate.updateStatisticalDistinct(distinct);
			});
		}
		
		if (temporaryMemory.hashThisTask()) {
			StatisticalTask statisticalTask = dataSourceTemplate.findStatisticalTask(temporaryMemory.getThisTask().getModelName());
			if (temporaryMemory.hashNextTask()) {
				dataSourceTemplate.deleteStatisticalTask(statisticalTask.getTaskId());
			} else {
				statisticalTask.setScanned(true);
				dataSourceTemplate.updateStatisticalTask(statisticalTask);
			}
		}
		
		if (temporaryMemory.hashNextTask()) {
			dataSourceTemplate.storeStatisticalTask(temporaryMemory.getNextTask());
		}
	}
	
	/**
	 * 获取统计数据
	 * @param modelName
	 * @param rowKey
	 * @return
	 */
	public StatisticalData findStatisticalData(String modelName, String rowKey) {
		return dataSourceTemplate.findStatisticalData(modelName, rowKey);
	}
	
	/**
	 *
	 * @param modelName
	 * @param rowKeys
	 * @return
	 */
	public List<StatisticalData> findStatisticalData(String modelName, List<String> rowKeys) {
		return dataSourceTemplate.findStatisticalData(modelName, rowKeys);
	}
	
	/**
	 * 获取统计任务
	 * @param modelName
	 * @return
	 */
	public StatisticalTask findStatisticalTask(String modelName) {
		return dataSourceTemplate.findStatisticalTask(modelName);
	}
	
	/**
	 * 获取去重数据
	 * @param rowKey
	 * @param distinctColumn
	 * @return
	 */
	public StatisticalDistinct findStatisticalDistinct(String rowKey, String distinctColumn) {
		return dataSourceTemplate.findStatisticalDistinct(rowKey, distinctColumn);
	}
}
