package db.marmot.test;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author shaokang
 */
@SpringBootApplication
public class Application {
	
	public static void main(String[] args) {
		new SpringApplication(Application.class).run(args);
	}
}
