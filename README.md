# ega-permissions
We are in the process to create a permissions API that will be used to apply permission into the EGA. The specifics of the API are still under consideration, in the meantime we want to start the generation of the process and the pipeline from development to production.

## Building
This project uses standard Maven build processes. Run `mvn package` to compile, test, and package the service.

## Docker
The service is built into a container for deployment, controlled by the Dockerfile. There are no things to configure about the container yet.

## CI
CI is done by Gitlab CI using the EGA group runner.

## TO-DO
* Fix docker build in gitlab CI, right now it is failing because the docker-in-docker service is not privileged.
* Add deployment to Kubernetes once the Docker container is working
* Update the documentation and this readme with details about how Docker and Kubernetes setup finally worked
