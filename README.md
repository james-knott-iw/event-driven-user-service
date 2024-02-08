# Event Driven User Service

This repository contains the code for two Spring Boot event-driven microservices, a DockerFile for each service and a Docker compose file to orchestrate the Solace PubSub+, PostgreSQL database and event-driven microservice images. The aim of this system is for one microservice to publish events, Solace PubSub Event broker to distribute the events and another microservice to subscribe to these and populate a PostgreSQL database with data from the events.

## System Requirements

- Java JDK17+
- Maven
- Docker (Version 2.6.1 or lower if using windows. There is current a bug with the COPY command in 2.7+)

## Getting Started

First you need to clone the repository.

This can be done by HTTPS, SSH or GitHubCLI.

### HTTPS

```bash
git clone https://github.com/james-knott-iw/event-driven-user-service.git
```

### SSH

```bash
git clone git@github.com:james-knott-iw/event-driven-user-service.git
```

### GitHubCLI

```bash
gh repo clone james-knott-iw/event-driven-user-service
```

Open the `/event-driven-user-service` directory in a terminal or IDE.

## Publisher and Subscriber

To begin, enter the [/publisher](/publisher) directory:

```bash
cd publisher
```

Build the publishing Spring boot application .jar file which will be located at `/target/userservice-pub-0.0.1.jar`.

```bash
mvn clean package -DskipTests
```

Now enter the [/subscriber](/subscriber) directory:

```bash
cd ../subscriber
```

Build the subscriber Spring boot application .jar file which will be located at `/target/userservice-sub-0.0.1.jar`.

```bash
mvn clean package -DskipTests
```

### Docker Compose File

In this project we need 5 applications running in 5 separate containers. Our Spring Boot publisher, Spring Boot Subscriber, Solace PubSub, the Postgres Database and pgAdmin dashboard. A Docker compose file helps to define multiple containers at once. There is one located in [compose.yaml](/compose.yml). Each container is defined as a `service`.

#### Solace

The first service defined is `solace`. This service runs our Solace PubSub+ event broker which distributes and routes events.

- This service uses the `solace/solace-pubsub-standard` image.
- There are various ports exposed however the most important are ports `8080` and `55555`.
- Port `8080` on the host machine(your machine) is mapped to to port `8080` on the container. This allows you to access the admin dashboard at [http://localhost:8080](http://localhost:8080). The user credentials are `username = admin` and `password = admin`
- The environment variable `username_admin_globalaccesslevel` sets the access level of the username `admin` to have `admin` access level.
- The environment variable `username_admin_password` sets the password for the admin username as `admin`.

#### DB

The second service defined is `db`. This service runs our Postgres database container.

- This service uses the `postgres:13.1-alpine` image.
- `restart` is set to `always`. Once the container is started it will restart anytime it stops or fails.
- The `container_name` is set to `db` to allow for easy identification.
- Two environment variables are set. `POSTGRES_USER` and `POSTGRES_PASSWORD` allow you to define the user credentials for the Postgres database.

#### pgAdmin

The third service defined is `pgadmin`. This service runs our pgAdmin container which is an admin dashboard GUI for the Postgres DB.

- This service uses the `dpage/pgadmin4:latest` image.
- `restart` is set to `always`. Once the container is started it will restart anytime it stops or fails.
- Two environment variables are set. `PGADMIN_DEFAULT_EMAIL` and `PGADMIN_DEFAULT_PASSWORD` these are the admin credentials used to login to the pgAdmin dashboard.
- Port `5050` on the host machine(your machine) is mapped to to port `80` on the container. This will allow you to access the containers port `80` through port `5050` on your machine (the host).
- This service depends on the [db](#db) service. Therefore, `pgadmin` will start after the [db](#db) service is running.

#### Subscriber

The fourth service defined is `sub`. This service runs our subscriber microservice which listens for events on the topic `user/create`. Upon receiving an event, the user info is extracted from the event and used to populate the PostgreSQL database.

- The service is based off an image called `user-service-sub:latest`.
- `restart` is set to `on-failure`. Once the container is started it will restart anytime it  fails. This is especially helpful when waiting for the Solace Event Broker to start up.
- The build context specifies the image will be built using a DockerFile within the `/subscriber` directory. This DockerFile will buil the `user-service-sub:latest`.
- The `sub` service `depends_on` `db` and `solace` services. Therefore, will start after those services as they are required for `sub` to run.
- There are three environment variables to set. `SPRING_DATASOURCE_URL` specifies the database URL so Spring knows where to connect to. `SPRING_DATA_SOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD` specify the credentials used to log into the database.

#### Publisher

The fifth service defined is `pub`. This service reads a `.csv` file of mock data and publishes and event for each row in the file.

- The service is based off an image called `user-service-pub:latest`.
- `restart` is set to `on-failure`. Once the container is started it will restart anytime it  fails. This is especially helpful when waiting for the Solace Event Broker to start up.
- The build context specifies the image will be built using a DockerFile within the `/publisher` directory. This DockerFile will buil the `user-service-pub:latest`.
- The `sub` service `depends_on` the `solace` service. Therefore, will start after the `solace` service as it is required for `sub` to run.

## Running the Containers

Now return to the [/event-driven-user-service](/) directory.

To run the containers defined in [compose.yml](/compose.yml):

```bash
docker-compose up -d
```

To stop and remove the containers:

```bash
docker-compose down
```

## Access the pgAdmin Dashboard and View Data

**NOTE - The Solace Event Broker can take around a minute or so to start up fully. Until the Event Broker is running, events can not be published. Therefore, initially the Users table will not exist as it relies on the subscriber service to create the table.**

The pgAdmin dashboard is available at [http://localhost:5050](http://localhost:5050). The first time accessing the dashboard you will have to login, it may take around a minute for it to be accessible after intial start up.  

- Your email address will be whatever you defined in the [pgadmin](#pgadmin) service environment variable `PGADMIN_DEFAULT_EMAIL`.
- Your password will be whatever you defined in the [pgadmin](#pgadmin) service environment variable `PGADMIN_DEFAULT_PASSWORD`.

Once successfully logged in, you will be brought to the dashboard home page. To view the Postgres database defined in [db](#db), you will need to click `Add New Server`.

- In the `General` tab, give your server the name `db`.
- Navigate to the `Connection` tab.
- For `Host name/address` we can use the name of the Postgres service [db](#db).
- Make sure the port is `5432`.
- `Username` is the `POSTGRES_USER` defined in the [db](#db) service.
- `Password` is the `POSTGRES_PASSWORD` defined in the [db](#db) service.
- Then click save and you should see the `db` server under `Servers` in the `Object Explorer`.

Now if you look at `Databases`, you will see `compose-postgres` this is the Postgres database holding the `Users` table for our Spring Boot API. Here you can explore the Postgres database and manage it using the admin UI.

### View Database Data
There are two two methods to view the data.
#### Method One
1. Under `compose-postgres`, open the `Schemas` tab.
2. Next, open the `public` tab.
3. Open the `Tables` tab.
4. Right click `users`, select `View/Edit Data` and finally click All Rows.

#### Method Two
1. Right click the `compose-postgres` tab.
2. Select `Query Tool`
3. Paste the folowing command
```SQL
SELECT * FROM public.users
ORDER BY id ASC 
```
5. Press the play/execute script button button or `F5`.
