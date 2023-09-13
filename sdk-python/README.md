# LittleHorse Python SDK

<a href="https://littlehorse.dev/"><img alt="littlehorse.dev" src="https://img.shields.io/badge/-LittleHorse.dev-7f7aff"></a>
<a href="https://github.com/littlehorse-enterprises/littlehorse"><img alt="github" src="https://img.shields.io/badge/-LittleHorse-gray?logo=github&logoColor=white"></a>

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.dev).

For examples go to the [examples](./examples/) folder.

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
poetry shell
python -m unittest discover -v
```

## Useful Commands

Set python version:

```
poetry env use python3.9
```

## Types Map

Task arguments type reference:

```
VariableType.JSON_OBJ: dict[str, Any]
VariableType.JSON_ARR: list[Any]
VariableType.DOUBLE:   float
VariableType.BOOL:     bool
VariableType.STR:      str
VariableType.INT:      int
VariableType.BYTES:    bytes
```
