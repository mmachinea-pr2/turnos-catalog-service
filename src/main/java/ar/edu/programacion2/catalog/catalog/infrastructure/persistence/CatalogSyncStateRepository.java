package ar.edu.programacion2.catalog.catalog.infrastructure.persistence;

import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface CatalogSyncStateRepository extends JpaRepository<CatalogSyncStateEntity, Integer> {
    // Solo se usa dentro de una transacción para serializar las sincronizaciones.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select state from CatalogSyncStateEntity state where state.id = 1")
    Optional<CatalogSyncStateEntity> findForUpdate();
}
