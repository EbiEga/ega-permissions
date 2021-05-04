# EGA-permissions
We are in the process to create a permissions API that will be used to apply permission into the EGA. The specifics of the API are still under consideration, in the meantime we want to start the generation of the process and the pipeline from development to production.

## Building
This project uses standard Maven build processes. Run `mvn package` to compile, test, and package the service.

## Docker
The service is built into a container for deployment, controlled by the Dockerfile. There are no things to configure about the container yet. The Docker image is store in the Gitlab Container Registry.

## CI
CI is done by Gitlab CI using the EGA group runner.

## Kubernetes
The Kubernetes folder contains config files for deploying into the EGA Kubernetes Cluster in the Vault. You can apply these changes using `kubectl`.  By default the endpoints are only reachable internally but they can be registed in zuul-ingress so they are reachable externally. You can test this [here](https://ega.ebi.ac.uk:8053/version)  

## Run IT tests in the local environment  
In the ```.m2/settings.xml``` file it's necessary to add these tags:     

```
<it.clientId>****</it.clientId>
<it.clientSecret>****</it.clientSecret>
<it.user>****</it.user>
<it.pass>****</it.pass>
```
The values are securely stored in the vault.

I'm just testing the GitLab pipeline for the EE-1942 ticket. Sorry!
Part 1.
