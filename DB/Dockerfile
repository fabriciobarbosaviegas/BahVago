FROM mysql:8.0

ENV MYSQL_ROOT_PASSWORD=root
ENV MYSQL_DATABASE=bahvagoBD

COPY ./migrations/bahvagoBD.sql /docker-entrypoint-initdb.d/bahvagoDB.sql

EXPOSE 3306