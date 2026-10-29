# Tareas de carga manual

- [x] Implementar adaptador de ejecución explícita.
- [x] Mantener arranque normal sin sincronización.
- [x] Documentar el comando y sus efectos sobre la base local.
- [x] Agregar pruebas de resultado, errores y recorrido completo.
- [x] Reproducir y corregir la conversión de zona horaria en horarios locales.
- [x] Completar clean verify.
- [x] Vincular issue #17 y rama `17-habilitar-la-carga-manual-del-catalogo`.
- [ ] Publicar PR para revisión docente.
- [x] Comprobar la carga contra la cátedra en el entorno de desarrollo.

Verificación del 27 de octubre de 2026: `clean verify` terminó con BUILD SUCCESS; 81 pruebas aprobadas. La regresión de horarios falló antes de la corrección y pasó después. La carga real con tiempo máximo de lectura de 30s finalizó con `snapshot applied`; MySQL confirmó versión 1, 8 categorías, 24 profesionales y 44 horarios. Hubo fallos de red transitorios en intentos previos. No se modificaron datos del servidor de la cátedra.
