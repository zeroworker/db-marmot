package db.marmot.statistical.sharding;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.api.sharding.complex.ComplexKeysShardingValue;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;

/**
 * @author shaokang
 */
public class StatisticalDataShardingAlgorithm implements ComplexKeysShardingAlgorithm<String> {
	
	@Override
	public Collection<String> doSharding(Collection<String> availableTargetNames, ComplexKeysShardingValue<String> shardingValue) {
		List<String> routeTables = Lists.newArrayList();
		for (String each : availableTargetNames) {
			StringBuilder builder = new StringBuilder();
			builder.append(shardingValue.getColumnNameAndShardingValuesMap().get("model_name").iterator().next());
			String availableTargetName = shardingValue.getLogicTableName() + "_" + Math.abs(builder.toString().hashCode() % 1024);
			if (each.equals(availableTargetName)) {
				routeTables.add(each);
			}
		}
		if (CollectionUtils.isEmpty(routeTables)) {
			throw new IllegalArgumentException(String.format("未找到表[%s]分片", shardingValue.getLogicTableName()));
		}
		return routeTables;
	}
}
