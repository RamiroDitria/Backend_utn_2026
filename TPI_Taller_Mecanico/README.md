# Taller Mecánico — Backend

Backend de gestión interna de un taller mecánico, hecho con microservicios en Java/Spring Boot.
Cubre el recorrido completo de una orden de trabajo (OT): ingreso del vehículo, diagnóstico (VIN +
vPIC), presupuesto, decisión del cliente, pedido de repuestos, reparación y entrega, avisando al
cliente por WhatsApp en cada etapa clave.

## Arquitectura

```
Jefe de taller ─┐                              ┌─ Keycloak (OIDC, roles)
Mecánico ───────┼──► API Gateway :8080 ─────────┤
                 │     (valida JWT y rutea)     │
                 │                              │
                 ├──► ms-clientes-vehiculos:8082 ──► NHTSA vPIC API
                 │      (clientes, vehículos, VIN)
                 │
                 ├──► ms-ordenes:8083 ─────┬──► ms-clientes-vehiculos (consulta vehículo)
                 │      (OT, presupuestos, │
                 │       repuestos, reglas)└──► ms-notificaciones:8084 ──► WhatsApp Cloud API
                 │
                 └──► ms-notificaciones:8084

Cada microservicio tiene su propia base PostgreSQL (clientes_vehiculos, ordenes, notificaciones).
```

- **api-gateway**: único punto de entrada, valida el JWT y rutea.
- **ms-clientes-vehiculos**: clientes, vehículos por patente, integración con vPIC.
- **ms-ordenes**: el corazón del dominio — OT, estados, desperfectos, repuestos, presupuestos,
  decisiones y entrega. Acá vive la mayoría de las reglas de negocio (RN-01 a RN-10).
- **ms-notificaciones**: arma y envía los WhatsApp (o los simula en el log), guarda el historial y
  reintenta los que fallan.
- **taller-commons**: librería compartida (manejo de errores, roles de Keycloak, validadores de
  patente y VIN).

## 1. Requisitos previos

| Herramienta | Versión | Para qué |
|---|---|---|
| **JDK** | **17** (exacto) | Compilar y correr los 4 servicios |
| Maven | 3.8+ | Compilar |
| Docker + Docker Compose | reciente | Levantar Postgres, Keycloak y los servicios |

### Ojo con la versión de JDK

Los `pom.xml` piden Java 17 y usamos Lombok, que no anda bien con JDK 21 o más nuevo. Si tenés
instalado solo un JDK más nuevo, `mvn compile` va a fallar con errores raros tipo
`cannot find symbol: method getXxx()` — no es un error del código, es que Lombok necesita JDK 17.

Fijate qué JDKs tenés instalados:

```bash
/usr/libexec/java_home -V        # macOS
```

Si no hay un `17.x`, instalalo:

```bash
brew install openjdk@17
```

Y compilá siempre con ese JDK activo:

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # tiene que decir 17.x
```

## 2. Puesta en marcha

### 2.1 Variables de entorno

```bash
cp .env.example .env
```

Con los valores por defecto alcanza para levantar todo en local. `WHATSAPP_ENABLED=false` hace que
los avisos se **simulen en el log** de `ms-notificaciones` (no hace falta cuenta de Meta para probar
el flujo completo).

### 2.2 Compilar (con JDK 17 activo)

```bash
./build.sh        # macOS/Linux
build.cmd         # Windows
```

Instala `taller-commons` en el `.m2` local y empaqueta los 4 servicios.

### 2.3 Levantar todo

```bash
docker compose up --build
```

Levanta, en este orden: PostgreSQL 16 (con las 3 bases creadas por `postgres/init.sql`), Keycloak 24
(importando `keycloak/realm-taller.json` con roles y usuarios de prueba) y los 4 servicios Java.
Esperá a ver los 4 `Started ...Application` en el log.

### 2.4 Verificar que levantó

| Servicio | URL |
|---|---|
| API Gateway (punto de entrada) | http://localhost:8080 |
| Keycloak (consola admin: `admin`/`admin`) | http://localhost:8081 |
| Swagger ms-clientes-vehiculos | http://localhost:8082/swagger-ui.html |
| Swagger ms-ordenes | http://localhost:8083/swagger-ui.html |
| Swagger ms-notificaciones | http://localhost:8084/swagger-ui.html |
| PostgreSQL | localhost:5433 (`taller`/`taller`) |

> Los puertos 8082-8084 están expuestos solo para ver el Swagger de cada servicio; en uso normal
> todo pasa por el gateway (8080). Postgres está en el **5433** del host (no 5432) para no chocar
> con una instancia de Postgres que ya tengas instalada local.

### 2.5 Ver logs

```bash
docker compose logs -f              # todos los servicios, en vivo
docker compose logs -f ms-ordenes   # solo uno
```

### 2.6 Apagar / limpiar

```bash
docker compose down        # apaga y conserva los datos
docker compose down -v     # apaga y borra también la base de datos (vuelve todo a cero)
```

## 3. Usuarios y roles de prueba

Vienen precargados en `keycloak/realm-taller.json` (realm `taller`, cliente público `taller-api`):

| Usuario | Password | Rol |
|---|---|---|
| `jefe` | `jefe123` | `JEFE_TALLER` |
| `mecanico1` | `mecanico123` | `MECANICO` |
| `mecanico2` | `mecanico123` | `MECANICO` |

Para pedir un token a mano:

```bash
curl -s -X POST http://localhost:8081/realms/taller/protocol/openid-connect/token \
  -d grant_type=password -d client_id=taller-api \
  -d username=jefe -d password=jefe123 | jq -r .access_token
```

## 4. Probar el flujo completo con Postman

En `postman/` están la colección y el entorno:

- `Taller-Mecanico.postman_collection.json`
- `Taller-Mecanico.postman_environment.json`

Importá los dos, seleccioná el entorno **"Taller Mecánico - Local"** y corré toda la colección con
el **Collection Runner** ("Run collection"), en orden. No hace falta cargar nada a mano: la primera
carpeta saca los tokens y cada request va guardando en el entorno los ids que usan las siguientes.

Carpetas, en el orden en que hay que correrlas:

1. **Autenticación** — tokens de `jefe`, `mecanico1` y `mecanico2`.
2. **Clientes y vehículos (F01, RN-01)** — alta de cliente y vehículo.
3. **Diagnóstico (F02-F05)** — abrir OT, registrar VIN (consulta real a vPIC), desperfectos y
   repuestos.
4. **Presupuesto y decisión (F06-F07)** — genera el presupuesto y la aceptación del cliente.
5. **Repuestos y reparación (F08)** — pedido y recepción del repuesto; la OT pasa a En reparación.
6. **Trabajo adicional (F09, RN-08)** — falla nueva durante la reparación, se presupuesta aparte.
7. **Entrega (F10-F11)** — finalizar trabajo y registrar la entrega.
8. **Integraciones externas (vPIC)** — un VIN válido pero que vPIC no reconoce, para probar la carga
   manual (`datosVerificados=false`).
9. **Casos negativos** — patente inválida/duplicada, acceso sin token (401), rol incorrecto (403),
   VIN inválido (RN-04), mecánico no asignado (RN-03), pedir repuestos sin aceptación (RN-07).

Cada request tiene asserts (`pm.test`) que chequean el código HTTP, el estado de la OT y el código
de regla de negocio cuando corresponde.

> No se automatizó el caso "presupuesto vencido" (RN-06) porque requiere esperar 7 días reales;
> para probarlo a mano, cambiá `fecha_vencimiento` directo en la tabla `ordenes.presupuestos`, o
> bajá `presupuesto.dias-vigencia` en `ms-ordenes/src/main/resources/application.yml`.

## 5. Mapeo de funcionalidades

| Funcionalidad | Endpoint | Rol | Reglas |
|---|---|---|---|
| F01 Registrar cliente/vehículo | `POST /api/clientes`, `POST /api/vehiculos` | Jefe | RN-01 |
| F02 Abrir OT y asignar mecánico | `POST /api/ordenes` | Jefe | RN-02 |
| F03 Registrar VIN | `POST /api/ordenes/{id}/vin` | Mecánico asignado | RN-03, RN-04 |
| F04 Registrar desperfecto | `POST /api/ordenes/{id}/desperfectos` | Mecánico asignado | RN-03, RN-05 |
| F05 Registrar repuesto | `POST /api/ordenes/{id}/repuestos` | Mecánico asignado | RN-03 |
| F06 Generar presupuesto | `POST /api/ordenes/{id}/presupuestos` | Jefe | RN-04, RN-06, RN-09 |
| F07 Registrar decisión | `POST .../presupuestos/{id}/decision` | Jefe | RN-06, RN-07, RN-08 |
| F08 Gestionar repuestos | `POST .../repuestos/pedido`, `.../recepcion` | Jefe | RN-07 |
| F09 Anotación adicional | `POST /api/ordenes/{id}/desperfectos` (en reparación) | Mecánico asignado | RN-03, RN-08 |
| F10 Finalizar trabajo | `POST /api/ordenes/{id}/finalizar` | Mecánico asignado | RN-03, RN-09, RN-10 |
| F11 Registrar entrega | `POST /api/ordenes/{id}/entrega` | Jefe | RN-10 |
| F12 Enviar notificación | `POST /api/notificaciones` (uso interno) | Sistema | RN-09 |

El detalle completo de reglas de negocio, DER y diagramas C4 está en `Respuestas Cuestionario.pdf`.

## 6. Estructura del repositorio

```
api-gateway/                 Spring Cloud Gateway (único punto de entrada)
ms-clientes-vehiculos/       Clientes, vehículos, integración vPIC
ms-ordenes/                  OT, presupuestos, repuestos, reglas de negocio
ms-notificaciones/           Envío de WhatsApp, historial, reintentos
taller-commons/              Librería compartida (errores, seguridad, validadores)
keycloak/realm-taller.json   Realm con roles y usuarios de prueba
postgres/init.sql            Crea las 3 bases al iniciar el contenedor
postman/                     Colección + entorno para probar el flujo completo
docker-compose.yml           Orquesta Postgres + Keycloak + los 4 servicios
build.sh / build.cmd         Compila taller-commons y empaqueta los 4 servicios
.env.example                 Variables de entorno (copiar como .env)
```

## 7. WhatsApp real (opcional)

Por defecto (`WHATSAPP_ENABLED=false`) los envíos se simulan en el log de `ms-notificaciones`, no
hace falta nada de Meta para correr la colección completa. Para mandar mensajes reales:

1. Sacá un número de prueba en el panel de desarrolladores de Meta (hasta 5 destinatarios
   verificados) y un token que no venza (el temporal del panel expira a las 24 h).
2. Completá en `.env`: `WHATSAPP_ENABLED=true`, `WHATSAPP_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`.
3. Las plantillas (`WHATSAPP_PLANTILLA_PRESUPUESTO`, `WHATSAPP_PLANTILLA_VEHICULO_LISTO`) tienen que
   estar aprobadas por Meta con ese mismo nombre.
4. Si el envío falla, `ms-notificaciones` reintenta solo (cada 60 s por defecto) sin frenar la OT,
   hasta `notificaciones.max-intentos` (5 por defecto).

## 8. Problemas comunes

| Síntoma | Causa probable | Solución |
|---|---|---|
| `mvn compile` falla con `cannot find symbol: method getXxx()` | JDK >= 21 activo | Usar JDK 17 (sección 1) |
| `docker compose up` no arranca nada | Docker no está corriendo | Abrir Docker Desktop y reintentar |
| `docker compose up --build` falla copiando el jar | No se corrió `build.sh`/`build.cmd` antes | Compilar en el host primero |
| 401 en todas las rutas | Token vencido o falta `Authorization: Bearer` | Pedir un token nuevo |
| 403 inesperado | Rol incorrecto, o mecánico que no es el asignado a esa OT (RN-03) | Revisar qué usuario llama y a quién está asignada la OT |
| Puerto ocupado (8080-8084, 5433, 8081) | Otro proceso local lo está usando | Liberarlo o cambiar el mapeo en `docker-compose.yml` |
| vPIC no responde / sin internet | API pública externa | El sistema cae solo a carga manual (`datosVerificados=false`); no bloquea el flujo |
