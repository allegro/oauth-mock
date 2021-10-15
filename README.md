# Mock OAuth Server

OAuth mock server capable of creating jwt tokens and providing JWK set.

## Endpoints

| Endpoint               | Method | Description                                                                                                       |
|------------------------|--------|-------------------------------------------------------------------------------------------------------------------|
| /{provider}/jwks       | GET    | Provides JWKS                                                                                                     |
| /{provider}/token      | GET    | Provides basic token                                                                    |
| /{provider}/token      | POST   | Provides token for specific client_id (client must be added to the application via json file in properties or /{provider}/client endpoint  |
| /{provider}/client     | PUT    | Adds client, accepts json body

## Providers

Default provider is `auth`. If request is made to non-existing provider it gets generated.

## Supported grant types

- Client credentials