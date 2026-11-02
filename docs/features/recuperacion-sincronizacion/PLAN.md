# Plan de recuperación y ejecución

Una issue y varios commits revisables:

1. `feat: recuperar catalogo por snapshot cuando sea necesario`: conectar el coordinador incremental con el snapshot, registrar el caso de uso y probar el flujo.
2. `feat: ejecutar flujo completo de sincronizacion manual`: agregar una ejecución explícita, manejo de resultados y documentación de uso.

Rama: `43-completar-recuperacion-y-ejecucion-manual`. Issue: [#43](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/43). Base: `00f03be`, coordinación incremental (#41). Milestone: Persistencia y sincronización del catálogo. PR hacia main para revisión docente después de integrar los avances anteriores.
