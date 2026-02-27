"""Tests for littlehorse.lh_struct — StructDef decorator, introspection,
serialization / deserialization, and workflow / worker integration."""

import datetime
import inspect
import unittest
from typing import Annotated

from littlehorse.lh_struct import (
    LHStructField,
    LHStructIgnore,
    _get_struct_properties,
    _snake_to_camel,
    class_to_inline_struct_def,
    class_to_put_struct_def_request,
    deserialize_struct,
    get_struct_def_info,
    get_struct_def_name,
    is_lh_struct,
    lh_struct_def,
    serialize_to_struct,
    _extract_struct_to_dict,
)
from littlehorse.model import (
    InlineStructDef,
    PutStructDefRequest,
    StructDefCompatibilityType,
    StructDefId,
    VariableType,
    VariableValue,
)
from littlehorse.model.variable_pb2 import InlineStruct, Struct, StructField
from littlehorse.utils import extract_value, to_variable_value
from littlehorse.workflow import (
    WfRunVariable,
    Workflow,
    WorkflowThread,
    to_variable_assignment,
)


# ---------------------------------------------------------------------------
# Fixture struct classes
# ---------------------------------------------------------------------------


@lh_struct_def(name="address")
class Address:
    house_number: int
    street: str
    city: str
    planet: str = "Earth"


@lh_struct_def(name="person", description="A person.")
class Person:
    first_name: str
    last_name: str
    home_address: Address


@lh_struct_def(name="annotated-fields")
class AnnotatedFields:
    vin_number: Annotated[str, LHStructField(masked=True)]
    custom_name: Annotated[int, LHStructField(name="myCustomName")]
    ignored: Annotated[str, LHStructIgnore()]
    normal_field: str


@lh_struct_def(name="all-types")
class AllTypes:
    s: str
    i: int
    f: float
    b: bool
    raw: bytes
    ts: datetime.datetime
    obj: dict
    arr: list


# Non-decorated class for negative tests
class PlainClass:
    x: int = 5


# ---------------------------------------------------------------------------
# Decorator & introspection
# ---------------------------------------------------------------------------


class TestLHStructDefDecorator(unittest.TestCase):
    def test_is_lh_struct(self):
        self.assertTrue(is_lh_struct(Address))
        self.assertFalse(is_lh_struct(PlainClass))

    def test_get_struct_def_info(self):
        info = get_struct_def_info(Person)
        self.assertEqual(info.name, "person")
        self.assertEqual(info.description, "A person.")

    def test_get_struct_def_info_not_decorated(self):
        with self.assertRaises(TypeError):
            get_struct_def_info(PlainClass)

    def test_get_struct_def_name(self):
        self.assertEqual(get_struct_def_name(Address), "address")

    def test_decorator_preserves_class(self):
        """The decorator should return the original class (not a subclass)."""
        a = Address.__new__(Address)
        a.house_number = 1
        a.street = "Main St"
        a.city = "Anytown"
        a.planet = "Earth"
        self.assertIsInstance(a, Address)


# ---------------------------------------------------------------------------
# snake_case → camelCase
# ---------------------------------------------------------------------------


class TestSnakeToCamel(unittest.TestCase):
    def test_simple(self):
        self.assertEqual(_snake_to_camel("first_name"), "firstName")

    def test_multiple_words(self):
        self.assertEqual(_snake_to_camel("vin_number_iso3779"), "vinNumberIso3779")

    def test_single_word(self):
        self.assertEqual(_snake_to_camel("name"), "name")

    def test_double_underscore(self):
        # Handles gracefully even if unusual
        result = _snake_to_camel("a__b")
        self.assertEqual(result, "aB")

    def test_trailing_underscore(self):
        result = _snake_to_camel("foo_")
        self.assertEqual(result, "foo")

    def test_already_camel(self):
        result = _snake_to_camel("alreadyCamel")
        self.assertEqual(result, "alreadyCamel")


# ---------------------------------------------------------------------------
# _StructProperty
# ---------------------------------------------------------------------------


class TestStructProperty(unittest.TestCase):
    def test_basic_properties(self):
        props = _get_struct_properties(Address)
        names = {p.python_name for p in props}
        self.assertEqual(names, {"house_number", "street", "city", "planet"})

    def test_field_name_conversion(self):
        props = _get_struct_properties(Address)
        by_python = {p.python_name: p for p in props}
        self.assertEqual(by_python["house_number"].field_name, "houseNumber")
        self.assertEqual(by_python["street"].field_name, "street")

    def test_default_value(self):
        props = _get_struct_properties(Address)
        by_python = {p.python_name: p for p in props}
        from littlehorse.lh_struct import _SENTINEL

        self.assertEqual(by_python["planet"].default_value, "Earth")
        self.assertIs(by_python["street"].default_value, _SENTINEL)

    def test_masked_field(self):
        props = _get_struct_properties(AnnotatedFields)
        by_python = {p.python_name: p for p in props}
        self.assertTrue(by_python["vin_number"].masked)
        self.assertTrue(by_python["vin_number"].type_def.masked)

    def test_custom_name(self):
        props = _get_struct_properties(AnnotatedFields)
        by_python = {p.python_name: p for p in props}
        self.assertEqual(by_python["custom_name"].field_name, "myCustomName")

    def test_ignored_field(self):
        props = _get_struct_properties(AnnotatedFields)
        names = {p.python_name for p in props}
        self.assertNotIn("ignored", names)

    def test_nested_struct_type_def(self):
        props = _get_struct_properties(Person)
        by_python = {p.python_name: p for p in props}
        td = by_python["home_address"].type_def
        self.assertTrue(td.HasField("struct_def_id"))
        self.assertEqual(td.struct_def_id.name, "address")

    def test_not_decorated_raises(self):
        with self.assertRaises(TypeError):
            _get_struct_properties(PlainClass)


# ---------------------------------------------------------------------------
# class_to_inline_struct_def
# ---------------------------------------------------------------------------


class TestClassToInlineStructDef(unittest.TestCase):
    def test_simple_struct(self):
        inline = class_to_inline_struct_def(Address)
        self.assertIsInstance(inline, InlineStructDef)
        self.assertIn("houseNumber", inline.fields)
        self.assertIn("street", inline.fields)
        self.assertIn("city", inline.fields)
        self.assertIn("planet", inline.fields)

    def test_field_types(self):
        inline = class_to_inline_struct_def(Address)
        self.assertEqual(
            inline.fields["houseNumber"].field_type.primitive_type,
            VariableType.INT,
        )
        self.assertEqual(
            inline.fields["street"].field_type.primitive_type,
            VariableType.STR,
        )

    def test_nested_struct(self):
        inline = class_to_inline_struct_def(Person)
        home = inline.fields["homeAddress"]
        self.assertTrue(home.field_type.HasField("struct_def_id"))
        self.assertEqual(home.field_type.struct_def_id.name, "address")

    def test_default_value_present(self):
        inline = class_to_inline_struct_def(Address)
        planet_field = inline.fields["planet"]
        self.assertTrue(planet_field.HasField("default_value"))
        self.assertEqual(planet_field.default_value.str, "Earth")

    def test_all_primitive_types(self):
        inline = class_to_inline_struct_def(AllTypes)
        self.assertEqual(inline.fields["s"].field_type.primitive_type, VariableType.STR)
        self.assertEqual(inline.fields["i"].field_type.primitive_type, VariableType.INT)
        self.assertEqual(
            inline.fields["f"].field_type.primitive_type, VariableType.DOUBLE
        )
        self.assertEqual(
            inline.fields["b"].field_type.primitive_type, VariableType.BOOL
        )
        self.assertEqual(
            inline.fields["raw"].field_type.primitive_type, VariableType.BYTES
        )
        self.assertEqual(
            inline.fields["ts"].field_type.primitive_type, VariableType.TIMESTAMP
        )
        self.assertEqual(
            inline.fields["obj"].field_type.primitive_type, VariableType.JSON_OBJ
        )
        self.assertEqual(
            inline.fields["arr"].field_type.primitive_type, VariableType.JSON_ARR
        )


# ---------------------------------------------------------------------------
# class_to_put_struct_def_request
# ---------------------------------------------------------------------------


class TestClassToPutStructDefRequest(unittest.TestCase):
    def test_builds_request(self):
        req = class_to_put_struct_def_request(Person)
        self.assertIsInstance(req, PutStructDefRequest)
        self.assertEqual(req.name, "person")
        self.assertEqual(req.description, "A person.")
        self.assertIn("firstName", req.struct_def.fields)
        self.assertEqual(
            req.allowed_updates,
            StructDefCompatibilityType.NO_SCHEMA_UPDATES,
        )

    def test_custom_allowed_updates(self):
        req = class_to_put_struct_def_request(
            Address,
            allowed_updates=StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
        )
        self.assertEqual(
            req.allowed_updates,
            StructDefCompatibilityType.FULLY_COMPATIBLE_SCHEMA_UPDATES,
        )


# ---------------------------------------------------------------------------
# Serialization (Python -> Struct proto)
# ---------------------------------------------------------------------------


class TestSerializeToStruct(unittest.TestCase):
    def test_simple_struct(self):
        addr = Address.__new__(Address)
        addr.house_number = 42
        addr.street = "Elm St"
        addr.city = "Springfield"
        addr.planet = "Earth"

        struct = serialize_to_struct(addr)
        self.assertIsInstance(struct, Struct)
        self.assertEqual(struct.struct_def_id.name, "address")
        self.assertEqual(struct.struct.fields["houseNumber"].value.int, 42)
        self.assertEqual(struct.struct.fields["street"].value.str, "Elm St")

    def test_nested_struct(self):
        addr = Address.__new__(Address)
        addr.house_number = 1
        addr.street = "Main"
        addr.city = "Gotham"
        addr.planet = "Earth"

        person = Person.__new__(Person)
        person.first_name = "Bruce"
        person.last_name = "Wayne"
        person.home_address = addr

        struct = serialize_to_struct(person)
        self.assertEqual(struct.struct_def_id.name, "person")
        self.assertEqual(struct.struct.fields["firstName"].value.str, "Bruce")

        # Nested
        inner = struct.struct.fields["homeAddress"].value.struct
        self.assertEqual(inner.struct_def_id.name, "address")
        self.assertEqual(inner.struct.fields["street"].value.str, "Main")


# ---------------------------------------------------------------------------
# Deserialization (Struct proto -> Python)
# ---------------------------------------------------------------------------


class TestDeserializeStruct(unittest.TestCase):
    def _make_address_struct(
        self,
        house_number: int = 42,
        street: str = "Elm St",
        city: str = "Springfield",
        planet: str = "Earth",
    ) -> Struct:
        return Struct(
            struct_def_id=StructDefId(name="address"),
            struct=InlineStruct(
                fields={
                    "houseNumber": StructField(value=VariableValue(int=house_number)),
                    "street": StructField(value=VariableValue(str=street)),
                    "city": StructField(value=VariableValue(str=city)),
                    "planet": StructField(value=VariableValue(str=planet)),
                }
            ),
        )

    def test_simple_deserialize(self):
        struct = self._make_address_struct()
        addr = deserialize_struct(struct, Address)
        self.assertIsInstance(addr, Address)
        self.assertEqual(addr.house_number, 42)
        self.assertEqual(addr.street, "Elm St")
        self.assertEqual(addr.city, "Springfield")

    def test_missing_field_uses_default(self):
        """If a field is missing from the proto, use the class default."""
        struct = Struct(
            struct_def_id=StructDefId(name="address"),
            struct=InlineStruct(
                fields={
                    "houseNumber": StructField(value=VariableValue(int=1)),
                    "street": StructField(value=VariableValue(str="X")),
                    "city": StructField(value=VariableValue(str="Y")),
                    # planet is missing → should use default "Earth"
                }
            ),
        )
        addr = deserialize_struct(struct, Address)
        self.assertEqual(addr.planet, "Earth")

    def test_missing_field_no_default_becomes_none(self):
        """If a required field is missing and has no default, set to None."""
        struct = Struct(
            struct_def_id=StructDefId(name="address"),
            struct=InlineStruct(
                fields={
                    # all missing except planet
                    "planet": StructField(value=VariableValue(str="Mars")),
                }
            ),
        )
        addr = deserialize_struct(struct, Address)
        self.assertIsNone(addr.house_number)
        self.assertEqual(addr.planet, "Mars")

    def test_nested_deserialize(self):
        addr_struct = self._make_address_struct(street="Batcave Rd")
        person_struct = Struct(
            struct_def_id=StructDefId(name="person"),
            struct=InlineStruct(
                fields={
                    "firstName": StructField(value=VariableValue(str="Bruce")),
                    "lastName": StructField(value=VariableValue(str="Wayne")),
                    "homeAddress": StructField(value=VariableValue(struct=addr_struct)),
                }
            ),
        )
        person = deserialize_struct(person_struct, Person)
        self.assertEqual(person.first_name, "Bruce")
        self.assertIsInstance(person.home_address, Address)
        self.assertEqual(person.home_address.street, "Batcave Rd")

    def test_not_decorated_raises(self):
        with self.assertRaises(TypeError):
            deserialize_struct(Struct(), PlainClass)

    def test_roundtrip(self):
        """Serialize then deserialize should produce equivalent object."""
        addr = Address.__new__(Address)
        addr.house_number = 99
        addr.street = "Round Trip Blvd"
        addr.city = "Testville"
        addr.planet = "Mars"

        result = deserialize_struct(serialize_to_struct(addr), Address)
        self.assertEqual(result.house_number, 99)
        self.assertEqual(result.street, "Round Trip Blvd")
        self.assertEqual(result.planet, "Mars")

    def test_nested_roundtrip(self):
        addr = Address.__new__(Address)
        addr.house_number = 7
        addr.street = "Nested Ln"
        addr.city = "Innertown"
        addr.planet = "Earth"

        person = Person.__new__(Person)
        person.first_name = "Test"
        person.last_name = "User"
        person.home_address = addr

        result = deserialize_struct(serialize_to_struct(person), Person)
        self.assertEqual(result.first_name, "Test")
        self.assertEqual(result.home_address.street, "Nested Ln")


# ---------------------------------------------------------------------------
# _extract_struct_to_dict
# ---------------------------------------------------------------------------


class TestExtractStructToDict(unittest.TestCase):
    def test_simple(self):
        struct = Struct(
            struct_def_id=StructDefId(name="address"),
            struct=InlineStruct(
                fields={
                    "houseNumber": StructField(value=VariableValue(int=42)),
                    "street": StructField(value=VariableValue(str="Test")),
                }
            ),
        )
        d = _extract_struct_to_dict(struct)
        self.assertEqual(d["houseNumber"], 42)
        self.assertEqual(d["street"], "Test")

    def test_nested(self):
        inner = Struct(
            struct_def_id=StructDefId(name="inner"),
            struct=InlineStruct(
                fields={"val": StructField(value=VariableValue(str="nested"))}
            ),
        )
        outer = Struct(
            struct_def_id=StructDefId(name="outer"),
            struct=InlineStruct(
                fields={
                    "child": StructField(value=VariableValue(struct=inner)),
                    "name": StructField(value=VariableValue(str="parent")),
                }
            ),
        )
        d = _extract_struct_to_dict(outer)
        self.assertEqual(d["name"], "parent")
        self.assertEqual(d["child"]["val"], "nested")


# ---------------------------------------------------------------------------
# utils.py integration: to_variable_value / extract_value with structs
# ---------------------------------------------------------------------------


class TestUtilsStructIntegration(unittest.TestCase):
    def test_to_variable_value_with_struct_instance(self):
        addr = Address.__new__(Address)
        addr.house_number = 1
        addr.street = "S"
        addr.city = "C"
        addr.planet = "P"

        vv = to_variable_value(addr)
        self.assertEqual(vv.WhichOneof("value"), "struct")
        self.assertEqual(vv.struct.struct_def_id.name, "address")

    def test_extract_value_with_struct(self):
        inner_struct = Struct(
            struct_def_id=StructDefId(name="x"),
            struct=InlineStruct(
                fields={"key": StructField(value=VariableValue(str="val"))}
            ),
        )
        vv = VariableValue(struct=inner_struct)
        result = extract_value(vv)
        self.assertIsInstance(result, dict)
        self.assertEqual(result["key"], "val")


# ---------------------------------------------------------------------------
# workflow.py integration: declare_struct & WfRunVariable
# ---------------------------------------------------------------------------


class TestWorkflowStructIntegration(unittest.TestCase):
    def test_declare_struct_with_class(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("my-addr", Address)
            self.assertEqual(var._struct_def_name, "address")
            self.assertEqual(var.name, "my-addr")

        Workflow("test-declare-class", my_entrypoint).compile()

    def test_declare_struct_with_string(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("my-str-addr", "my-custom-struct")
            self.assertEqual(var._struct_def_name, "my-custom-struct")

        Workflow("test-declare-string", my_entrypoint).compile()

    def test_declare_struct_not_decorated_raises(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            wf.declare_struct("broken", PlainClass)

        with self.assertRaises(TypeError):
            Workflow("test-declare-bad", my_entrypoint).compile()

    def test_compile_struct_variable(self):
        def my_entrypoint(wf: WorkflowThread) -> None:
            wf.declare_struct("addr-var", Address)

        wf_spec = Workflow("compile-struct", my_entrypoint).compile()
        entrypoint = wf_spec.thread_specs[wf_spec.entrypoint_thread_name]
        var_def = entrypoint.variable_defs[0].var_def

        self.assertTrue(var_def.type_def.HasField("struct_def_id"))
        self.assertEqual(var_def.type_def.struct_def_id.name, "address")


# ---------------------------------------------------------------------------
# WfRunVariable.get() and LHPath
# ---------------------------------------------------------------------------


class TestWfRunVariableGet(unittest.TestCase):
    def test_get_string_key(self):
        captured = {}

        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("p", Person)
            sub = var.get("firstName")
            captured["sub"] = sub

        Workflow("test-get-key", my_entrypoint).compile()
        sub = captured["sub"]
        self.assertEqual(len(sub._lh_path), 1)
        self.assertEqual(sub._lh_path[0].key, "firstName")

    def test_get_int_index(self):
        captured = {}

        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("arr-var", "some-struct")
            sub = var.get(3)
            captured["sub"] = sub

        Workflow("test-get-idx", my_entrypoint).compile()
        sub = captured["sub"]
        self.assertEqual(len(sub._lh_path), 1)
        self.assertEqual(sub._lh_path[0].index, 3)

    def test_chained_get(self):
        captured = {}

        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("p2", Person)
            sub = var.get("homeAddress").get("city")
            captured["sub"] = sub

        Workflow("test-chained", my_entrypoint).compile()
        sub = captured["sub"]
        self.assertEqual(len(sub._lh_path), 2)
        self.assertEqual(sub._lh_path[0].key, "homeAddress")
        self.assertEqual(sub._lh_path[1].key, "city")

    def test_get_does_not_mutate_original(self):
        captured = {}

        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("p3", Person)
            _ = var.get("firstName")
            captured["var"] = var

        Workflow("test-no-mutate", my_entrypoint).compile()
        self.assertEqual(len(captured["var"]._lh_path), 0)

    def test_get_with_json_path_raises(self):
        def entrypoint(wf: WorkflowThread) -> None:
            pass

        workflow = Workflow("test-jpget", entrypoint)
        wf_thread = WorkflowThread(workflow, entrypoint)
        var = WfRunVariable("x", VariableType.JSON_OBJ, wf_thread)
        var.json_path = "$.foo"
        with self.assertRaises(ValueError):
            var.get("bar")

    def test_to_variable_assignment_with_lh_path(self):
        captured = {}

        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("p4", Person)
            sub = var.get("homeAddress").get("city")
            captured["sub"] = sub

        Workflow("test-assign-path", my_entrypoint).compile()
        assignment = to_variable_assignment(captured["sub"])
        self.assertTrue(assignment.HasField("lh_path"))
        self.assertEqual(len(assignment.lh_path.path), 2)
        self.assertEqual(assignment.lh_path.path[0].key, "homeAddress")
        self.assertEqual(assignment.lh_path.path[1].key, "city")

    def test_to_variable_assignment_without_lh_path(self):
        captured = {}

        def my_entrypoint(wf: WorkflowThread) -> None:
            var = wf.declare_struct("p5", Person)
            captured["var"] = var

        Workflow("test-assign-no-path", my_entrypoint).compile()
        assignment = to_variable_assignment(captured["var"])
        self.assertFalse(assignment.HasField("lh_path"))


# ---------------------------------------------------------------------------
# worker.py integration: _to_variable_def and _return_to_lh_schema
# ---------------------------------------------------------------------------


class TestWorkerStructIntegration(unittest.TestCase):
    def test_to_variable_def_struct_param(self):
        from littlehorse.worker import _to_variable_def

        def _dummy(addr: Address) -> None:
            pass

        sig = inspect.signature(_dummy)
        param = sig.parameters["addr"]
        vdef = _to_variable_def(param)
        self.assertTrue(vdef.type_def.HasField("struct_def_id"))
        self.assertEqual(vdef.type_def.struct_def_id.name, "address")

    def test_to_variable_def_primitive_param(self):
        from littlehorse.worker import _to_variable_def

        def _dummy(x: str) -> None:
            pass

        sig = inspect.signature(_dummy)
        param = sig.parameters["x"]
        vdef = _to_variable_def(param)
        self.assertEqual(vdef.type_def.primitive_type, VariableType.STR)

    def test_return_to_lh_schema_struct(self):
        from littlehorse.worker import _return_to_lh_schema

        result = _return_to_lh_schema(Person)
        self.assertTrue(result.return_type.HasField("struct_def_id"))
        self.assertEqual(result.return_type.struct_def_id.name, "person")

    def test_return_to_lh_schema_primitive(self):
        from littlehorse.worker import _return_to_lh_schema

        result = _return_to_lh_schema(str)
        self.assertEqual(result.return_type.primitive_type, VariableType.STR)


if __name__ == "__main__":
    unittest.main()
