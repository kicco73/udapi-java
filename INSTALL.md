# How to install Java Udapi

## Java setup

- You need JDK 17 or higher
- Project developed with: OpenJDK 64-Bit Server VM Temurin-17.0.6+10 on mac (<https://adoptium.net/en-GB/download/>)

## Build Udapi

```bash
git clone https://github.com/udapi/udapi-java.git
cd udapi-java
./gradlew build
```

## Test Udapi

`cd udapi-java/demo/` and run `./demo.sh`.

## Docker image

To build and run the rut server in a docker, issue the commands:

```bash
make image
make run
```
