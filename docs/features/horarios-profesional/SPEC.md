# Consultar horarios semanales de un profesional

## Contrato

`GET /api/catalog/professionals/{professionalId}/schedules` devuelve HTTP 200 y un array de horarios semanales habilitados. También deben estar habilitados el profesional y su categoría. La consulta usa únicamente la copia MySQL local.

Campos de cada horario: `id`, `professionalId`, `dayOfWeek`, `startTime`, `endTime` y `slotDurationMinutes`. Las horas son locales de atención, sin zona horaria. No se exponen campos de auditoría ni entidades JPA.

El orden es lunes a domingo, hora de inicio, hora de fin e ID ascendentes. Se conservan los intervalos originales, incluso si coinciden o se superponen; no se generan slots ni se calculan ocupaciones.

Un ID debe ser entero positivo representable como long; valores inválidos reciben HTTP 400. Un profesional inexistente, deshabilitado, de categoría deshabilitada o sin horarios habilitados recibe HTTP 200 con `[]`. Este contrato no distingue esas causas.

## Ejemplo ficticio

```json
[
  {"id": 21, "professionalId": 15, "dayOfWeek": "MONDAY", "startTime": "09:00:00", "endTime": "12:00:00", "slotDurationMinutes": 30}
]
```

## Arquitectura y límites

WeeklyScheduleController usa el puerto ListProfessionalSchedules y un DTO propio. El servicio ordena los intervalos de la agenda semanal usando DayOfWeek y LocalTime. CatalogScheduleReader filtra en MySQL por profesional y por estado habilitado de horario, profesional y categoría, dentro de una transacción de solo lectura.

La consulta no depende del cliente de cátedra. La seguridad JWT sigue pendiente. Estos horarios indican atención habitual, no turnos libres: la disponibilidad requiere combinar la agenda con ocupaciones y holds de la cátedra en el flujo de reservas.

## Uso local

Con el backend iniciado, elegir un ID obtenido del listado de profesionales:

```bash
curl http://localhost:8080/api/catalog/professionals/15/schedules
```

## Pruebas

Pruebas HTTP sobre MySQL temporal verifican orden semanal y desempates, formato y campos de respuesta, horas nocturnas, duración, exclusión de bajas lógicas en las tres entidades, resultados vacíos y errores de parámetros. Los datos son ficticios.
