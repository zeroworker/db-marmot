package db.marmot.boot;

import db.marmot.contorller.WebTokenAuthorize;
import db.marmot.graphic.contorller.DashboardControllerAdapter;
import db.marmot.graphic.contorller.GraphicDataControllerAdapter;
import db.marmot.graphic.contorller.GraphicDownloadControllerAdapter;
import db.marmot.graphic.download.GraphicDataDownloadAdapter;
import db.marmot.graphic.download.GraphicDownloadAdapter;
import db.marmot.graphic.generator.GraphicDataGeneratorAdapter;
import db.marmot.graphic.generator.GraphicGeneratorAdapter;
import db.marmot.repository.DataSourceRepository;
import db.marmot.repository.DataSourceRepositoryFactoryBean;
import db.marmot.statistical.contorller.StatisticalControllerAdapter;
import db.marmot.statistical.generator.StatisticalDataGenerateAdapter;
import db.marmot.statistical.generator.StatisticalGenerateAdapter;
import db.marmot.volume.controller.VolumeControllerAdapter;
import db.marmot.volume.generator.ColumnDataGeneratorAdapter;
import db.marmot.volume.generator.ColumnGeneratorAdapter;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

/**
 * @author shaokang
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(MarmotProperties.class)
@AutoConfigureOrder(value = Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnProperty(value = "db.marmot.enable", matchIfMissing = true)
public class MarmotAutoConfiguration extends WebMvcConfigurerAdapter {
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new WebTokenInterceptor()).addPathPatterns("/marmot/**");
	}
	
	@Bean
	public DataSourceRepositoryFactoryBean dataSourceRepositoryFactoryBean(MarmotProperties properties, DataSource dataSource) {
		return new DataSourceRepositoryFactoryBean(properties.isSharding(), dataSource);
	}
	
	@Bean
	public ColumnGeneratorAdapter columnGeneratorAdapter(DataSourceRepository dataSourceRepository) {
		return new ColumnDataGeneratorAdapter(dataSourceRepository);
	}
	
	@Bean
	public StatisticalGenerateAdapter statisticalGenerateAdapter(MarmotProperties properties, DataSourceRepository dataSourceRepository) {
		return new StatisticalDataGenerateAdapter(properties.getModelThreadSize(), properties.getModelReviseDelay(), dataSourceRepository);
	}
	
	@Bean
	public GraphicGeneratorAdapter graphicGeneratorAdapter(	DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter,
															StatisticalGenerateAdapter statisticalGenerateAdapter) {
		return new GraphicDataGeneratorAdapter(dataSourceRepository, columnGeneratorAdapter, statisticalGenerateAdapter);
	}
	
	@Bean
	public GraphicDownloadAdapter graphicDownloadAdapter(MarmotProperties properties, DataSourceRepository dataSourceRepository, GraphicGeneratorAdapter graphicGeneratorAdapter) {
		GraphicDownloadAdapter graphicDownloadAdapter = new GraphicDataDownloadAdapter(dataSourceRepository, graphicGeneratorAdapter);
		graphicDownloadAdapter.setFileUrl(properties.getFileUrl());
		graphicDownloadAdapter.setDownloadNum(properties.getDownloadThreadSize());
		graphicDownloadAdapter.setDownloadUrl(properties.getDownloadUrl());
		return graphicDownloadAdapter;
	}
	
	@Bean
	public VolumeControllerAdapter volumeControllerAdapter(DataSourceRepository dataSourceRepository, ColumnGeneratorAdapter columnGeneratorAdapter) {
		return new VolumeControllerAdapter(dataSourceRepository, columnGeneratorAdapter);
	}
	
	@Bean
	public DashboardControllerAdapter dashboardControllerAdapter(DataSourceRepository dataSourceRepository) {
		return new DashboardControllerAdapter(dataSourceRepository);
	}
	
	@Bean
	public GraphicDataControllerAdapter graphicDataControllerAdapter(GraphicGeneratorAdapter graphicGeneratorAdapter, GraphicDownloadAdapter graphicDownloadAdapter) {
		return new GraphicDataControllerAdapter(graphicDownloadAdapter, graphicGeneratorAdapter);
	}
	
	@Bean
	public GraphicDownloadControllerAdapter graphicDownloadControllerAdapter(DataSourceRepository dataSourceRepository) {
		return new GraphicDownloadControllerAdapter(dataSourceRepository);
	}
	
	@Bean
	public StatisticalControllerAdapter statisticalControllerAdapter(DataSourceRepository dataSourceRepository, StatisticalGenerateAdapter statisticalGenerateAdapter) {
		return new StatisticalControllerAdapter(dataSourceRepository, statisticalGenerateAdapter);
	}
	
	public class WebTokenInterceptor implements HandlerInterceptor {
		@Override
		public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
			try {
				WebTokenAuthorize.verify(request.getHeader("token"));
			} catch (Exception e) {
				response.sendError(401,e.getMessage());
				return false;
			}
			return true;
		}
		
		@Override
		public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
		}
		
		@Override
		public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
		}
	}
	
}
