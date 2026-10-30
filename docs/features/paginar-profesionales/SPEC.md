# Paginar la consulta de profesionales

## Contrato

El endpoint conserva el array JSON y los filtros categoryId y name. Se agregan parámetros opcionales:

- `page`: índice desde cero; predeterminado 0.
- `size`: cantidad máxima por página; predeterminado 20, rango 1 a 100.

Ejemplo:

```text
GET /api/catalog/professionals?categoryId=1&name=ana&page=0&size=10
```

La cabecera `X-Total-Count` informa el total de profesionales que coinciden con categoría, nombre y estados habilitados, no solo la cantidad en esa página. Permite calcular páginas con el size solicitado. Una página fuera de los resultados devuelve HTTP 200 con `[]` y conserva el total; sin coincidencias, el total es cero.

Se mantiene el orden por apellido, nombre e ID, que permite desempatar registros con nombres iguales. La paginación por desplazamiento no garantiza una vista inmutable entre peticiones si se sincroniza el catálogo entre ellas.

## Validación

Page negativo, size fuera de 1–100, texto no numérico o valores fuera de int producen HTTP 400. También se rechaza un desplazamiento page × size mayor que Integer.MAX_VALUE, límite de la consulta JPA. La aplicación valida estos límites sin depender de Spring.

## Arquitectura y compatibilidad

PageQuery y ProfessionalPage son tipos propios de aplicación sin dependencias de Spring Data. El adaptador JPA traduce la solicitud a PageRequest y devuelve los modelos junto con el total. MySQL limita los resultados: no se carga el listado completo para paginarlo en memoria.

Las consultas existentes sin page ni size reciben ahora como máximo los primeros 20 resultados. El formato de cada profesional no cambia. Un consumidor que necesite todos debe recorrer las páginas. Las categorías y los horarios no cambian en este incremento. JWT sigue pendiente.

## Prueba manual

```bash
curl -i 'http://localhost:8080/api/catalog/professionals?categoryId=1&page=0&size=10'
```

## Pruebas

HTTP contra MySQL temporal con más de una página: valores predeterminados, total filtrado, nombres idénticos sin duplicados ni omisiones al recorrer páginas, última página incompleta, página vacía, size=100, ausencia de coincidencias y rechazo de parámetros inválidos.
