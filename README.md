# Pedidos360 - Backend Cloud Native

## Sobre el proyecto

Pedidos360 es un proyecto que desarrollamos para la asignatura de Desarrollo Cloud Native I

Nuestro proyecto está enfocado en la gestión de productos y pedidos de una tienda de panadería y pastelería, permitiendo que los clientes puedan realizar compras y que los administradores puedan gestionar los productos, revisar los pedidos y consultar información sobre las ventas y el inventario

Para el desarrollo del backend utilizamos Java con Spring Boot, trabajando bajo una arquitectura de microservicios. También incorporamos MongoDB para el almacenamiento de información, RabbitMQ para la comunicación mediante mensajes y Apache Kafka para el procesamiento de eventos

Utilizamos Docker para facilitar la ejecución de los distintos servicios sin necesidad de instalar y configurar cada uno por separado

## Integrantes

- Benjamín Vásquez
- Dante Muñoz
- Carlo Fuentes
- Alejandro Salazar

## Tecnologías utilizadas

Para desarrollar nuestro backend utilizamos las siguientes tecnologías:

- Java 21
- Spring Boot 4.0.8
- Spring Security
- Spring Data MongoDB
- Spring AMQP
- Apache Kafka
- Kafka Streams
- Kafka UI
- Apache ZooKeeper
- RabbitMQ
- MongoDB
- Microsoft Entra ID
- OAuth 2.0
- JWT
- Docker
- Docker Compose
- Maven
- API REST
- Git y GitHub

## Arquitectura del proyecto

Nuestro backend está dividido en cuatro microservicios, cada uno encargado de una responsabilidad diferente dentro del sistema

Esta separación nos permite organizar mejor el código, trabajar de manera independiente en cada servicio y facilitar la integración de nuevas funcionalidades

Los microservicios que forman parte del proyecto son:

| Microservicio | Puerto | Función |
|---|---|---|
| ms-order | 8081 | Gestión de pedidos |
| ms-product | 8082 | Gestión de productos e inventario |
| ms-rabbit-admin | 8083 | Administración de RabbitMQ |
| ms-kafka-admin | 8084 | Administración de Kafka |

Además de estos microservicios, contamos con MongoDB, RabbitMQ, Kafka, ZooKeeper y Kafka UI, los cuales forman parte de la infraestructura del proyecto

## Microservicios

### ms-order

Este microservicio se encarga de gestionar los pedidos realizados por los clientes

Cuando un usuario realiza una compra, el servicio se comunica con el microservicio de productos para verificar y descontar el stock correspondiente. Posteriormente, guarda el pedido en MongoDB y publica eventos en RabbitMQ y Kafka

Sus principales funcionalidades son:

- Registrar nuevos pedidos
- Consultar los pedidos de un usuario
- Consultar todos los pedidos como administrador
- Identificar al cliente mediante su token JWT
- Validar la disponibilidad de productos
- Comunicarse con el microservicio de productos
- Publicar eventos en RabbitMQ
- Publicar eventos en Kafka
- Generar comandas de cocina en los registros del servicio
- Simular notificaciones de pedidos
- Registrar información de auditoría
- Procesar estadísticas de ventas
- Consultar alertas de stock bajo
- Obtener los productos más vendidos

### ms-product

Este microservicio se encarga de administrar el catálogo de productos y controlar el inventario de la tienda

Utilizamos MongoDB para almacenar los productos y mantener control sobre las cantidades disponibles

Sus principales funcionalidades son:

- Registrar nuevos productos
- Consultar los productos disponibles
- Actualizar información de productos
- Eliminar productos
- Gestionar precios y stock
- Descontar stock al realizar un pedido
- Validar que exista stock suficiente
- Registrar información relacionada con las ventas
- Detectar productos con stock bajo
- Publicar alertas de inventario mediante Kafka
- Consumir eventos de pedidos mediante RabbitMQ y Kafka

### ms-rabbit-admin

Este microservicio lo desarrollamos para administrar los recursos de RabbitMQ mediante una API REST

De esta manera podemos consultar y gestionar los elementos utilizados para la comunicación entre los servicios

Sus principales funcionalidades son:

- Crear colas
- Consultar las colas existentes
- Eliminar colas
- Crear exchanges
- Consultar exchanges
- Eliminar exchanges
- Crear bindings
- Consultar bindings
- Eliminar bindings
- Comunicarse con la API de administración de RabbitMQ

El acceso a estas funcionalidades está restringido a usuarios con permisos de administrador cuando la seguridad se encuentra habilitada

### ms-kafka-admin

Este microservicio se encarga de administrar los recursos de Apache Kafka

Nos permite gestionar los tópicos utilizados para el procesamiento de eventos y consultar información relacionada con los grupos de consumidores

Sus principales funcionalidades son:

- Crear tópicos
- Consultar los tópicos disponibles
- Consultar información de un tópico
- Actualizar la configuración de tópicos
- Eliminar tópicos
- Consultar grupos de consumidores
- Consultar información de un grupo de consumidores
- Eliminar grupos de consumidores

Este microservicio también utiliza autenticación mediante JWT y requiere permisos de administrador

## Base de datos

Para el almacenamiento de información utilizamos MongoDB, una base de datos NoSQL que permite trabajar con documentos

Dividimos la información en dos bases de datos según las responsabilidades de nuestros microservicios

| Base de datos | Responsabilidad |
|---|---|
| pedidos360_order | Almacenamiento de pedidos y auditoría |
| pedidos360_product | Almacenamiento de productos e información de ventas |

Las principales colecciones utilizadas son:

- orders
- order_audit
- products
- product_sales
- processed_order_events

MongoDB se ejecuta dentro de un contenedor Docker y utiliza un volumen persistente para conservar la información cuando se detienen o reinician los contenedores

## Integración con RabbitMQ

Utilizamos RabbitMQ para implementar la comunicación asíncrona entre nuestros microservicios

La idea es que ciertas operaciones puedan ejecutarse mediante mensajes sin necesidad de que todos los procesos dependan directamente de una solicitud HTTP

Cuando se registra un pedido, se publican mensajes que pueden ser procesados por distintos consumidores según la función que deban realizar

Dentro del proyecto utilizamos RabbitMQ para:

- Procesar eventos de pedidos creados
- Simular notificaciones de confirmación
- Generar comandas de cocina mediante logs
- Registrar información de ventas
- Manejar errores durante el procesamiento de mensajes
- Administrar colas de mensajes fallidos

### Exchanges utilizados

| Exchange | Función |
|---|---|
| pedidos360.topic | Distribución de eventos de pedidos |
| pedidos360.direct | Enrutamiento directo de mensajes |
| pedidos360.dlx | Gestión de mensajes que no pudieron procesarse |

### Colas utilizadas

| Cola | Función |
|---|---|
| order.notification.queue | Procesamiento de notificaciones |
| kitchen.ticket.queue | Generación de comandas de cocina |
| product.sales.queue | Registro de información de ventas |
| order.notification.dlq | Mensajes fallidos de notificaciones |
| kitchen.ticket.dlq | Mensajes fallidos de cocina |
| product.sales.dlq | Mensajes fallidos de ventas |

También incorporamos mecanismos de confirmación manual de mensajes y manejo de errores mediante Dead Letter Queues (DLQ)

### Clúster de RabbitMQ

Para RabbitMQ configuramos dos nodos dentro de Docker:

- rabbitmq1
- rabbitmq2

Ambos nodos están configurados para formar un clúster y compartir una política de replicación de colas clásicas

Con esto buscamos aplicar los conceptos de disponibilidad y tolerancia a fallos trabajados durante la asignatura

## Integración con Apache Kafka

Además de RabbitMQ, incorporamos Apache Kafka para trabajar con eventos y procesar información relacionada con los pedidos y el inventario

Cuando se registra un pedido, el microservicio ms-order publica un evento en Kafka, el cual puede ser procesado por distintos consumidores sin reemplazar el flujo de mensajería que ya teníamos implementado con RabbitMQ

Utilizamos Kafka para:

- Publicar eventos de pedidos creados
- Registrar información de auditoría
- Procesar estadísticas de ventas
- Detectar productos con stock bajo
- Generar eventos de inventario
- Procesar información mediante múltiples consumidores
- Gestionar eventos que presentan errores

### Tópicos utilizados

| Tópico | Función |
|---|---|
| pedidos360.orders.created | Eventos de pedidos creados |
| pedidos360.inventory.low-stock | Alertas de stock bajo |
| pedidos360.orders.created.DLT | Eventos de pedidos con errores |
| pedidos360.inventory.low-stock.DLT | Eventos de inventario con errores |

Los tópicos principales se encuentran configurados con tres particiones y un factor de replicación de uno para nuestro entorno actual

También contamos con tópicos DLT (Dead Letter Topic), donde se pueden enviar eventos que no pudieron ser procesados correctamente

## Kafka Streams

Incorporamos Kafka Streams dentro del microservicio ms-order para procesar los eventos generados por el sistema

Con esta implementación podemos obtener información relacionada con las ventas y el inventario a partir de los eventos publicados en Kafka

Las principales funcionalidades son:

- Procesamiento de eventos de pedidos
- Agrupación de productos vendidos
- Cálculo de unidades vendidas por producto
- Generación del ranking de productos más vendidos
- Almacenamiento de la última alerta de stock por producto
- Validación de eventos
- Envío de eventos inválidos a un tópico DLT

Para almacenar el estado del procesamiento utilizamos dos State Stores:

| State Store | Función |
|---|---|
| units-sold-store | Almacenar las unidades vendidas por producto |
| low-stock-store | Almacenar la última alerta de stock por producto |

Toda esta información puede ser consultada desde el frontend mediante los endpoints de streaming

## Apache ZooKeeper

Utilizamos Apache ZooKeeper como parte de la configuración de nuestro entorno Kafka

Su función dentro del proyecto es apoyar la coordinación y administración del broker de Kafka

ZooKeeper se ejecuta mediante Docker y trabaja junto con el contenedor de Kafka

## Kafka UI

Incorporamos Kafka UI para facilitar la visualización y administración de los recursos de Kafka desde una interfaz web

Desde esta herramienta podemos:

- Visualizar los tópicos
- Consultar las particiones
- Revisar los mensajes publicados
- Consultar los grupos de consumidores
- Revisar información del broker
- Comprobar el flujo de eventos del proyecto

Una vez iniciado el proyecto, podemos ingresar a Kafka UI desde:

http://localhost:8090

## Autenticación y seguridad

Para la autenticación de los usuarios utilizamos Microsoft Entra ID, integrado con Spring Security y OAuth 2.0

Los usuarios se autentican desde el frontend y reciben un token JWT, el cual se envía al backend al realizar solicitudes a los servicios protegidos

El backend se encarga de validar el token y comprobar los permisos correspondientes según la operación que se quiera realizar

Dentro del sistema diferenciamos principalmente dos perfiles

### Cliente

- Consultar productos
- Realizar pedidos
- Consultar sus propios pedidos

### Administrador

- Consultar todos los pedidos
- Acceder a las estadísticas de ventas
- Consultar información de auditoría
- Consultar alertas de stock
- Administrar recursos de RabbitMQ
- Administrar recursos de Kafka

Actualmente, la consulta de productos es pública, mientras que las operaciones de modificación del catálogo requieren autenticación

Como mejora de seguridad, queda pendiente restringir las operaciones administrativas de productos específicamente al rol de administrador

## Flujo de funcionamiento

Cuando un cliente realiza un pedido, el sistema sigue el siguiente flujo:

1. El cliente inicia sesión desde el frontend
2. El cliente selecciona los productos que desea comprar
3. El frontend envía el pedido al microservicio ms-order
4. El microservicio valida el token JWT del usuario
5. ms-order se comunica con ms-product
6. ms-product verifica y descuenta el stock correspondiente
7. El pedido se almacena en MongoDB
8. Se publica un evento de pedido creado en Kafka
9. Se publican los mensajes correspondientes en RabbitMQ
10. Los consumidores procesan las notificaciones simuladas, comandas y registros de ventas
11. Los consumidores de Kafka procesan la información de auditoría e inventario
12. Kafka Streams actualiza los indicadores correspondientes
13. El frontend puede consultar la información mediante las APIs del backend

## Estructura del proyecto

Organizamos nuestro backend de la siguiente manera:

```text
backend-Cloud-Native/
│
├── ms-order/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw
│
├── ms-product/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw
│
├── ms-rabbit-admin/
│   ├── src/
│   ├── Dockerfile
│   └── pom.xml
│
├── ms-kafka-admin/
│   ├── src/
│   ├── Dockerfile
│   ├── pom.xml
│   └── mvnw
│
├── rabbitmq/
│   └── rabbitmq.conf
│
├── docker-compose.yml
├── README-KAFKA-ZOOKEEPER-INTEGRADO.md
└── README.md
```

Dentro de cada microservicio organizamos las clases según sus responsabilidades, utilizando controladores, servicios, modelos, repositorios, configuraciones y componentes de mensajería cuando corresponde

## Requisitos para ejecutar el proyecto

Antes de ejecutar el backend es necesario tener instalado:

- Git
- Docker Desktop o Docker Engine
- Docker Compose
- Visual Studio Code o IntelliJ IDEA

En caso de querer ejecutar o compilar los microservicios directamente fuera de Docker, también se requiere:

- Java 21
- Maven

## Instalación y ejecución

Primero debemos clonar nuestro repositorio de GitHub

```bash
git clone https://github.com/benjawl-alt/backend-Cloud-Native.git
```

Ingresamos a la carpeta del proyecto

```bash
cd backend-Cloud-Native
```

Si queremos trabajar específicamente con la rama feature-ale, ejecutamos:

```bash
git switch feature-ale
```

Antes de levantar los servicios debemos comprobar que Docker se encuentre iniciado

```bash
docker --version
```

```bash
docker compose version
```

Podemos revisar la configuración de Docker Compose con el siguiente comando:

```bash
docker compose config
```

Una vez comprobada la configuración, construimos e iniciamos todos los servicios

```bash
docker compose up -d --build
```

Para comprobar el estado de los contenedores utilizamos:

```bash
docker compose ps
```

Con esto deberían iniciarse los microservicios y los componentes de infraestructura necesarios para el funcionamiento del backend

## Servicios y puertos

Los servicios disponibles en nuestro entorno local son los siguientes:

| Servicio | Dirección o puerto |
|---|---|
| Microservicio de pedidos | http://localhost:8081 |
| Microservicio de productos | http://localhost:8082 |
| Administración de RabbitMQ | http://localhost:8083 |
| Administración de Kafka | http://localhost:8084 |
| RabbitMQ Management - Nodo 1 | http://localhost:25682 |
| RabbitMQ Management - Nodo 2 | http://localhost:25683 |
| RabbitMQ AMQP - Nodo 1 | localhost:5672 |
| RabbitMQ AMQP - Nodo 2 | localhost:5673 |
| Kafka UI | http://localhost:8090 |
| Kafka Broker | localhost:29092 |
| ZooKeeper | localhost:2181 |
| MongoDB | localhost:27018 |

Los servicios internos se comunican mediante los nombres de los contenedores definidos en Docker Compose

## Endpoints del backend

### Microservicio de productos

| Método | Endpoint | Función |
|---|---|---|
| GET | /api/products | Obtener los productos |
| POST | /api/products | Registrar un producto |
| PUT | /api/products/{id} | Actualizar un producto |
| DELETE | /api/products/{id} | Eliminar un producto |
| POST | /api/products/decrease-stock | Descontar stock |

### Microservicio de pedidos

| Método | Endpoint | Función |
|---|---|---|
| GET | /api/orders | Consultar todos los pedidos |
| POST | /api/orders | Registrar un pedido |
| GET | /api/orders/mine | Consultar los pedidos del usuario |

### Endpoints de streaming

| Método | Endpoint | Función |
|---|---|---|
| GET | /api/orders/stream/metrics | Consultar métricas de pedidos y ventas |
| GET | /api/orders/stream/audit | Consultar registros de auditoría |
| GET | /api/orders/stream/alerts | Consultar alertas de stock bajo |
| GET | /api/orders/stream/top-products?limit=5 | Consultar los cinco productos más vendidos |

### Administración de RabbitMQ

| Método | Endpoint | Función |
|---|---|---|
| GET | /api/rabbit/queues | Consultar colas |
| POST | /api/rabbit/queues | Crear una cola |
| DELETE | /api/rabbit/queues/{name} | Eliminar una cola |
| GET | /api/rabbit/exchanges | Consultar exchanges |
| POST | /api/rabbit/exchanges | Crear un exchange |
| DELETE | /api/rabbit/exchanges/{name} | Eliminar un exchange |
| GET | /api/rabbit/bindings | Consultar bindings |
| POST | /api/rabbit/bindings | Crear un binding |
| DELETE | /api/rabbit/bindings | Eliminar un binding |

### Administración de Kafka

| Método | Endpoint | Función |
|---|---|---|
| GET | /api/kafka/topics | Consultar tópicos |
| POST | /api/kafka/topics | Crear un tópico |
| GET | /api/kafka/topics/{name} | Consultar un tópico |
| PUT | /api/kafka/topics/{name} | Actualizar un tópico |
| DELETE | /api/kafka/topics/{name} | Eliminar un tópico |
| GET | /api/kafka/consumer-groups | Consultar grupos de consumidores |
| GET | /api/kafka/consumer-groups/{id} | Consultar un grupo |
| DELETE | /api/kafka/consumer-groups/{id} | Eliminar un grupo |

Para consumir los endpoints protegidos es necesario enviar un token JWT válido en la cabecera de autorización

```text
Authorization: Bearer TOKEN_DE_ACCESO
```

## Variables de entorno

Dentro de Docker Compose utilizamos variables de entorno para configurar la comunicación entre los servicios

Algunas de las principales variables utilizadas son:

| Variable | Función |
|---|---|
| RABBITMQ_USER | Usuario de RabbitMQ |
| RABBITMQ_PASSWORD | Contraseña de RabbitMQ |
| RABBITMQ_ERLANG_COOKIE | Cookie compartida entre los nodos |
| RABBITMQ_ADDRESSES | Direcciones de los nodos de RabbitMQ |
| SPRING_MONGODB_URI | Dirección de conexión a MongoDB |
| SPRING_KAFKA_BOOTSTRAP_SERVERS | Dirección de conexión a Kafka |
| PRODUCT_SERVICE_URL | Dirección del microservicio de productos |
| CORS_ORIGINS | Orígenes permitidos para las solicitudes |
| APP_SECURITY_ENABLED | Habilitación de seguridad en ms-rabbit-admin |
| APP_STOCK_LOW_THRESHOLD | Límite para generar alertas de stock bajo |
| KAFKA_REPLICAS | Factor de replicación de los tópicos |

Para configurar las credenciales de RabbitMQ podemos crear un archivo `.env` en la raíz del proyecto

```env
RABBITMQ_USER=pedidos360
RABBITMQ_PASSWORD=REEMPLAZAR_CON_CONTRASENA_SEGURA
RABBITMQ_ERLANG_COOKIE=REEMPLAZAR_CON_VALOR_SEGURO
CORS_ORIGINS=http://localhost:4200
APP_SECURITY_ENABLED=true
```

Por seguridad, recomendamos no subir el archivo `.env` ni credenciales reales al repositorio

## Comprobación de los servicios

Para revisar los registros de todos los contenedores podemos utilizar:

```bash
docker compose logs -f
```

Para revisar los registros de un microservicio en particular:

```bash
docker compose logs -f ms-order
```

```bash
docker compose logs -f ms-product
```

```bash
docker compose logs -f ms-rabbit-admin
```

```bash
docker compose logs -f ms-kafka-admin
```

### Comprobar RabbitMQ

Para consultar el estado del clúster:

```bash
docker exec rabbitmq1 rabbitmqctl cluster_status
```

Para consultar las colas existentes:

```bash
docker exec rabbitmq1 rabbitmqctl list_queues
```

Para consultar los exchanges:

```bash
docker exec rabbitmq1 rabbitmqctl list_exchanges
```

### Comprobar Kafka

Para consultar los tópicos existentes:

```bash
docker exec pedidos360-kafka kafka-topics --bootstrap-server kafka:9092 --list
```

Para consultar los eventos de pedidos publicados:

```bash
docker exec pedidos360-kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic pedidos360.orders.created --from-beginning
```

Para consultar los eventos de stock bajo:

```bash
docker exec pedidos360-kafka kafka-console-consumer --bootstrap-server kafka:9092 --topic pedidos360.inventory.low-stock --from-beginning
```

Podemos finalizar la consulta de mensajes utilizando Ctrl + C

## Detener el proyecto

Para detener todos los servicios utilizamos:

```bash
docker compose down
```

Si necesitamos reiniciar los contenedores:

```bash
docker compose restart
```

Es importante evitar utilizar el comando `docker compose down -v` si queremos conservar los datos almacenados en los volúmenes

## Compilación y pruebas

Cada microservicio cuenta con su propio archivo `pom.xml` para administrar las dependencias mediante Maven

Si queremos compilar un microservicio de manera independiente, debemos ingresar a su carpeta y ejecutar:

```bash
mvn clean package
```

Para ejecutar las pruebas disponibles:

```bash
mvn test
```

Las pruebas que requieren servicios externos deben ejecutarse con la infraestructura correspondiente disponible

## Integración con el frontend

Nuestro backend se comunica con un frontend desarrollado en Angular

Desde el frontend se pueden realizar solicitudes a los microservicios para consultar productos, realizar pedidos, revisar el historial de compras y visualizar los indicadores del panel administrativo

La comunicación se realiza mediante APIs REST, utilizando tokens JWT en las solicitudes protegidas

### Repositorio del frontend

https://github.com/benjawl-alt/Frontend-Cloud-Native

### Repositorio del backend

https://github.com/benjawl-alt/backend-Cloud-Native

## Trabajo en equipo

Durante el desarrollo utilizamos Git y GitHub para organizar nuestro código y trabajar de manera colaborativa

Utilizamos ramas para desarrollar funcionalidades, realizar modificaciones y posteriormente integrar los cambios al proyecto

Esto nos permitió mantener un registro de los avances, identificar errores y organizar el trabajo de los integrantes

## Consideraciones del proyecto

- Las notificaciones de correo y push actualmente se simulan mediante registros en los logs
- Las comandas de cocina se generan en los registros del microservicio
- Las métricas generales de pedidos y ventas mantienen contadores en memoria
- Kafka utiliza un broker en el entorno actual
- La publicación de eventos no utiliza todavía el patrón Outbox
- Si ocurre un error en la publicación de un evento, el pedido puede quedar almacenado sin que dicho evento sea publicado
- El funcionamiento de los servicios protegidos depende de la configuración de Microsoft Entra ID

## Mejoras futuras

Algunas mejoras que nos gustaría incorporar más adelante son:

- Implementar notificaciones reales mediante correo electrónico
- Incorporar un sistema de notificaciones en tiempo real
- Mejorar la disponibilidad de Kafka mediante múltiples brokers
- Incorporar más pruebas unitarias y de integración
- Mejorar la seguridad de las operaciones administrativas
- Implementar el patrón Outbox para mejorar la confiabilidad de los eventos
- Incorporar herramientas de monitoreo de microservicios
- Automatizar los procesos de integración y despliegue
- Ampliar las estadísticas de ventas e inventario
- Mejorar el manejo de errores y recuperación de servicios

## Conclusión

Con este proyecto pudimos aplicar los conocimientos adquiridos durante la asignatura, principalmente el desarrollo de microservicios, la comunicación mediante APIs REST, el uso de bases de datos NoSQL, la autenticación de usuarios y la mensajería asíncrona

La integración de RabbitMQ y Apache Kafka nos permitió trabajar con diferentes formas de comunicación y procesamiento de eventos, mientras que Kafka Streams nos permitió obtener información relacionada con las ventas y el inventario

También utilizamos Docker para organizar la infraestructura y facilitar la ejecución de los distintos servicios que forman parte de nuestro sistema

Durante el desarrollo aprendimos a integrar diferentes tecnologías dentro de un mismo proyecto y a trabajar de manera colaborativa utilizando Git y GitHub

---

Proyecto desarrollado por estudiantes de Ingeniería en Informática

Duoc UC - 2026

Asignatura: Desarrollo Cloud Native I (DSY1107)
