# Plan

Rama: `47-recuperar-catalogo-ante-notif-perdidas`. PR hacia `main`.

1. Preparar una tarea de comprobación con control de concurrencia, manejo seguro de fallos y pruebas.
2. Configurar el temporizador opcional, las variables externas, compatibilidad con modos manuales y pruebas de configuración y ciclo de vida.

La rama parte de `116afcf`; la tarea reutiliza la recuperación integrada en #43. El PR de Kafka todavía no está integrado en esta base. Al integrar ambos, conservar sus configuraciones y validar el conjunto.

Integración: incorporado `origin/main` en `3bc34d4`, incluyendo el consumidor Kafka. Se conservan las variables de ambos mecanismos y los comandos manuales desactivan ambos explícitamente.
