# HTTP RESTful API for SPACE

[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=pgmarc_space-java-client&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=pgmarc_space-java-client)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=pgmarc_space-java-client&metric=coverage)](https://sonarcloud.io/summary/new_code?id=pgmarc_space-java-client)
[![Maintainability Rating](https://sonarcloud.io/api/project_badges/measure?project=pgmarc_space-java-client&metric=sqale_rating)](https://sonarcloud.io/summary/new_code?id=pgmarc_space-java-client)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=pgmarc_space-java-client&metric=ncloc)](https://sonarcloud.io/summary/new_code?id=pgmarc_space-java-client)

HTTP REST client implemented in Java for Subscription and Pricing Access Control Engine (SPACE).

## Features

- Read, create and update user's subscriptions
- Verify a user's feature availability based on their subscription
- Configure SPACE client with:
  - Host (required)
  - Port (optional, by default SPACE listens on port `5403`)
  - SPACE URL path prefix (optional, by default space is under `api/v1`, i.e., `http://example.com/api/v1`)
  - Read and write timeout of HTTP client (optional, uses OkHTTP client under the hood)

SPACE client implements the following operations of [SPACE OAS](https://github.com/Alex-GF/space/blob/main/api/docs/space-api-docs.yaml):

- `POST /contracts`
- `GET /contracts/{userId}`
- `PUT /contracts/{userId}`
- `POST /features/{userId}/{featureId}`
- `POST /features/{userId}/pricing-token`


## Installation

### Requirements

- Java 11 or later
- Apache Maven 3.6.3 or later (excluding `4.X`)

### Maven

```xml
<dependency>
    <groupId>io.github.pgmarc.space</groupId>
    <artifactId>space-client</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

To build and install the jar locally execute the following:

```bash
cd space-client
mvn clean install
```

### Usage

Before using `SpaceClient` you will need to do the following:
1. Start a SPACE instance in your machine
2. Get the corresponding api key for the required role
3. As `MANAGER` or `ADMIN` add a service in SPACE by uploading a pricing in `Pricing2Yaml` format specification

See [Pricing4SaaS docs](https://sphere-docs.vercel.app) for more information.

> [!WARNING]
> API Keys are secrets and should be kept safe end encrypted.

Configure `SpaceClient` using the builder:
```java
SpaceClient client = SpaceClient.builder("example.com", apiKey)
        .port(8080)
        .path("example/path")
        .build();
```

Create subscriptions:
```java
UserContact contact = UserContact
        .builder("3f5f934c-951b-4a2d-ad10-b1679ac9b7ba", "example-user").build();

SubscriptionRequest createSubscriptionRequest = SubscriptionRequest.builder(contact)
    .startService("Petclinic", "2024")
        .plan("Enterprise")
        .addOn("petLover", 1)
    .endService()
    .build();

Subscription subscription = client.contracts().addContract(createSubscriptionRequest);
```

Evaluate subscription features at runtime:

```java
String userId = "3f5f934c-951b-4a2d-ad10-b1679ac9b7ba";
FeatureEvaluationResult result = client
        .features()
        .evaluate(userid, "Petclinic", "pets");
```

These are just some examples, but you can find more in `examples` directory.

### Documentation

You read more documentation about SPACE in [SPHERE docs](https://sphere-docs.vercel.app).
