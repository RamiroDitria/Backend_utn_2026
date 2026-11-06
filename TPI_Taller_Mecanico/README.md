# Taller Mecánico — Backend

Backend de gestión interna de un taller mecánico de automóviles, implementado como un conjunto de
microservicios en Java/Spring Boot. Cubre el recorrido completo de una orden de trabajo (OT): ingreso
del vehículo, diagnóstico (VIN + vPIC), presupuesto, decisión del cliente, pedido de repuestos,
reparación (con posibles adicionales) y entrega, notificando al cliente por WhatsApp en cada etapa
clave.

Este documento explica cómo levantar **todo** el sistema de punta a punta y cómo probarlo.

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

- **api-gateway**: único punto de entrada, valida el JWT y aplica permisos gruesos por ruta.
- **ms-clientes-vehiculos**: clientes, vehículos por patente, integración con vPIC.
- **ms-ordenes**: el corazón del dominio — OT, estados, desperfectos, repuestos, presupuestos,
  decisiones y entrega. Aplica la mayoría de las reglas de negocio (RN-01 a RN-10).
- **ms-notificaciones**: arma y envía los WhatsApp (o los simula en el log), guarda el historial y
  reintenta los que fallan.
- **taller-commons**: librería compartida (manejo de errores, conversión de roles de Keycloak,
  validadores de patente y VIN).

## 1. Requisitos previos

| Herramienta | Versión | Para qué |
|---|---|---|
| **JDK** | **17** (exacto) | Compilar y correr los 4 servicios |
| Maven | 3.8+ | Compilar (`mvn`) |
| Docker + Docker Compose | reciente | Levantar Postgres, Keycloak y los servicios |

### ⚠️ Sobre la versión de JDK

Los `pom.xml` fijan `java.version=17` y el proyecto usa Lombok (vía el parent de Spring Boot 3.2.5),
que **no procesa anotaciones correctamente con JDK 21+**. Si tu máquina tiene instalado solo un JDK
más nuevo (21, 25, 26...), `mvn compile` va a fallar con errores del tipo
`cannot find symbol: method getXxx()` en clases que usan `@Getter/@Setter` de Lombok — no es un error
del código, es un problema de compatibilidad de herramientas.

Verificá qué JDKs tenés instalados:

```bash
/usr/libexec/java_home -V        # macOS
```

Si no aparece un `17.x`, instalalo (en este repo se validó con `openjdk@17` de Homebrew):

```bash
brew install openjdk@17
```

Y compilá siempre con JDK 17 explícito (no hace falta cambiar el JDK por defecto del sistema):

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -version   # debe decir 17.x
```

Opcional, para que `/usr/libexec/java_home -v 17` también lo encuentre (requiere `sudo`):

```bash
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
```

> Esto ya se verificó en este entorno: con JDK 17 corre `mvn clean install` en los 4 módulos
> (`taller-commons`, `api-gateway`, `ms-clientes-vehiculos`, `ms-ordenes`, `ms-notificaciones`) y los
> tests (`taller-commons`, `ms-ordenes`) sin errores.

## 2. Puesta en marcha

### 2.1 Variables de entorno

```bash
cp .env.example .env
```

Con los valores por defecto alcanza para levantar todo en local. `WHATSAPP_ENABLED=false` hace que
los avisos se **simulen en el log** de `ms-notificaciones` (no hace falta cuenta de Meta para probar
el flujo completo). Si querés enviar WhatsApp real, completá `WHATSAPP_TOKEN` y
`WHATSAPP_PHONE_NUMBER_ID` (ver sección 7).

### 2.2 Compilar (con JDK 17 activo)

```bash
./build.sh        # macOS/Linux
build.cmd         # Windows
```

Esto instala `taller-commons` en el `.m2` local y empaqueta los 4 servicios (`target/*.jar`).

### 2.3 Levantar la infraestructura

```bash
docker compose up --build
```

Levanta, en este orden: PostgreSQL 16 (con las 3 bases ya creadas por `postgres/init.sql`), Keycloak
24 (importando `keycloak/realm-taller.json` con roles y usuarios de prueba) y los 4 servicios Java.

Esperá a ver los 4 microservicios con `Started ...Application` en el log. La primera vez Keycloak
tarda un poco más en importar el realm.

### 2.4 Verificar que levantó

| Servicio | URL |
|---|---|
| API Gateway (punto de entrada) | http://localhost:8080 |
| Keycloak (consola admin: `admin`/`admin`) | http://localhost:8081 |
| Swagger ms-clientes-vehiculos | http://localhost:8082/swagger-ui.html |
| Swagger ms-ordenes | http://localhost:8083/swagger-ui.html |
| Swagger ms-notificaciones | http://localhost:8084/swagger-ui.html |
| PostgreSQL | localhost:5432 (`taller`/`taller`) |

> Los puertos 8082-8084 están expuestos solo para ver el Swagger de cada servicio directamente; en
> uso normal **todo pasa por el gateway (8080)**.

### 2.5 Apagar / limpiar

```bash
docker compose down        # apaga y conserva los datos (volumen pgdata)
docker compose down -v     # apaga y borra también la base de datos
```

## 3. Usuarios y roles de prueba

Vienen precargados en `keycloak/realm-taller.json` (realm `taller`, cliente público `taller-api`):

| Usuario | Password | Rol |
|---|---|---|
| `jefe` | `jefe123` | `JEFE_TALLER` |
| `mecanico1` | `mecanico123` | `MECANICO` |
| `mecanico2` | `mecanico123` | `MECANICO` |

Obtener un token (grant `password`, sin client secret por ser cliente público):

```bash
curl -s -X POST http://localhost:8081/realms/taller/protocol/openid-connect/token \
  -d grant_type=password -d client_id=taller-api \
  -d username=jefe -d password=jefe123 | jq -r .access_token
```

## 4. Probar el flujo completo con Postman

En `postman/` están la colección y el entorno comprometidos para la cátedra:

- `Taller-Mecanico.postman_collection.json`
- `Taller-Mecanico.postman_environment.json`

Importá ambos en Postman, seleccioná el entorno **"Taller Mecánico - Local"** y corré la colección
completa con el **Collection Runner** (botón "Run collection"), en orden. No hace falta cargar nada a
mano: la primera carpeta obtiene los tokens y cada request guarda en el entorno los ids que usan las
siguientes.

Carpetas, en el orden en que deben ejecutarse:

1. **Autenticación** — tokens de `jefe`, `mecanico1` y `mecanico2`.
2. **Clientes y vehículos (F01, RN-01)** — alta de cliente y vehículo (patente aleatoria en cada
   corrida, para poder re-ejecutar la colección sin chocar con RN-01).
3. **Diagnóstico (F02-F05)** — abrir OT, registrar VIN (consulta real a vPIC), desperfectos y
   repuestos.
4. **Presupuesto y decisión (F06-F07)** — genera el presupuesto y registra la aceptación del cliente.
5. **Repuestos y reparación (F08)** — pedido y recepción del repuesto; la OT pasa a En reparación.
6. **Trabajo adicional (F09, RN-08)** — aparece una falla nueva durante la reparación, se presupuesta
   aparte y se decide antes de poder finalizar.
7. **Entrega (F10-F11)** — finalizar trabajo y registrar la entrega (OT Entregada).
8. **Integraciones externas (vPIC)** — un VIN con formato válido pero inexistente, para ejercitar la
   carga manual cuando vPIC no reconoce el vehículo (`datosVerificados=false`).
9. **Casos negativos** — patente inválida/duplicada (RN-01), acceso sin token (401), acceso con rol
   incorrecto (403), VIN inválido (RN-04), mecánico no asignado (RN-03) y pedir repuestos sin
   aceptación del cliente (RN-07).

Cada request tiene asserts (`pm.test`) que validan el código HTTP, el estado resultante de la OT y el
código de regla de negocio (`RN-XX`) cuando corresponde — el Collection Runner muestra un resumen
pass/fail de todo el recorrido.

> No se automatizó el caso "presupuesto vencido" (RN-06) porque requiere esperar 7 días reales; para
> probarlo manualmente, generá un presupuesto y cambiá `fecha_vencimiento` directamente en la tabla
> `ordenes.presupuestos`, o bajá `presupuesto.dias-vigencia` en
> `ms-ordenes/src/main/resources/application.yml` para una demo.

## 5. Mapeo de trazabilidad (resumen)

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

El detalle completo de funcionalidades, reglas de negocio, DER, diagramas C4 y el diagrama de estados
de la OT está en `Respuestas Cuestionario.pdf`.

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

Por defecto (`WHATSAPP_ENABLED=false`) los envíos se simulan en el log de `ms-notificaciones`, así que
no hace falta nada de Meta para correr la colección completa. Para enviar mensajes reales:

1. Conseguí un número de prueba en el panel de desarrolladores de Meta (hasta 5 destinatarios
   verificados) y un **token que no venza** (el temporal del panel expira a las 24 h).
2. Completá en `.env`: `WHATSAPP_ENABLED=true`, `WHATSAPP_TOKEN`, `WHATSAPP_PHONE_NUMBER_ID`.
3. Dado que los avisos de etapa se envían fuera de la ventana de 24 h de un mensaje del cliente, las
   plantillas (`WHATSAPP_PLANTILLA_PRESUPUESTO`, `WHATSAPP_PLANTILLA_VEHICULO_LISTO`) tienen que estar
   **aprobadas por Meta** con ese mismo nombre.
4. Si el envío falla, `ms-notificaciones` reintenta solo (`ReintentoScheduler`, cada 60 s por
   defecto) sin frenar la OT (RN-09), hasta `notificaciones.max-intentos` (5 por defecto).

## 8. Troubleshooting

| Síntoma | Causa probable | Solución |
|---|---|---|
| `mvn compile` falla con `cannot find symbol: method getXxx()` | JDK >= 21 activo, Lombok no lo soporta | Usar JDK 17 (ver sección 1) |
| `docker compose up` no arranca nada | El daemon de Docker no está corriendo | Abrir Docker Desktop y reintentar |
| `docker compose up --build` falla copiando el jar | No se corrió `build.sh`/`build.cmd` antes | Los `Dockerfile` solo empaquetan un JRE 17 y copian `target/*.jar`; hay que compilar en el host primero |
| 401 en todas las rutas | Token vencido (`accessTokenLifespan` 1800 s) o falta `Authorization: Bearer` | Pedir un token nuevo |
| 403 inesperado | Rol incorrecto, o mecánico que no es el asignado a esa OT (RN-03) | Revisar qué usuario llama y a quién está asignada la OT |
| Puerto ocupado (8080-8084, 5432, 8081) | Otro proceso local lo está usando | Liberarlo o cambiar el mapeo de puertos en `docker-compose.yml` |
| vPIC no responde / sin internet | API pública externa | El sistema cae solo a carga manual (`datosVerificados=false`); no bloquea el flujo |
