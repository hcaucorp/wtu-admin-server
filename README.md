## Development server

Run `java -jar wtu-server-core/target/server-0.2.0.jar` for a dev server. Navigate to `http://localhost:5000/`.

Rebuild & restart:

`mvn clean package && java -jar wtu-server-core/target/server-0.2.0.jar`

## Build

Run `mvn package` to build the project.

## Running unit tests

Run `mvn test` 

## Deployment

Run `eb deploy` on local. Assuming set up access tokens in `~/.aws/config` file and `.elsticbeanstalk/config.yml` deployment file.

Auto-deployment should use environment variables set. See https://docs.aws.amazon.com/elasticbeanstalk/latest/dg/eb-cli3-configuration.html

`mvn clean package && eb deploy`