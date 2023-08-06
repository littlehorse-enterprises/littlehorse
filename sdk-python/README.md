# LittleHorse GoLang SDK

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.dev).

## Dependencies

- Install python.
- Install [poetry](https://python-poetry.org/): `brew install poetry`
- Install grpc tools: `pip3 install grpcio-tools`

## Initialize

```
poetry install
```

## Protobuf Compilation

```
../local-dev/compile-proto.sh
```

## Run tests

```
poetry run python -m unittest -v
```
