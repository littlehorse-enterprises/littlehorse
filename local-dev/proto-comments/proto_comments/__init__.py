from typing import Any
from proto_schema_parser.parser import Parser
from proto_schema_parser.ast import (
    File,
    Message,
    Comment,
    Field,
    OneOf,
)
import json


def parse_file(file: File) -> dict[str, dict]:
    content = {"messages": {}}
    comment = ""

    for element in file.file_elements:
        if isinstance(element, Comment):
            comment = comment + comment_to_str(element)
        if isinstance(element, Message):
            name = element.name
            message = parse_message(element)

            if comment is not "":
                message["comment"] = comment.strip()
                comment = ""

            content["messages"][name] = message

    return content


def parse_message(message: Message) -> dict[str, Any]:
    content = {"fields": {}, "messages": {}, "oneofs": {}}

    comment = ""

    for element in message.elements:
        if isinstance(element, Comment):
            comment = comment + comment_to_str(element)
        if isinstance(element, OneOf):
            name = element.name
            oneof = parse_oneof(element)

            content["oneofs"][name] = {}

            if comment is not "":
                content["oneofs"][name]["comment"] = comment.strip()
                comment = ""

            for field_name, field_value in oneof["fields"].items():
                content["fields"][field_name] = field_value
        if isinstance(element, Field):
            name = element.name
            field = parse_field(element)

            field["comment"] = comment.strip()
            comment = ""

            content["fields"][name] = field
        if isinstance(element, Message):
            name = element.name
            child_message = parse_message(element)

            child_message["comment"] = comment.strip()
            comment = ""

            content["messages"][name] = child_message
    return content


def parse_field(field: Field) -> dict[str, Any]:
    content = {"type": field.type, "number": field.number}

    if field.cardinality is not None:
        content["cardinality"] = field.cardinality

    return content


def parse_oneof(oneof: OneOf) -> dict[str, dict[str, Any]]:
    content = {"fields": {}}

    comment = ""

    for element in oneof.elements:
        if isinstance(element, Comment):
            comment = comment + comment_to_str(element)
        if isinstance(element, Field):
            name = element.name
            field = parse_field(element)

            field["oneof"] = oneof.name

            if comment is not "":
                field["comment"] = comment.strip()
                comment = ""

            content["fields"][name] = field

    return content


def comment_to_str(comment: Comment) -> str:
    comment_text = comment.text
    if comment_text.startswith("//"):
        comment_text = comment_text[2:]
    return comment_text


def get_sample_1() -> str:
    return """
   syntax = "proto3";

   package littlehorse;

   // A `StructDef` is a versioned metadata object (tenant-scoped) inside LittleHorse
   // that defines the structure and content of a variable value. It allows strong typing.
   message StructDef {
   // The id of the `Schema`. This includes the version.
   StructDefId id = 1;
   }
   """


def get_sample_2() -> str:
    return """
  // Defines the type of a value in LittleHorse. Can be used for Task Parameters,
  // Task return types, External Event types, ThreadSpec variables, etc.
  message TypeDefinition {
    // The type of this definition. One of the following will be set.
    //
    // Note: if no `defined_type` is set, defaults to a `primitive_type` of `JSON_OBJ`
    // to support old clients from 0.14.1 or lower.
    oneof defined_type {
      VariableType primitive_type = 1;

      StructDefId struct_def_id = 5;
    }

    // For compatibility purposes.
    reserved 2, 3;

    // Set to true if values of this type contain sensitive information and must be masked.
    bool masked = 4;
  }
  """


def get_from_file(file_name: str) -> str:
    with open(f"../../schemas/littlehorse/{file_name}.proto", "r") as file:
        return file.read()


def main():
    #  content = get_from_file("struct_def")
    content = get_sample_2()

    result = None

    if content is not None:
        result = Parser().parse(content)

    if result is not None:
        output = parse_file(result)
        print(json.dumps(output))


main()
