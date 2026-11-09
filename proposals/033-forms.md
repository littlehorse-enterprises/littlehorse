# Versioned Forms for Struct-Backed User Tasks

Author: Jake Rose

## Table of Contents

- [Motivation](#motivation)
- [Proposal](#proposal)
- [Protobuf Changes](#protobuf-changes)
  - [Existing Messages](#existing-messages)
  - [object_id.proto](#object_idproto)
  - [form.proto](#formproto)
  - [service.proto](#serviceproto)
- [Annotations For Forms](#annotations-for-forms)
  - [Annotations Are a Convenience, Not the Source of Truth](#annotations-are-a-convenience-not-the-source-of-truth)
- [Alternative: UI Metadata on StructDef](#alternative-ui-metadata-on-structdef)
- [Key Features to Support Industry-Standard Forms](#key-features-to-support-industry-standard-forms)
  - [Feature Priority Matrix](#feature-priority-matrix)
  - [Initial Schema](#initial-schema)
    - [Basic Input Components](#basic-input-components)
    - [Field Labels, Descriptions, and Placeholders](#field-labels-descriptions-and-placeholders)
    - [Required Fields and Validation](#required-fields-and-validation)
    - [Dropdown, Radio, and Checkbox](#dropdown-radio-and-checkbox)
    - [Form Versioning](#form-versioning)
    - [Multiple Pages](#multiple-pages)
    - [Conditional Field Visibility](#conditional-field-visibility)
    - [Nested Objects](#nested-objects)
    - [Read-Only and Disabled Fields](#read-only-and-disabled-fields)
    - [Save Draft and Resume](#save-draft-and-resume)
- [Keeping Forms in Sync with Structs](#keeping-forms-in-sync-with-structs)
  - [Form and StructDef Compatibility](#form-and-structdef-compatibility)
- [Clients](#clients)

## Motivation

The Struct-backed User Task implementation defines typed output but user task still lack a configurable way to display complex data collection.

Form separates presentation from the data contract, supporting widgets and
multi-page forms and conditional visibility without requiring UI metadata in
StructDef. Annotations on the classes registered as StructDefs can still provide
a convenient way to generate these forms.

## Proposal

Add a versioned Form that references a StructDef and contains ordered pages
of fields. UserTaskDef optionally references the form. UserTaskRun retains its
resolved version.

## Protobuf Changes

New fields below extend existing messages after the Struct-backed implementation.

### Existing Messages

```protobuf
// user_tasks.proto
message UserTaskDef {
  // ... existing fields unchanged.
  // Optional form used to collect this task's output.
  // Allows client to reference pinned version of the form.
  FormReference form = 7;
}

// service.proto
message PutUserTaskDefRequest {
  // ... existing fields unchanged.
  // Optional form to associate with the task definition.
  FormReference form = 5;
}

// user_tasks.proto
message UserTaskRun {
  // ... existing fields unchanged.
  // Exact form version retained for this task run.
  FormId form_id = 15;
}

```

### object_id.proto

```protobuf
message FormId {
  // Name of the form.
  string name = 1;
  // Exact revision of the named form.
  int32 version = 2;
}
```

### form.proto



```protobuf
message FormReference {
  // Name of the form to use.
  string name = 1;
  // Exact revision to use; omitted means latest, while 0 is explicit.
  optional int32 version = 2;
}

message Form {
  // Unique name and version of this form.
  FormId id = 1;
  // Schema that the collected answers must satisfy.
  StructDefId result_struct_def_id = 2;
  // Optional heading displayed for the form.
  optional string title = 3;
  // Form pages in display order.
  repeated FormPage pages = 4;
  // Time this form version was created.
  google.protobuf.Timestamp created_at = 5;
}

message FormPage {
  // Optional heading displayed for the page.
  optional string title = 1;
  // Input fields in display order.
  repeated FormField fields = 2;
  // Boolean expression evaluated against draft answers; unset means always visible.
  VariableAssignment visible_when = 3;
}

message FormField {
  // Root-relative path to the answer within the result Struct.
  LHPath field_path = 1;
  // Input control used to render the field.
  FormWidget widget = 2;
  // Optional display label for the input.
  optional string label = 3;
  // Optional guidance displayed alongside the input.
  optional string help_text = 4;
  // Optional hint displayed while the input is empty.
  optional string placeholder = 5;
  // Choices available for selection-based widgets.
  repeated FormOption options = 6;
  // Boolean expression evaluated against draft answers; unset means always visible.
  VariableAssignment visible_when = 7;
  // Boolean expression evaluated against draft answers; unset means editable.
  VariableAssignment read_only_when = 8;
  // Boolean expression evaluated against draft answers; unset means enabled.
  VariableAssignment disabled_when = 9;
}

message FormOption {
  // Display text for this choice.
  string label = 1;
  // Answer value represented by this choice.
  VariableValue value = 2;
}

enum FormWidget {
  FORM_WIDGET_AUTO = 0;
  FORM_WIDGET_TEXT = 1;
  FORM_WIDGET_TEXTAREA = 2;
  FORM_WIDGET_NUMBER = 3;
  FORM_WIDGET_CHECKBOX = 4;
  FORM_WIDGET_SELECT = 5;
  FORM_WIDGET_RADIO = 6;
  FORM_WIDGET_MULTISELECT = 7;
  FORM_WIDGET_DATE = 8;
  FORM_WIDGET_TIME = 9;
  FORM_WIDGET_DATETIME = 10;
  FORM_WIDGET_EMAIL = 11;
  FORM_WIDGET_PASSWORD = 12;
  FORM_WIDGET_PHONE = 13;
  FORM_WIDGET_TOGGLE = 14;
}

```

### service.proto

```protobuf
message PutFormRequest {
  // Name of the form to register.
  string name = 1;
  // Schema that the collected answers must satisfy.
  StructDefId result_struct_def_id = 2;
  // Optional heading displayed for the form.
  optional string title = 3;
  // Form pages in display order.
  repeated FormPage pages = 4;
}

message GetFormRequest {
  // Name of the form to fetch.
  string name = 1;
  // Exact revision to fetch; omitted means latest, while 0 is explicit.
  optional int32 version = 2;
}

message SearchFormRequest {
  // Pagination token from a previous search response.
  optional bytes bookmark = 1;
  // Maximum number of results to return.
  optional int32 limit = 2;
  // Selects the form search filter.
  oneof form_criteria {
    // Match form names starting with this prefix.
    string prefix = 3;
    // Match this exact form name.
    string name = 4;
  }
}

message FormIdList {
  // Form identifiers returned by the search.
  repeated FormId results = 1;
  // Pagination token used to request the next page.
  optional bytes bookmark = 2;
}

rpc PutForm(PutFormRequest) returns (Form) {}
rpc GetForm(GetFormRequest) returns (Form) {}
rpc SearchForm(SearchFormRequest) returns (FormIdList) {}
```

## Annotations For Forms

We can keep Forms separate from StructDefs while using annotations to make
forms easier to create. This lets backend developers define a form's UI
alongside their object classes when needed, without storing UI metadata in
the StructDef itself.

An `@LHForm` annotation on a class registered with `@LHStructDef` would define
the form's name and title. Field annotations would define labels, input
controls, and other UI settings. The SDK would use these annotations to
build a separate Form definition and submit it through `PutFormRequest`.
The resulting Form would reference the StructDef but have its own versions.

For example, the following illustrates a possible Java authoring API, not
annotations available today:

```java
@LHStructDef("contact-details")
@LHForm(name = "contact-details-form", title = "Contact details")
public class ContactDetails {
    @LHFormField(label = "Full name", order = 0)
    public String fullName;

    @LHFormField(label = "Email address", widget = FormWidget.FORM_WIDGET_EMAIL, order = 1)
    public String email;
}
```

The SDK would resolve the registered StructDefId, map annotated fields to their
serialized LHPaths, and generate ordered Form fields. Field annotations could
also describe help text, placeholders, options, and page membership. More
complex layouts and cross-field conditions could be configured through an
explicit Form builder when annotations become cumbersome. Both authoring paths
would produce the same Form representation and follow the same validation and
versioning rules.

Generation and publication should be explicit: registering a StructDef alone
must not silently publish a Form. A client can generate a form from its annotated
class, customize it before publication, and associate the published form with a
UserTaskDef. Changes to annotations only affect newly generated and published
forms; existing runs retain their resolved FormId. A schema change alone does
not require generating or publishing a new Form. If developers generate an
updated Form from the changed class, it references the updated StructDefId and
must be published as a new Form version before clients use it.

Backend developers can start with annotations, and UI developers can adjust
the generated Form or create another Form for the same StructDef. Changing
labels or layouts would not require a new StructDef version. The StructDef
still defines the fields, types, and data validation rules; the Form defines
how to display and collect that data.

### Annotations Are a Convenience, Not the Source of Truth

Annotations give developers an optional, convenient way to create Forms.
The published Form version is the source of truth for clients rendering
that version, not the annotations in the source code.

A UI or another application can edit a Form and publish a new version without
changing the annotations. Those edits are not copied back into the source
code, so the annotations may no longer match the latest Form version.
Generating a Form from annotations again should be an explicit choice, not an
automatic step that overwrites those edits. Developers should review the
generated definition before publishing it as a new version.

## Alternative: UI Metadata on StructDef

Instead of creating a separate Form object, UI metadata could be attached
directly to the StructDef. The field annotations described above
could generate this metadata as part of registering the StructDef, rather than
publishing a separate Form version. The metadata could describe labels, input
controls, pages, field order, and rules for when to show a field or page.

This keeps the data schema and its presentation together, but means a UI-only
change creates a new StructDef version. It also gives a StructDef one associated
form layout, while separate Form objects can provide different layouts for the
same StructDef.

| Topic | UI metadata stored in StructDef | Separate Form |
| --- | --- | --- |
| Creating forms | Keeps field types and UI settings together, making a default form easy to generate. | Defines the form separately, but SDK annotations can generate it from the same class. |
| Complex layouts | Could describe pages, field order, and rules that show fields based on other answers. The annotations would need to support these settings. | Stores pages, field order, and rules for showing fields without changing how the Struct's data is organized. |
| Representing different forms with the same StructDef | Gives the StructDef one default form layout. UserTasks that need different layouts would need extra settings to define and choose each layout. Without those settings, each layout would need a separate StructDef with the same fields and types but different UI metadata. | Each UserTask can use a different named Form with the same StructDef. |
| Changing how a form looks | Changing UI metadata requires a new StructDef version, even if the fields and types stay the same. This can be confusing because the data contract gets a new version only because the UI changed. | Creates a new Form version while keeping the same StructDef version. |
| Adding or removing Struct fields | UI settings stay with their fields, so there is less to update separately. Page layouts and rules that use those fields still need to be checked. | The existing Form can stay unchanged if it still works with the new StructDef version. A new Form version is needed to change what the UI shows. |
| Reading forms from clients | Clients fetch the StructDef to read its UI metadata. | Clients fetch the Form to read its UI metadata. |
| Runtime behavior | New UserTaskRuns resolve the latest StructDef version when they are created, so they automatically get its updated UI metadata and form layout. No separate Form needs to be published. | New UserTaskRuns resolve the latest StructDef version without automatically changing the Form. The existing Form can still be used if it works with that schema. Updating the UI requires publishing and selecting a new Form version. Existing runs keep their resolved Form version. |

## Key Features to Support Industry-Standard Forms

### Feature Priority Matrix

| Feature | Priority | Why it matters |
| --- | --- | --- |
| Basic input components | P0 | Foundation of every form |
| Field labels, descriptions, placeholders | P0 | Makes fields usable |
| Required fields and validation | P0 | Ensures valid submissions |
| Dropdown, radio, checkbox | P0 | Structured user choices |
| Form versioning | P0 | Allows UserTaskDefs to change presentation without updating userTaskDef |
| Multiple pages | P0 | Supports complex workflows |
| Conditional field visibility | P0 | Enables dynamic forms |
| Nested objects | P0 | Supports nested `StructDef` outputs |
| Default values | P0 | Reduces manual input |
| Read-only and disabled fields | P0 | Supports approvals and review workflows |
| Save draft and resume | P0 | Important for long forms |
| Dynamic dropdown options | P1 | Supports external data |
| File uploads | P1 | Common enterprise requirement |
| Calculated fields | P2 | Useful for financial and operational forms |
| Localization | P2 | Supports international customers |
| PDF form generation | P3 | Supports specialized use cases |

### Initial Schema

The initial schema will cover all P0 features.
Other features in the matrix are outside the initial shcema and may be added as needed.

The sections below describe how these features and the protobuf draft above
work together.

#### Basic Input Components

Relevant schema: `FormField.widget` and `FormWidget` in the
[Form schema draft](#formproto).

An input component is the control a person uses to enter or select one value:
for example, a text box for a name, a number input for an amount, or a checkbox
for a yes/no answer. `FormField.field_path` identifies the Struct field that
stores the answer, and `FormField.widget` tells the client which control to
show. The widget affects presentation only; the answer is still stored using
the field's StructDef type.

For example, if the result StructDef has a string field named `email`, the
Form can point to that field and select `FORM_WIDGET_EMAIL`. The client shows
an email input and submits the entered value as a string. A boolean field such
as `accepted_terms` could use `FORM_WIDGET_CHECKBOX` and submit a boolean.

`FORM_WIDGET_AUTO` lets the client choose a suitable control based on the
field's StructDef type. The server checks at Form publication that an explicit
widget is compatible with the target field's type. The table shows the
proposed widget-to-type mappings:

| Widget | Valid types |
| --- | --- |
| AUTO | Types supported by the client's automatic input selection |
| TEXT, TEXTAREA, EMAIL, PASSWORD, PHONE | STR |
| NUMBER | INT, DOUBLE |
| CHECKBOX, TOGGLE | BOOL |
| SELECT, RADIO | The field's type; every option value must match it |
| MULTISELECT | Array; every option value must match the array's item type |
| DATE | STR containing an ISO 8601 calendar date (`YYYY-MM-DD`) |
| TIME | STR containing an ISO 8601 local time, without a timezone |
| DATETIME | TIMESTAMP, representing an instant in UTC |

These widgets determine how clients enter and display values. They do not add
server-side format restrictions to a StructDef field; applications that need
such restrictions must validate the submitted data separately.

#### Field Labels, Descriptions, and Placeholders

Relevant schema: `FormField.label`, `help_text`, and `placeholder` in the
[Form schema draft](#formproto).

Clients display `label` as the field name, `help_text` as guidance, and
`placeholder` inside an empty input. If no label is provided, clients use the
Struct field's name. These settings affect display only, not the submitted data.

#### Required Fields and Validation

Relevant schema: [StructDef](../schemas/littlehorse/struct_def.proto) and
[TypeDefinition](../schemas/littlehorse/type_definition.proto), with completion
behavior described in the [Struct-backed UserTask proposal](./028-struct-backed-user-tasks.md).

StructDef defines the data rules, including required fields. The form uses
those rules to guide the user, and the server checks the answers when the
UserTaskRun is completed.

Hiding a field only hides its input. If the StructDef requires a value, that
value must still be supplied or filled by a default.

#### Dropdown, Radio, and Checkbox

Relevant schema: `FormField.options`, `FormOption`, and `FormWidget` in the
[Form schema draft](#formproto).

Selection controls use `FormField.options`, with a display label and a typed
value for each choice. A single selection writes one value; a multiselect
writes an array of values. A standalone checkbox writes a boolean.

Options are fixed in the published Form version. The server checks that their
values match the target field's type when the Form is published. StructDef
validation remains responsible for data restrictions; a list of UI options
alone does not prevent an API client from submitting other values.

#### Form Versioning

Relevant schema: [FormId](#object_idproto), `FormReference` and `Form` in the
[Form schema draft](#formproto), and the
[UserTask Form references](#existing-messages).

Publishing a changed Form creates a new version. When a UserTaskRun is created,
it resolves its FormReference to a FormId: the specified version if pinned,
or the latest Form version if unpinned. The UserTaskRun records and keeps that
FormId, so later Form changes do not change a UserTaskRun Form.

The selected Form does not need to reference the latest StructDef version.
The StructDef can evolve without a new Form versiond. The Form's fields and rules must still work with
the UserTaskRun's resolved schema, and completion uses that schema to check
the answers.

#### Multiple Pages

Relevant schema: `Form.pages` and `FormPage` in the
[Form schema draft](#formproto).

Clients display `Form.pages` in order and provide Next and Back controls.
Answers stay in the same draft as the user moves between pages. Page navigation
does not complete the UserTaskRun; completion submits the full set of answers.
Pages whose visibility rules do not pass are skipped.

#### Conditional Field Visibility

Relevant schema: `FormField.visible_when`, `FormPage.visible_when`, and
[`VariableAssignment`](../schemas/littlehorse/common_wfspec.proto) in the
[Form schema draft](#formproto).

Visibility and field state reuse `VariableAssignment` expressions for
comparisons and nested `AND`/`OR` conditions. For example, a field could be
visible when `country == "USA" OR country == "CA"`. Inverse comparators such as
`NOT_EQUALS` and `NOT_IN` are available

In this section, `draft` means the current set of answers, represented as a
Struct that the client constructs and manages while the form is being edited.
It is a special reference for evaluating form conditions, not a workflow
variable or server-held state. The client updates the draft as answers change;
it can separately send the answers to the UserTask API to save progress or
complete the task. Use `lh_path` to select an answer from the draft, such as
`country`.

The initial release supports literals, draft references, comparisons, and
`AND`/`OR`; other assignment sources, operations, casts, JSON paths, and dynamic
path selectors are rejected at publication. Each expression must produce a
boolean, and its paths and operand types must be valid for both the Form's
referenced StructDef and the UserTaskRun's resolved StructDef.

An unset `visible_when` means always visible. For example, the following
nested `VariableAssignment` represents
`(country == "USA" OR country == "CA") AND age >= 18`:

```json
{
  "expression": {
    "lhs": {
      "expression": {
        "lhs": {
          "variableName": "draft",
          "lhPath": {
            "path": [{ "key": "country" }]
          }
        },
        "comparator": "EQUALS",
        "rhs": {
          "literalValue": { "str": "USA" }
        }
      },
      "mutationType": "OR",
      "rhs": {
        "expression": {
          "lhs": {
            "variableName": "draft",
            "lhPath": {
              "path": [{ "key": "country" }]
            }
          },
          "comparator": "EQUALS",
          "rhs": {
            "literalValue": { "str": "CA" }
          }
        }
      }
    },
    "mutationType": "AND",
    "rhs": {
      "expression": {
        "lhs": {
          "variableName": "draft",
          "lhPath": {
            "path": [{ "key": "age" }]
          }
        },
        "comparator": "GREATER_THAN_EQ",
        "rhs": {
          "literalValue": { "int": "18" }
        }
      }
    }
  }
}
```

#### Nested Objects

Relevant schema: `FormField.field_path` in the [Form schema draft](#formproto)
and [StructDef](../schemas/littlehorse/struct_def.proto).

LHPath points to fields inside Structs. Clients collect those answers
into the matching nested objects rather than flattening them into top-level
fields.


#### Read-Only and Disabled Fields

Relevant schema: `FormField.read_only_when` and `disabled_when` in the
[Form schema draft](#formproto).

Each condition uses the same draft expression rules as visibility and is
reevaluated as answers change. An unset condition is false; a literal `true`
can make a field permanently read-only or disabled. Read-only fields show a
value that the user cannot edit. Disabled fields show an unavailable control.
If both conditions are true, disabled takes precedence.



#### Save Draft and Resume

Relevant schema: `SaveUserTaskRunProgressRequest` and `UserTaskRun` in
[user_tasks.proto](../schemas/littlehorse/user_tasks.proto), with Struct output
changes described in the [Struct-backed UserTask proposal](./028-struct-backed-user-tasks.md).

Clients use the existing `SaveProgress` API to save the current answers without
completing the UserTaskRun. When users return, clients load the saved output and
render it using the run's recorded Form version.


## Keeping Forms in Sync with Structs

Form definitions are maintained separately from Struct definitions. When a
Struct version changes, an existing Form can still be used if its fields and
rules work with that version. A new Form version is needed only to change the
form's inputs, layout, or other UI settings. Struct evolution does not
automatically add or remove form inputs or update their layout.

When a `UserTaskRun` is created, it resolves its FormReference to a FormId.
A pinned reference resolves to the specified version; an unpinned reference
resolves to the latest Form version at that time. The UserTaskRun records that
FormId and keeps it for its lifetime. Publishing a newer Form does not change
the display of an existing UserTaskRun.

A Form does not have to reference the exact StructDef version resolved by the
UserTaskRun. A version difference alone is not an error. A UserTaskRun resolves the latest StructDef
version at or above the minimum referenced by its UserTaskDef when it is created.
This does not automatically update the selected Form version. The Form's field
paths, widgets, and rules must still work with the resolved schema, and submitted
answers must satisfy that schema.

### Form and StructDef Compatibility

The server validates a Form's field paths, widgets, options, and conditions
against the StructDef referenced by the Form when the Form is published.
Invalid Forms are rejected with an error identifying the incompatible fields
or conditions.

The Form and StructDef can change independently after publication. For example,
a later StructDef version can remove an optional field used by the Form. The
server therefore validates the selected Form again against the UserTaskRun's
resolved StructDef when creating the run, whether the Form reference is pinned
or resolves to the latest version. The Form and UserTaskRun must refer to the
same named StructDef, but their versions need not match if the Form remains
compatible.

If validation fails, UserTaskRun creation fails rather than creating a task
with an incompatible Form. The error identifies the Form and StructDef versions
and the incompatible fields or conditions. This check can cause an existing
workflow to fail at runtime after a metadata update; it does not prevent schema
drift or replace the separate decision about pinning the UserTaskDef's
StructDef version.

## Clients

The following simpler example illustrates annotations for a basic contact form.
These annotation names are illustrative and are not APIs available today.

```java
@LHStructDef("contact-details")
@LHForm(name = "contact-details-form", title = "Contact details")
public class ContactDetails {
    @LHFormField(label = "Full name")
    public String fullName;

    @LHFormField(label = "Email address", widget = FormWidget.EMAIL,
                 helpText = "We will use this to contact you")
    public String email;

    @LHFormField(label = "Phone number", widget = FormWidget.PHONE,
                 placeholder = "555-0100")
    public String phoneNumber;
}
```


```java
@LHStructDef("loan-application")
@LHForm(name = "loan-application-form", title = "Loan application")
@LHFormPage(name = "applicant", title = "Applicant", order = 0)
@LHFormPage(name = "address", title = "Address", order = 1)
@LHFormPage(name = "employment", title = "Employment", order = 2,
            visibleWhen = "country == 'USA' OR country == 'Canada'")
public class LoanApplication {
    @LHFormField(page = "applicant", label = "Full name",
                 helpText = "Enter your legal name", placeholder = "Jane Doe")
    public String fullName;

    @LHFormField(page = "applicant", label = "Country",
                 widget = FormWidget.FORM_WIDGET_RADIO,
                 options = {
                     @LHFormOption(label = "United States", value = "USA"),
                     @LHFormOption(label = "Canada", value = "Canada")
                 })
    public String country;

    @LHFormField(page = "applicant", label = "I confirm this information is accurate",
                 widget = FormWidget.FORM_WIDGET_CHECKBOX)
    public Boolean confirmed;

    @LHFormField(page = "address", label = "Address")
    public Address address;

    @LHFormField(page = "employment", label = "Employment status",
                 widget = FormWidget.FORM_WIDGET_SELECT,
                 options = {
                     @LHFormOption(label = "Employed", value = "EMPLOYED"),
                     @LHFormOption(label = "Unemployed", value = "UNEMPLOYED")
                 })
    public String employmentStatus;

    @LHFormField(page = "employment", label = "Employer",
                 visibleWhen = "employmentStatus == 'EMPLOYED'",
                 readOnlyWhen = "reviewStatus == 'APPROVED'",
                 disabledWhen = "reviewStatus == 'CLOSED'")
    public String employerName;

    @LHFormField(page = "applicant", label = "Review status",
                 widget = FormWidget.FORM_WIDGET_TEXT, readOnlyWhen = "true")
    public String reviewStatus;
}

@LHStructDef("loan-application-address")
public class Address {
    @LHFormField(label = "Street")
    public String street;

    @LHFormField(label = "City")
    public String city;

    @LHFormField(label = "Postal code")
    public String postalCode;
}
```

As this example shows, annotations can become tricky to read and maintain as
forms gain pages, nested fields, and conditional behavior. The Form builder
should be the primary way to construct complex forms. Annotations remain an
optional convenience for simple forms or for developers who prefer that style.
