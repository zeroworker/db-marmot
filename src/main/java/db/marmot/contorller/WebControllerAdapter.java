package db.marmot.contorller;

import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author shaokang
 */
public abstract class WebControllerAdapter implements ApplicationContextAware, InitializingBean {
	
	private ApplicationContext applicationContext;
	
	@Override
	public void afterPropertiesSet() {
		DefaultListableBeanFactory factory = (DefaultListableBeanFactory) applicationContext.getAutowireCapableBeanFactory();
		Map<String, Class> controllers = getController();
		if (controllers != null && controllers.size() > 0) {
			controllers.forEach((path, controllerClass) -> {
				factory.registerBeanDefinition(path, createControllerBeanDefinition(controllerClass));
				factory.getBean(controllerClass);
			});
		}
	}
	
	private RootBeanDefinition createControllerBeanDefinition(Class controllerClass) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(controllerClass);
		beanDefinition.setScope(BeanDefinition.SCOPE_SINGLETON);
		return beanDefinition;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
	
	/**
	 * 获取controller
	 * @return
	 */
	protected abstract Map<String, Class> getController();
}
