"""Support for LittleHorse StructDef types.

This module provides the ``@lh_struct_def`` class decorator and related
utilities for defining, introspecting, serializing, and registering
LittleHorse ``StructDef`` types in Python.

Example usage::

    from littlehorse.lh_struct import lh_struct_def, LHStructField
    from typing import Annotated

    @lh_struct_def(name="car")
    class Car:
        make: str
        model: str
        year: int = 1970
        vin_number: Annotated[str, LHStructField(masked=True)]

"""

from __future__ import annotations

import datetime
import logging
from typing import (
    Any,
    Annotated,
    Optional,
    get_args,
    get_origin,
    get_type_hints,
)

from littlehorse.config import LHConfig
from littlehorse.model import (
    InlineStructDef,
    PutStructDefRequest,
    StructDefCompatibilityType,
    StructDefId,
    StructFieldDef,
    TypeDefinition,
    VariableType,
    VariableValue,
    WfRunId,
)
from littlehorse.model.variable_pb2 import InlineStruct, Struct, StructField

# ---------------------------------------------------------------------------
# Sentinel / metadata helpers
# ---------------------------------------------------------------------------

# Attribute name stored on decorated classes
_LH_STRUCT_DEF_ATTR = "__lh_struct_def__"


class _LHStructDefInfo:
    """Internal metadata attached to ``@lh_struct_def``-decorated classes."""

    __slots__ = ("name", "description")

    def __init__(self, name: str, description: str = "") -> None:
        self.name = name
        self.description = description


class LHStructField:
    """Metadata marker used with ``typing.Annotated`` to customise a
    struct field.

    Parameters
    ----------
    name : str, optional
        Override the field name that will be stored in the ``StructDef``.
        By default, the Python attribute name is converted from
        ``snake_case`` to ``camelCase``.
    masked : bool
        If ``True`` the field is treated as sensitive data.

    Example::

        from typing import Annotated
        vin: Annotated[str, LHStructField(masked=True)]
    """

    __slots__ = ("name", "masked")

    def __init__(self, name: str = "", masked: bool = False) -> None:
        self.name = name
        self.masked = masked


class LHStructIgnore:
    """Metadata marker used with ``typing.Annotated`` to skip a field.

    Example::

        from typing import Annotated
        internal_id: Annotated[int, LHStructIgnore()]
    """

    pass


# ---------------------------------------------------------------------------
# Decorator
# ---------------------------------------------------------------------------


def lh_struct_def(
    name: str,
    description: str = "",
) -> Any:
    """Class decorator that marks a Python class as a LittleHorse ``StructDef``.

    The decorated class must use **type annotations** for its fields
    (standard Python class-level annotations).

    Fields without a default value are treated as **required**.
    Fields with a default value will have that default stored in the
    ``StructFieldDef``.

    Parameters
    ----------
    name : str
        The StructDef name as registered in LittleHorse.
    description : str, optional
        Human-readable description.

    Returns
    -------
    Callable
        A class decorator.
    """

    def decorator(cls: type) -> type:
        info = _LHStructDefInfo(name=name, description=description)
        setattr(cls, _LH_STRUCT_DEF_ATTR, info)

        # Generate an __init__ if the class doesn't define its own.
        if "__init__" not in cls.__dict__:
            _generate_init(cls)

        return cls

    return decorator


def _generate_init(cls: type) -> None:
    """Synthesize an ``__init__`` method for a ``@lh_struct_def`` class.

    Required fields (no default) come first, fields with defaults after.
    All parameters are keyword-only for clarity.
    """
    hints = get_type_hints(cls, include_extras=True)

    required: list[str] = []
    has_default: list[tuple[str, Any]] = []

    for attr_name, _attr_type in hints.items():
        if attr_name.startswith("_"):
            continue
        # Check for LHStructIgnore
        origin = get_origin(_attr_type)
        if origin is Annotated:
            for arg in get_args(_attr_type)[1:]:
                if isinstance(arg, LHStructIgnore):
                    break
            else:
                # Not ignored
                default = getattr(cls, attr_name, _SENTINEL)
                if default is _SENTINEL:
                    required.append(attr_name)
                else:
                    has_default.append((attr_name, default))
            continue
        default = getattr(cls, attr_name, _SENTINEL)
        if default is _SENTINEL:
            required.append(attr_name)
        else:
            has_default.append((attr_name, default))

    # Build parameter names in order: required first, then defaulted
    all_fields = required + [name for name, _ in has_default]
    defaults = {name: val for name, val in has_default}

    def __init__(self: Any, **kwargs: Any) -> None:
        for field in all_fields:
            if field in kwargs:
                setattr(self, field, kwargs[field])
            elif field in defaults:
                setattr(self, field, defaults[field])
            else:
                raise TypeError(
                    f"{cls.__name__}() missing required keyword argument: '{field}'"
                )

    cls.__init__ = __init__  # type: ignore[attr-defined]


# ---------------------------------------------------------------------------
# Introspection helpers
# ---------------------------------------------------------------------------


def is_lh_struct(cls: type) -> bool:
    """Return ``True`` if *cls* has been decorated with ``@lh_struct_def``."""
    return hasattr(cls, _LH_STRUCT_DEF_ATTR)


def get_struct_def_info(cls: type) -> _LHStructDefInfo:
    """Return the ``_LHStructDefInfo`` attached to *cls*.

    Raises ``TypeError`` if *cls* is not an ``@lh_struct_def`` class.
    """
    info = getattr(cls, _LH_STRUCT_DEF_ATTR, None)
    if info is None:
        raise TypeError(f"{cls.__name__} is not decorated with @lh_struct_def")
    return info


def get_struct_def_name(cls: type) -> str:
    """Return the StructDef name for *cls*."""
    return get_struct_def_info(cls).name


# ---------------------------------------------------------------------------
# snake_case -> camelCase conversion
# ---------------------------------------------------------------------------


def _snake_to_camel(name: str) -> str:
    """Convert a ``snake_case`` name to ``camelCase``.

    Examples::

        >>> _snake_to_camel("first_name")
        'firstName'
        >>> _snake_to_camel("is_sold")
        'isSold'
        >>> _snake_to_camel("vin_number_iso3779")
        'vinNumberIso3779'
        >>> _snake_to_camel("name")
        'name'
    """
    components = name.split("_")
    return components[0] + "".join(_capitalize_segment(c) for c in components[1:])


def _capitalize_segment(segment: str) -> str:
    """Capitalize a segment, handling all-digit or all-lower strings."""
    if not segment:
        return segment
    return segment[0].upper() + segment[1:]


# ---------------------------------------------------------------------------
# Python type -> LH TypeDefinition mapping
# ---------------------------------------------------------------------------

_PYTHON_TYPE_TO_LH_VARIABLE_TYPE: dict[type, VariableType] = {
    str: VariableType.STR,
    int: VariableType.INT,
    float: VariableType.DOUBLE,
    bool: VariableType.BOOL,
    bytes: VariableType.BYTES,
    datetime.datetime: VariableType.TIMESTAMP,
    WfRunId: VariableType.WF_RUN_ID,
    dict: VariableType.JSON_OBJ,
    list: VariableType.JSON_ARR,
}


def _python_type_to_type_definition(
    python_type: type,
) -> TypeDefinition:
    """Map a Python type annotation to a ``TypeDefinition`` protobuf.

    Handles:
    - Primitive types (str, int, float, bool, bytes, datetime, WfRunId)
    - dict / list → JSON_OBJ / JSON_ARR
    - ``@lh_struct_def``-decorated classes → StructDefId reference
    - ``Annotated[...]`` wrappers (peels them off)
    """
    masked = False

    # Peel Annotated
    origin = get_origin(python_type)
    if origin is Annotated:
        args = get_args(python_type)
        python_type = args[0]
        for arg in args[1:]:
            if isinstance(arg, LHStructField):
                masked = arg.masked
            if isinstance(arg, LHStructIgnore):
                # Caller should have already filtered these out
                pass
        # Re-resolve origin after peeling
        origin = get_origin(python_type)

    # Check for struct
    if isinstance(python_type, type) and is_lh_struct(python_type):
        return TypeDefinition(
            struct_def_id=StructDefId(name=get_struct_def_name(python_type)),
            masked=masked,
        )

    # Check for generic dict/list
    if origin is dict:
        return TypeDefinition(primitive_type=VariableType.JSON_OBJ, masked=masked)
    if origin is list:
        return TypeDefinition(primitive_type=VariableType.JSON_ARR, masked=masked)

    # Primitive types
    lh_type = _PYTHON_TYPE_TO_LH_VARIABLE_TYPE.get(python_type)
    if lh_type is not None:
        return TypeDefinition(primitive_type=lh_type, masked=masked)

    raise TypeError(f"Unsupported type {python_type} for LittleHorse StructDef field")


# ---------------------------------------------------------------------------
# Struct field introspection
# ---------------------------------------------------------------------------


class _StructProperty:
    """Represents one field of a ``@lh_struct_def``-decorated class."""

    __slots__ = (
        "python_name",
        "field_name",
        "python_type",
        "type_def",
        "default_value",
        "ignored",
        "masked",
    )

    def __init__(
        self,
        python_name: str,
        raw_type: type,
        default: Any,
    ) -> None:
        self.python_name = python_name
        self.ignored = False
        self.masked = False
        self.field_name = _snake_to_camel(python_name)
        self.default_value = default

        # Inspect Annotated metadata
        origin = get_origin(raw_type)
        inner_type = raw_type
        if origin is Annotated:
            args = get_args(raw_type)
            inner_type = args[0]
            for arg in args[1:]:
                if isinstance(arg, LHStructIgnore):
                    self.ignored = True
                    return
                if isinstance(arg, LHStructField):
                    self.masked = arg.masked
                    if arg.name:
                        self.field_name = arg.name

        self.python_type = inner_type
        self.type_def = _python_type_to_type_definition(raw_type)
        if self.masked:
            self.type_def = TypeDefinition(
                primitive_type=(
                    self.type_def.primitive_type
                    if self.type_def.HasField("primitive_type")
                    else 0
                ),
                struct_def_id=(
                    self.type_def.struct_def_id
                    if self.type_def.HasField("struct_def_id")
                    else None
                ),
                masked=True,
            )

    def to_struct_field_def(self) -> StructFieldDef:
        """Build a ``StructFieldDef`` protobuf from this property."""
        from littlehorse.utils import to_variable_value as _to_var_val

        default_vv: Optional[VariableValue] = None
        if self.default_value is not _SENTINEL:
            default_vv = _to_var_val(self.default_value)

        return StructFieldDef(
            field_type=self.type_def,
            default_value=default_vv,
        )


# Sentinel for "no default"
_SENTINEL = object()


def _get_struct_properties(cls: type) -> list[_StructProperty]:
    """Return the list of ``_StructProperty`` for an ``@lh_struct_def`` class."""
    if not is_lh_struct(cls):
        raise TypeError(f"{cls.__name__} is not decorated with @lh_struct_def")

    hints = get_type_hints(cls, include_extras=True)
    properties: list[_StructProperty] = []

    for attr_name, attr_type in hints.items():
        if attr_name.startswith("_"):
            continue

        # Determine default value
        default = getattr(cls, attr_name, _SENTINEL)

        prop = _StructProperty(attr_name, attr_type, default)
        if not prop.ignored:
            properties.append(prop)

    return properties


# ---------------------------------------------------------------------------
# Build InlineStructDef from class
# ---------------------------------------------------------------------------


def class_to_inline_struct_def(cls: type) -> InlineStructDef:
    """Build an ``InlineStructDef`` protobuf from a ``@lh_struct_def`` class.

    Parameters
    ----------
    cls : type
        A class decorated with ``@lh_struct_def``.

    Returns
    -------
    InlineStructDef
    """
    properties = _get_struct_properties(cls)
    fields: dict[str, StructFieldDef] = {}
    for prop in properties:
        fields[prop.field_name] = prop.to_struct_field_def()
    return InlineStructDef(fields=fields)


def class_to_put_struct_def_request(
    cls: type,
    allowed_updates: StructDefCompatibilityType = StructDefCompatibilityType.NO_SCHEMA_UPDATES,
) -> PutStructDefRequest:
    """Build a ``PutStructDefRequest`` for a ``@lh_struct_def`` class."""
    info = get_struct_def_info(cls)
    return PutStructDefRequest(
        name=info.name,
        description=info.description or "",
        struct_def=class_to_inline_struct_def(cls),
        allowed_updates=allowed_updates,
    )


# ---------------------------------------------------------------------------
# Serialization: Python object -> Struct proto
# ---------------------------------------------------------------------------


def serialize_to_struct(obj: Any) -> Struct:
    """Serialize a ``@lh_struct_def``-decorated instance to a ``Struct`` proto.

    Parameters
    ----------
    obj : Any
        An instance of a ``@lh_struct_def``-decorated class.

    Returns
    -------
    Struct
    """
    from littlehorse.utils import to_variable_value as _to_var_val

    cls = type(obj)
    info = get_struct_def_info(cls)
    properties = _get_struct_properties(cls)

    inline = InlineStruct()
    for prop in properties:
        value = getattr(obj, prop.python_name, None)

        # Nested struct
        if isinstance(value, type) or (value is not None and is_lh_struct(type(value))):
            field_val = VariableValue(struct=serialize_to_struct(value))
        else:
            field_val = _to_var_val(value)

        inline.fields[prop.field_name].CopyFrom(StructField(value=field_val))

    return Struct(
        struct_def_id=StructDefId(name=info.name),
        struct=inline,
    )


# ---------------------------------------------------------------------------
# Deserialization: Struct proto -> Python object
# ---------------------------------------------------------------------------


def deserialize_struct(struct: Struct, cls: type) -> Any:
    """Deserialize a ``Struct`` protobuf into an instance of *cls*.

    Parameters
    ----------
    struct : Struct
        The protobuf ``Struct`` message.
    cls : type
        The target ``@lh_struct_def``-decorated class.

    Returns
    -------
    An instance of *cls*.
    """
    from littlehorse.utils import extract_value as _extract_value

    if not is_lh_struct(cls):
        raise TypeError(f"{cls.__name__} is not decorated with @lh_struct_def")

    instance = cls.__new__(cls)
    properties = _get_struct_properties(cls)
    fields_map = struct.struct.fields

    for prop in properties:
        if prop.field_name not in fields_map:
            # Use the default if present
            if prop.default_value is not _SENTINEL:
                setattr(instance, prop.python_name, prop.default_value)
            else:
                setattr(instance, prop.python_name, None)
            continue

        field_value: VariableValue = fields_map[prop.field_name].value

        # Nested struct
        if (
            field_value.WhichOneof("value") == "struct"
            and isinstance(prop.python_type, type)
            and is_lh_struct(prop.python_type)
        ):
            setattr(
                instance,
                prop.python_name,
                deserialize_struct(field_value.struct, prop.python_type),
            )
        else:
            setattr(instance, prop.python_name, _extract_value(field_value))

    return instance


# ---------------------------------------------------------------------------
# Registration
# ---------------------------------------------------------------------------


def create_struct_def(
    cls: type,
    config: LHConfig,
    allowed_updates: StructDefCompatibilityType = StructDefCompatibilityType.NO_SCHEMA_UPDATES,
    timeout: Optional[int] = None,
) -> None:
    """Register a ``@lh_struct_def`` class as a ``StructDef`` on the server.

    Any nested ``@lh_struct_def`` dependencies must be registered
    separately before calling this function.

    Parameters
    ----------
    cls : type
        A class decorated with ``@lh_struct_def``.
    config : LHConfig
        The LittleHorse configuration.
    allowed_updates : StructDefCompatibilityType
        Schema evolution compatibility type.
    timeout : int, optional
        gRPC timeout in seconds.
    """
    stub = config.stub()
    request = class_to_put_struct_def_request(cls, allowed_updates)
    stub.PutStructDef(request, timeout=timeout)
    logging.info(f"Registered StructDef: {get_struct_def_name(cls)}")


# ---------------------------------------------------------------------------
# Lightweight deserialization (Struct -> dict) for extract_value
# ---------------------------------------------------------------------------


def _extract_struct_to_dict(struct: Struct) -> dict[str, Any]:
    """Convert a ``Struct`` proto to a plain Python dict.

    This is used by ``extract_value`` when no target class is known.
    Nested structs become nested dicts.
    """
    from littlehorse.utils import extract_value as _extract_value

    result: dict[str, Any] = {}
    for field_name, struct_field in struct.struct.fields.items():
        val: VariableValue = struct_field.value
        if val.WhichOneof("value") == "struct":
            result[field_name] = _extract_struct_to_dict(val.struct)
        else:
            result[field_name] = _extract_value(val)
    return result
