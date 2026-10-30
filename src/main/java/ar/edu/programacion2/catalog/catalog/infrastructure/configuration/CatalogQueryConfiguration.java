package ar.edu.programacion2.catalog.catalog.infrastructure.configuration;

import ar.edu.programacion2.catalog.catalog.application.port.in.ListEnabledCategories;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogCategoryReader;
import ar.edu.programacion2.catalog.catalog.application.service.ListEnabledCategoriesService;
import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalsByCategory;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogProfessionalReader;
import ar.edu.programacion2.catalog.catalog.application.service.ListProfessionalsByCategoryService;
import ar.edu.programacion2.catalog.catalog.application.port.in.ListProfessionalSchedules;
import ar.edu.programacion2.catalog.catalog.application.port.out.CatalogScheduleReader;
import ar.edu.programacion2.catalog.catalog.application.service.ListProfessionalSchedulesService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CatalogQueryConfiguration {
    @Bean
    ListProfessionalSchedules listProfessionalSchedules(CatalogScheduleReader reader) {
        return new ListProfessionalSchedulesService(reader);
    }

    @Bean
    ListProfessionalsByCategory listProfessionalsByCategory(CatalogProfessionalReader reader) {
        return new ListProfessionalsByCategoryService(reader);
    }

    @Bean
    ListEnabledCategories listEnabledCategories(CatalogCategoryReader reader) {
        return new ListEnabledCategoriesService(reader);
    }
}
