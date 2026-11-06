@echo off
REM Compila el JAR común, lo instala en el .m2 local y empaqueta cada servicio.
call mvn -f taller-commons\pom.xml clean install || exit /b 1
for %%s in (api-gateway ms-clientes-vehiculos ms-ordenes ms-notificaciones) do (
  call mvn -f %%s\pom.xml clean package -DskipTests || exit /b 1
)
echo Listo. Ahora: docker compose up --build
