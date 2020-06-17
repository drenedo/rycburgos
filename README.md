This project uses Quarkus framework. Modules: Jackson, Panache and Postgresql

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```
./mvnw quarkus:dev
```

## Packaging and running the application

The application is packageable using `./mvnw package`.
It produces the executable `cyrburgos-1.0.0-SNAPSHOT-runner.jar` file in `/target` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/lib` directory.

The application is now runnable using `java -jar target/cyrburgos-1.0.0-SNAPSHOT-runner.jar`.

## Creating a native executable

You can create a native executable using: `./mvnw package -Pnative`.

Or you can use Docker to build the native executable using: `./mvnw package -Pnative -Dquarkus.native.container-build=true`.

You can then execute your binary: `./target/cyrburgos-1.0.0-SNAPSHOT-runner`

## Useful cheat sheet
https://lordofthejars.github.io/quarkus-cheat-sheet/

## Generated map

Last update from 10AM 16/06/2020 GMT +2 

http://umap.openstreetmap.fr/es/map/radares-y-controles-burgos_467980#9/42.4437/-3.4442
