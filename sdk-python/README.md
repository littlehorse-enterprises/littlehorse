# LittleHorse Python SDK

For documentation on how to use this library, please go to [the LittleHorse website](https://littlehorse.io).

For examples go to the [examples](./examples/) folder.

## Dependencies

- Install python.
- Install [pipx](https://github.com/pypa/pipx): `brew install pipx`
- Install [poetry](https://python-poetry.org/): `pipx install poetry`
- Install [poetry shell plugin](https://github.com/python-poetry/poetry-plugin-shell): `poetry self add poetry-plugin-shell`

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

## Validate Indentations
```
poetry run ruff check .
```

## Validate types
```
poetry run mypy .
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

## Python Code Formatter 

```
poetry shell
black . 
```