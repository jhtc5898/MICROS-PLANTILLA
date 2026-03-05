


## Despliegue local ACCOUNT
## ------------------------------------------------
## Despliegue local
"docker-compose up -d postgres-customer postgres-account zookeeper kafka kafka-ui"
## Despliegue puerto 
http://localhost:8084/api/v1/customers
## KAFKA
http://localhost:8082
## SWAGGER
http://localhost:8084/swagger-ui.html
## ------------------------------------------------

## Despliegue local CUSTOMER
## ------------------------------------------------
## Despliegue local
"docker-compose up -d postgres-customer postgres-account zookeeper kafka kafka-ui"
## Despliegue puerto 
http://localhost:8083
## KAFKA
http://localhost:8082
## SWAGGER
http://localhost:8083/swagger-ui.html
## ------------------------------------------------

## POSTMAN 
https://mibuseta.postman.co/workspace/My-Workspace~78e0128f-8e38-4cb0-80b8-57720a3ead0e/collection/13910567-07eb6d58-49a2-4cc1-9484-119fe9ce1109?action=share&creator=13910567&active-environment=13910567-c9ae3109-2eef-4a94-b1f2-ca93bd2604b1

## Bases de Datos

El sistema usa **2 bases de datos PostgreSQL**:

### Customer Database
- **Puerto**: 5432
- **Nombre**: `customerdb`
- **Tablas**: `persons`, `customers`
- **Usuario**: `postgres`
- **Contraseña**: `postgres`

### Account Database
- **Puerto**: 5433
- **Nombre**: `accountdb`
- **Tablas**: `accounts`, `movements`
- **Usuario**: `postgres`
- **Contraseña**: `postgres`
