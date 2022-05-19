# Spring Boot Modular Project

## Description

Spring boot project with

* **Java 11**
* **Spring Boot 2.6+**
* **TLS 1.2 + 1.3 enabled** and properly configured (optionally redirecting HTTP to HTTPS when enabling the HTTP port).
* **HTTP2 enabled**
* **JWT** authentication
* **JUnit 5 / AssertJ / Mockito** for state-of-the-art testing
* **OpenAPI 3** with *Springdoc*

Features:

* **Modular architecture**: App, Platform and common / business modules
* API (Service, DTO, Exception)
* Persistence (Spring Data Repository, JPA)
* Service Layer
* REST controller (CRUD)

* **Access Log** filter, logging all requests (method, URI, response status and execution time)
* **Performance Logging** filter, logging performance tree of nested service calls
* **Logging of all endpoints** on startup
* **Docker support** (Docker and Docker-Compose files), with **Prometheus** monitoring / **Grafana** dashboard.

* **Business metrics**: custom Micrometer *gauge* to expose number of records (notes)

### Setting up TLS

The application uses a TLS server certificate issued by a test CA (certificate authority).  
Clients need to trust this CA in order to connect. The test client already has a properly set up client truststore
including this CA certificate.

When connecting with other clients (browser, Postman, ...), you need to add this CA as trusted root CA (browser:
certificates, add trusted root certificate).  
The CA certificate is located in the project root (`test_ca_001.cer`).

### Setting up JWT authentication

JWTs (JSON Web Tokens) are used for authentication. For testing purpose, this app  
contains a REST endpoint where JWT tokens can be issued for any user.

Signing and verifying JWTs requires a Key Pair (private key for signing, public key for verifying).  
An example key pair is already included and configured. To create your own key pair, use:

* **For RSA 4096 key:**
  ```bash  
  ssh-keygen -t rsa -b 4096 -m PEM -f jwt.key  
  openssl rsa -in jwt.key -pubout -outform PEM -out jwt.pub.pem  
  ```  

Do not enter a password (not required).

* **For EC 256 key:**
   ```bash  
  ssh-keygen -t ecdsa -b 256 -m PEM -f jwt.key  
  openssl ec -in jwt.key -pubout -outform PEM -out jwt.pub.pem  
  ```  

Afterwards extract the private and public keys into separate files:

```bash  
openssl pkcs8 -topk8 -inform PEM -outform PEM -in jwt.key -out jwt.pem -nocrypt  
```  

This yields the `jwt.pem` private key and `jwt.pub.pem` public key.

In the configuration (`application-yml`), enable JWT authentication using:

```yaml  
ch.frostnova.platform.security:  
 auth: jwt 
 signing: 
   key-type: RSA 
   public-key: jwt.pub.pem 
   private-key: jwt.pem
 ```  

- `key-type` is required (either **RSA** or **EC**)
- `public-key` is required to validate JWT tokens.
- `private-key` is optional, if configured the application can issue arbitrary JWT tokens in the `/login` endpoint (only
  for testing - do not use for production. Refer to swagger-ui for usage).

## Build

To build this project with Gradle (default tasks: _clean build install_):

    ./gradlew 

## Start

To start this project with Gradle:

    ./gradlew start  

## OpenAPI 3 Documentation (Springdoc)

The OpenAPI 3 schema is available at: [https://localhost:8443/v3/api-docs]()

The Swagger UI is available at: [https://localhost:8443/swagger-ui/index.html]()

## Authentication

The note endpoint requires **authentication using JWT** (header: Authorization: Bearer {jwt}).

**For testing**, you can configure the signing private-key in the security config, which enables the **login endpoint**,
which can issue arbitrary valid JWT tokens. (WARNING: only configure this for testing):
[https://localhost:8443/login/{{tenant}}/{{user}}?roles={{roles}} ]()

## Actuator Endpoints

Info endpoint: https://localhost:8443/info

```json
{
  "name": "spring-multi-module",
  "description": "Spring Boot Multi Module Project",
  "version": "1.0.0-SNAPSHOT",
  "cpu": {
    "usage": "0.01%"
  },
  "memory": {
    "used": "605.73MB",
    "allocated": "2.41GB",
    "usage": "24.56%"
  }
}
```

* Health: https://localhost:8443/health
* Metrics: https://localhost:8443/metrics
* HTTP request metrics: https://localhost:8443/metrics/http.server.requests
* Prometheus: https://localhost:8443/prometheus
* Metrics, custom gauge (number of notes in the DB): <br>
  https://localhost:8443/metrics/business.metrics.notes.count<br>
  https://localhost:8443/prometheus?includedNames=business_metrics_notes_count

## Docker

This project is dockerized using Docker Compose. The docker configuation resides in the `docker/config` directory.
Common tasks and docker-compose commands   
(some tasks can be executed using the docker-compose gradle plugin):

| Task          | with docker-compose | with gradle |  
| -------------:|:---------------------|------------|  
| **Deploy/Start** | `docker-compose up -d` | `gradle composeup` |  
| Build images      | `docker-compose build` | `gradle composebuild` |  
| Follow logs | `docker logs -f spring-boot-modular` | - |  
| Download logs | - | `gradle composelogs` |  
| **Stop/undeploy** | `docker-compose down` |  `gradle composedown` |  
| Stop/undeploy, <br>remove volumes<br>and images | `docker-compose down -v --rmi local` | - |  

### Monitoring

The `docker-compose` file also includes Prometheus and Grafana, along with a preconfigured dashboard.

Open the dashboard at port 3000, e.g. http://localhost:3000, with user `admin` and password `admin`:

![Grafana Dashboard](grafana.png)

## Housekeeping

### Dependency management

To check for dependency updates, run:

gradle dependencyUpdates This will report all available newer versions of the dependencies, so you can update them in
the `build.gradle`.
