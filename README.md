## Development server

Run `java -jar target/server-0.1.0.jar` for a dev server. Navigate to `http://localhost:8080/`.

Rebuild & restart:

`mvn clean package && java -jar target/server-0.1.0.jar`

## Build

Run `mvn package` to build the project.

## Running unit tests

Run `mvn test` 

## Deployment

Run `eb deploy` on local. Assuming set up access tokens in `~/.aws/config` file.

Autodeployment should use environment variables set. See https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3-configuration.html

## Liquibase scripts

```bash
liquibase --driver=org.postgresql.Driver \
      --classpath=/Users/hubertczerpak/.m2/repository/org/postgresql/postgresql/42.2.5/postgresql-42.2.5.jar \
      --changeLogFile=src/main/resources/db/changelog/master.yaml \
      --url="jdbc:postgresql://localhost:5432/postgres" \
      --username=postgres \
      --password=postgres \
      generateChangeLog
 ```