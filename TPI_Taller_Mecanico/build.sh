#!/usr/bin/env bash
# Compila el JAR común, lo instala en el .m2 local y empaqueta cada servicio.
set -e
mvn -f taller-commons/pom.xml clean install
for s in api-gateway ms-clientes-vehiculos ms-ordenes ms-notificaciones; do
  mvn -f $s/pom.xml clean package -DskipTests
done
echo "Listo. Ahora: docker compose up --build"
