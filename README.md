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