package db.marmot.statistical;

import java.util.List;
import java.util.Map;

import org.springframework.dao.DuplicateKeyException;

import com.alibaba.druid.sql.builder.SQLBuilderFactory;
import com.alibaba.druid.sql.builder.SQLSelectBuilder;
import db.marmot.enums.TemplateType;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceTemplate;
import db.marmot.repository.RepositoryException;
import db.marmot.statistical.generator.memory.TemporaryMemory;
import db.marmot.volume.Database;
import db.marmot.volume.DatabaseTemplate;

/**
 * @author shaokang
 */
public class StatisticalRepository extends DataSourceRepository {
	
	private DatabaseTemplate databaseTemplate;
	private StatisticalTemplate statisticalTemplate;
	
	public StatisticalRepository(Map<TemplateType, DataSourceTemplate> templates) {
		super(templates);
		this.databaseTemplate = getTemplate(TemplateType.database);
		this.statisticalTemplate = getTemplate(TemplateType.statistical);
	}
	
	/**
	 * 保存统计模型
	 */
	public void storeStatisticalModel(StatisticalModel statisticalModel) {
		if (statisticalModel == null) {
			throw new RepositoryException("统计模型不能为空");
		}
		Database database = databaseTemplate.findDatabase(statisticalModel.getDbName());
		if (database == null) {
			throw new RepositoryException(String.format("数据源%不存在", statisticalModel.getModelName()));
		}
		statisticalModel.validateStatisticalModel(database);
		
		try {
			SQLSelectBuilder sqlSelectBuilder = SQLBuilderFactory.createSelectSQLBuilder(statisticalModel.getFetchSql(), database.getDbType()).limit(1);
			databaseTemplate.queryData(statisticalModel.getDbName(), sqlSelectBuilder.toString());
		} catch (Exception e) {
			throw new RepositoryException(String.format("统计模型%s fetch sql 验证异常", statisticalModel.getModelName(), e));
		}
		
		try {
			statisticalTemplate.storeStatisticalModel(statisticalModel);
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
		StatisticalModel statisticalModel = statisticalTemplate.findStatisticalModel(modelName);
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
		return statisticalTemplate.findStatisticalModelByStatus(true, false);
	}
	
	/**
	 * 更新统计模型为计算中状态
	 * @param statisticalModel
	 */
	public void updateStatisticalModelCalculateIng(StatisticalModel statisticalModel) {
		
		StatisticalModel originalStatisticalModel = statisticalTemplate.loadStatisticalModel(statisticalModel.getModelId());
		if (originalStatisticalModel == null) {
			throw new RepositoryException(String.format("统计模型%s不存在", statisticalModel.getModelName()));
		}
		if (!originalStatisticalModel.isCalculated()) {
			throw new RepositoryException(String.format("统计模型%s未计算完成", statisticalModel.getModelName()));
		}
		statisticalModel.setCalculated(false);
		statisticalTemplate.updateStatisticalModel(statisticalModel);
	}
	
	/**
	 * 更新统计模型为计算中状态
	 * @param statisticalModel
	 */
	public void updateStatisticalModelCalculated(StatisticalModel statisticalModel, TemporaryMemory temporaryMemory) {
		
		StatisticalModel originalStatisticalModel = statisticalTemplate.loadStatisticalModel(statisticalModel.getModelId());
		if (originalStatisticalModel == null) {
			throw new RepositoryException(String.format("统计模型%s不存在", statisticalModel.getModelName()));
		}
		
		if (!originalStatisticalModel.isCalculated()) {
			statisticalModel.setCalculated(true);
			statisticalTemplate.updateStatisticalModel(statisticalModel);
		}
		
		if (temporaryMemory.hashMemoryStatistics()) {
			temporaryMemory.getMemoryStatistics().values().forEach(data -> {
				if (statisticalTemplate.findStatisticalData(data.getModelName(), data.getRowKey()) == null) {
					statisticalTemplate.storeStatisticalData(data);
					return;
				}
				statisticalTemplate.updateStatisticalData(data);
			});
		}
		
		if (temporaryMemory.hashMemoryDistinct()) {
			temporaryMemory.getMemoryDistinct().values().forEach(distinct -> {
				if (statisticalTemplate.findStatisticalDistinct(distinct.getRowKey(), distinct.getDistinctColumn()) == null) {
					statisticalTemplate.storeStatisticalDistinct(distinct);
					return;
				}
				statisticalTemplate.updateStatisticalDistinct(distinct);
			});
		}
		
		if (temporaryMemory.hashThisTask()) {
			StatisticalTask statisticalTask = statisticalTemplate.findStatisticalTask(temporaryMemory.getThisTask().getModelName());
			if (temporaryMemory.hashNextTask()) {
				statisticalTemplate.deleteStatisticalTask(statisticalTask.getTaskId());
			} else {
				statisticalTask.setScanned(true);
				statisticalTemplate.updateStatisticalTask(statisticalTask);
			}
		}
		
		if (temporaryMemory.hashNextTask()) {
			statisticalTemplate.storeStatisticalTask(temporaryMemory.getNextTask());
		}
	}
	
	/**
	 * 获取统计数据
	 * @param modelName
	 * @param rowKey
	 * @return
	 */
	public StatisticalData findStatisticalData(String modelName, String rowKey) {
		return statisticalTemplate.findStatisticalData(modelName, rowKey);
	}
	
	/**
	 *
	 * @param modelName
	 * @param rowKeys
	 * @return
	 */
	public List<StatisticalData> findStatisticalData(String modelName, List<String> rowKeys) {
		return statisticalTemplate.findStatisticalData(modelName, rowKeys);
	}
	
	/**
	 * 获取统计任务
	 * @param modelName
	 * @return
	 */
	public StatisticalTask findStatisticalTask(String modelName) {
		return statisticalTemplate.findStatisticalTask(modelName);
	}
	
	/**
	 * 获取去重数据
	 * @param rowKey
	 * @param distinctColumn
	 * @return
	 */
	public StatisticalDistinct findStatisticalDistinct(String rowKey, String distinctColumn) {
		return statisticalTemplate.findStatisticalDistinct(rowKey, distinctColumn);
	}
}
