# Plan de decisión de sincronización

1. Definir las acciones posibles como un modelo de aplicación.
2. Implementar el caso de uso con los lectores local y remoto.
3. Registrar el caso de uso cuando Redis esté habilitado.
4. Probar los límites y la propagación de fallos.
5. Documentar y verificar el proyecto completo.

Rama: `33-determinar-la-estrategia-de-sincronizacion-del-catalogo`. Issue: [#33](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/33). Milestone: Persistencia y sincronización del catálogo. Base: `7bcf56a`, lectura de versiones Redis (#31). El PR se dirigirá a main después de integrar el avance anterior.
