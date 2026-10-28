# Plan de carga manual

1. Agregar un adaptador ApplicationRunner activado exclusivamente por opción explícita.
2. Mantener desactivada la ejecución en la configuración predeterminada.
3. Informar resultado sin credenciales ni contenido remoto.
4. Probar la aplicación completa con HTTP local y MySQL temporal.
5. Documentar el comando para una carga local controlada.
6. Corregir el mapeo de horas locales descubierto durante la carga real y agregar una prueba de regresión.

Rama: `17-habilitar-la-carga-manual-del-catalogo`. Issue: [#17](https://github.com/mmachinea-pr2/turnos-catalog-service/issues/17).

Depende del caso de uso del issue #15, cuyos cambios se incorporaron a esta rama porque todavía no estaban integrados en main. Integrar primero ese PR para revisar este incremento por separado. El PR se dirigirá a main para revisión docente.
