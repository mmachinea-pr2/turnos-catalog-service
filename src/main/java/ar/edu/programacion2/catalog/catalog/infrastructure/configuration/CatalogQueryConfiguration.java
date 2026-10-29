package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListEnabledCategories;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogCategoryReader;
import ar.edu.programacion2.catalog.catalog.application.service.ListEnabledCategoriesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CatalogQueryConfiguration {
    @Bean
    ListEnabledCategories listEnabledCategories(CatalogCategoryReader reader) {
        return new ListEnabledCategoriesService(reader);
    }
}
