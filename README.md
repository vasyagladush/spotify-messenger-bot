# spotify-messenger-bot

A bot which uses Spotify API, Genius API and a messenger platform API to send you the text of a song which is currently being played.

# Commands

-   Spring Boot Run (not needed as we dockerize our app itself): `./mvnw spring-boot:run`
-   Maven package (skipping tests): `./mvnw clean package -DskipTests`
-   Maven install (skipping tests): `./mvnw clean install -DskipTests` (used in Dockerfile)
-   run Docker compose: `docker-compose up`
-   build/rebuild Docker image: `docker-compose build`
-   rebuild image and run Docker compose: `docker-compose up --build` (most used)
-   rebuild image and run Docker compose in background: `docker-compose up --build -d` (most used)
-   run Docker compose and run containers in background: `docker-compose up -d`
-   force recreate Docker image, build and run Docker compose: `docker-compose up --force-recreate --build -d`
-   stop Docker compose containers: `docker-compose down` (most used)
-   restart Docker compose containers: `docker-compose restart`

# [start.spring.io](https://start.spring.io) config

![Sprint Start Config](spring-start-config.png)

# systom.properties
This file is added for Heroku hosting so it knows which Java version is used 

# Sources:
-   A really good guide on having both Spring Boot App and Database in Docker Containers: https://dev.to/francescoxx/java-crud-rest-api-using-spring-boot-hibernate-postgres-docker-and-docker-compose-5cln
-   Postgres: https://hackernoon.com/using-postgres-effectively-in-spring-boot-applications
-   Postgres: https://www.baeldung.com/spring-boot-postgresql-docker
-   Postgres and Using Models, Repositories, Services in Controllers: https://zetcode.com/springboot/postgresql/?utm_content=cmp-true
-   Postgres Migrations (Flyway): https://www.enterprisedb.com/blog/generating-and-managing-postgresql-database-migrationsupgrades-spring-boot-jpa
-   Postgres and Docker: https://blog.devgenius.io/integrating-postgresql-database-running-on-docker-into-spring-boot-application-3e4386e422a6
-   JSON: https://www.baeldung.com/jackson-object-mapper-tutorial
-   Spring Boot and Docker: https://spring.io/guides/topicals/spring-boot-docker/
-   Spring Boot + Postgres + Docker: https://blog.phillipninan.com/how-to-containerize-spring-boot-and-postgres-jdbc
-   Spring Boot + Postgres + Docker: https://www.baeldung.com/spring-boot-postgresql-docker
-   Official Docker Guidelines for Spring Boot: https://www.docker.com/blog/9-tips-for-containerizing-your-spring-boot-code/
