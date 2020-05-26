package db.marmot.test;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author shaokang
 */
@Configuration
public class TestConfiguration {
	
	@Bean(initMethod = "init", destroyMethod = "close")
	@ConfigurationProperties(prefix = "datasource.master")
	public DruidDataSource dataSource() {
		return new DruidDataSource();
	}
}
