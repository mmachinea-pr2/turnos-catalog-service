# Plan de lectura de cambios Redis

1. Definir el modelo inmutable de IDs afectados y el puerto de lectura.
2. Leer la clave de una versión con el cliente Redis existente.
3. Distinguir ausencia de versión, cambios vacíos y errores.
4. Validar JSON, versión y listas sin conversiones silenciosas.
5. Probar con Redis temporal y documentar.

Rama: `35-leer-cambios-por-version-redis`. Issue: [#35](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/35). Base: `1236057`, estrategia de sincronización (#33). Milestone: Persistencia y sincronización del catálogo. El PR irá a main después de integrar los avances previos.
