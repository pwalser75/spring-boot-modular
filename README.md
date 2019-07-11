# Spring Boot Modular Project

## Description

Spring boot project with

* API (Service, DTO, Exception)
* Persistence (Spring Data Repository, JPA)
* Service Layer
* REST controller (CRUD)
* TLS enabled and properly configured (optionally redirecting HTTP to HTTPS when enabling the HTTP port).
* Access Log filter, logging all requests (method, URI, response status and execution time)
* Performance Logging filter, logging performance tree of nested service calls

### Setting up TLS

The application uses a TLS server certificate issued by a test CA (certificate authority).
Clients need to trust this CA in order to connect. The test client already has a properly set up client truststore including this CA certificate.

When connecting with other clients (browser, Postman, ...), you need to add this CA as trusted root CA (browser: certificates, add trusted root certificate).
The CA certificate is located in the project root (`test_ca_001.cer`).

## Build

To build this project with Gradle (default tasks: _clean build install_):

    gradle
    
## Start
    
To start this project with Gradle:
    
    gradle start

## API Documentation (Swagger)

API documentation reachable at [https://localhost:8443/swagger-ui.html](https://localhost/swagger-ui.html)

## Actuator Endpoints

* Info: https://localhost:8443/info
* Health: https://localhost:8443/health
* Metrics: https://localhost/metrics
* HTTP request metrics: https://localhost:8443/metrics/http.server.requests
* HTTP request metrics for a specific endpoint: [Example: POST /api/notes](https://localhost:8443/metrics/http.server.requests?tag=uri:/api/notes&tag=method:POST)

