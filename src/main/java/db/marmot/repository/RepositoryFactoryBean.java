/*
 * Copyright (c) 2017-2018 glassescat All Rights Reserved
 */

package db.marmot.repository;

import db.marmot.enums.TemplateType;
import db.marmot.repository.validate.Validators;
import db.marmot.sharding.StatisticalDataShardingAlgorithm;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.shardingjdbc.api.ShardingDataSourceFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.TransactionProxyFactoryBean;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author shaokang
 */
public abstract class RepositoryFactoryBean<T> implements FactoryBean<T>, InitializingBean {
	
	private Class<T> targetType;
	private boolean sharding = false;
	private DataSource dataSource;
	private PlatformTransactionManager transactionManager;
	private Map<TemplateType, DataSourceTemplate> templates;
	private TransactionProxyFactoryBean proxyFactoryBean = new TransactionProxyFactoryBean();
	
	public void setSharding(boolean sharding) {
		this.sharding = sharding;
	}
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	
	public void setTemplates(Map<TemplateType, DataSourceTemplate> templates) {
		this.templates = templates;
	}
	
	public Map<TemplateType, DataSourceTemplate> getTemplates() {
		return templates;
	}
	
	@Override
	public void afterPropertiesSet() {
		
		Validators.notNull(dataSource, "jdbcTemplate 不能为空");
		
		Type factoryBeanType = getClass().getGenericSuperclass();
		if (!(factoryBeanType instanceof ParameterizedType)) {
			throw new RepositoryException("RepositoryFactoryBean 实现过程不可忽略范型");
		}
		this.targetType = (Class<T>) ((ParameterizedType) factoryBeanType).getActualTypeArguments()[0];
		
		if (sharding) {
			try {
				ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
				shardingRuleConfig.getBindingTableGroups().add("statistical_data");
				TableRuleConfiguration statisticsDataTableRuleConfiguration = new TableRuleConfiguration("statistical_data", "statistics_data_$->{0..1023}");
				StatisticalDataShardingAlgorithm statisticalDataShardingAlgorithm = new StatisticalDataShardingAlgorithm();
				ComplexShardingStrategyConfiguration statisticsDataStandardShardingStrategyConfiguration = new ComplexShardingStrategyConfiguration("model_name", statisticalDataShardingAlgorithm);
				shardingRuleConfig.setDefaultTableShardingStrategyConfig(statisticsDataStandardShardingStrategyConfiguration);
				statisticsDataTableRuleConfiguration.setTableShardingStrategyConfig(statisticsDataStandardShardingStrategyConfiguration);
				shardingRuleConfig.getTableRuleConfigs().add(statisticsDataTableRuleConfiguration);
				Map<String, DataSource> dataSourceMap = new HashMap<>();
				dataSourceMap.put("shardingDataSource", dataSource);
				dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, null);
			} catch (SQLException e) {
				throw new RepositoryException("创建分表数据源异常", e);
			}
		}
		
		this.transactionManager = new DataSourceTransactionManager(dataSource);
		Properties transactionAttributes = new Properties();
		try {
			transactionAttributes.load(new StringReader("store*=PROPAGATION_REQUIRED\n" + "delete*=PROPAGATION_REQUIRED\n" + "update*=PROPAGATION_REQUIRED\n" + "load*=PROPAGATION_REQUIRED\n" + "*=PROPAGATION_NOT_SUPPORTED,readOnly\n"));
		} catch (IOException e) {
			throw new RepositoryException("初始化事务参数异常", e);
		}
		
		proxyFactoryBean.setTransactionAttributes(transactionAttributes);
		proxyFactoryBean.setTarget(newInstance());
		proxyFactoryBean.setProxyTargetClass(true);
		proxyFactoryBean.setTransactionManager(transactionManager);
		proxyFactoryBean.afterPropertiesSet();
	}
	
	public abstract T newInstance();
	
	@Override
	public T getObject() {
		return (T) proxyFactoryBean.getObject();
	}
	
	@Override
	public Class<T> getObjectType() {
		return targetType;
	}
	
	@Override
	public boolean isSingleton() {
		return true;
	}
	
}
