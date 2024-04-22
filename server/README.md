# LittleHorse Server

## Running LH

The LH Server depends on Kafka as a backend database.

### With your own kafka

Write a `server.config` file with next settings:

```
LHS_KAFKA_BOOTSTRAP_SERVERS=path-to-kafka:9092
LHS_SHOULD_CREATE_TOPICS=true
```

Then:

```
../gradlew run --args="server.config"
```

### Using Scripts

Start a local kafka cluster:

```
../local-dev/setup.sh
```

Next, you can start the LH server itself. The server can be started in a single command:

```
../local-dev/do-server.sh
```

You can confirm that the Server is running via:

```
lhctl search wfSpec
```

Result:

```
{
  "results": []
}
```

## Protobuf Compilation

```
../local-dev/compile-proto.sh
```

## Configurations

- [Server Configurations](../docs/SERVER_CONFIGURATIONS.md)
