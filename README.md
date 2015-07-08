### Vert.x 3.0 Web Application
[![Build Status](https://travis-ci.org/maasdi/vertx-web.svg?branch=master)](https://travis-ci.org/maasdi/vertx-web)

This project use to learn how to create simple webapp with Vert.x 3.0.

You can run it directly in your IDE by creating a run configuration that uses the main class `io.vertx.core.Starter`
and passes in the arguments `run app.web.Server`.

The build.gradle uses the Gradle shadowJar plugin to assemble the application and all it's dependencies into a single "fat" jar.

To build the "fat jar"

    1. Make sure you have MySQLServer and execute database scripts (see : src/main/resources/sql/scripts.sql)
    2. Verify app config and change if needed (see : src/main/resources/config.json)
    3. ./gradlew shadowJar

To run the fat jar:

    java -jar build/libs/vertx-web-1.0.0-SNAPSHOT-fat.jar

(You can take that jar and run it anywhere there is a Java 8+ JDK. It contains all the dependencies it needs so you
don't need to install Vert.x on the target machine).

Now point your browser at http://localhost:8080

Writing code in verticles allow you to scale it more easily, e.g. let's say you have 8 cores on your server and you
want to utilise them all, you can deploy 8 instances as follows:

    java -jar build/libs/vertx-web-1.0.0-SNAPSHOT-fat.jar -instances 8

You can also enable clustering and ha at the command line, e.g.

    java -jar build/libs/vertx-web-1.0.0-SNAPSHOT-fat.jar -cluster

    java -jar build/libs/vertx-web-1.0.0-SNAPSHOT-fat.jar -ha

Please see the docs for a full list of Vert.x command line options.

#Things I want todo
- [x] CRUD
- [] Validation, right now there's no input validation
- [x] Upload File sample
- [] Websocket sample, eg: provide chatting box for logged in user
- [] EventBus sample
- [] More on verticle