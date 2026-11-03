package ar.edu.programacion2.catalog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CatalogApplication {

	public static void main(String[] args) {
		var context = SpringApplication.run(CatalogApplication.class, args);
        if (context.getEnvironment().getProperty("catalog.synchronization.full-run-on-startup", Boolean.class, false)) {
            context.close();
        }
	}

}
