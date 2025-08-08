# Type Casting

## Motivation

### Current Problem

LittleHorse currently has strict typing without type conversion, leading to registration or runtime errors:

**Example 1: INT → STR conversion needed**
```java
public static Workflow getStringErrorWorkflow() {
    return new WorkflowImpl(
        "string-error-example",
        wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            wf.execute("string-method", intVar); // ERROR: expects STR but gets INT
        }
    );
}
```

**Error Message:**
```
Exception in thread "main" io.grpc.StatusRuntimeException: INVALID_ARGUMENT: 
PutWfSpecRequest is invalid: ThreadSpec entrypoint invalid: Node 1-string-method-TASK invalid: 
Task input variable with name arg0 at position 0 expects type STR but is type INT
```

**Example 2: INT → DOUBLE conversion needed**
```java
public static Workflow getDoubleErrorWorkflow() {
    return new WorkflowImpl(
        "double-error-example",
        wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            wf.execute("double-method", intVar); // ERROR: expects DOUBLE but gets INT
        }
    );
}
```

**Error Message:**
```
Exception in thread "main" io.grpc.StatusRuntimeException: INVALID_ARGUMENT: 
PutWfSpecRequest is invalid: ThreadSpec entrypoint invalid: Node 1-double-method-TASK invalid: 
Input variable 0 needs to be DOUBLE but cannot be!
```

**Example 3: Runtime INT → DOUBLE conversion error**
```java
public static Workflow getDoubleErrorFromNodeWorkflow() {
    return new WorkflowImpl(
        "double-from-node-error-example",
        wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            NodeOutput result = wf.execute("int-method", intVar); // this returns an INT
            wf.execute("double-method", result);                  // ERROR: expects DOUBLE but gets INT
        }
    );
}
```

**Error Message** (occurs at **workflow runtime**):
```
Failed calculating TaskRun Input Vars: Variable arg0 invalid: should be DOUBLE but is of type INT
```

**Note**: Examples 1 and 2 fail during workflow registration, while Example 3 shows the runtime error when the workflow actually executes and encounters the type mismatch during task execution.

While these error messages indicate the type mismatch, they could be improved to better guide users toward solutions.

## Proposed Solution

We propose implementing **two types of type conversion** for **primitive types only, for now**:

### Scope Limitation
This proposal **only covers primitive types**: `INT`, `DOUBLE`, `STR`, `BOOL`, `BYTES`, and `WF_RUN_ID`. 

**Complex types excluded**: `JSON_OBJ` and `JSON_ARR` are explicitly excluded from this proposal. Type conversions for complex/structured types will be addressed in a future proposal once struct definitions are fully implemented.

### 1. Automatic Conversions (Server-Side)
- **Any primitive type → STR**: All LittleHorse primitive types automatically convert to string
- **INT → DOUBLE**: Integers automatically widen to doubles, since we are putting a lower into a higher type

### 2. Manual Conversions (Explicit Casting)
- All other primitive conversions require explicit `.cast()` calls
- Provides safety for potentially unsafe conversions
- **Important: Casting is non-mutating** - creates a converted copy without modifying the original value

## Design Principles

### Non-Mutating Operations
**Casting never modifies the original variable or value.** Instead, it produces a new value with the target type that can be assigned to variables or passed to task inputs. The original value remains unchanged in its original location.

### Value Resolution Context  
Casting operations are resolved at the point where the value is consumed (variable assignment, task input, etc.), not when the casting expression is evaluated. This ensures type safety and allows the same source value to be cast to different types in different contexts without affecting the original.

**Important**: Calling `stringVar.cast(VariableType.INT)` alone creates a casting expression but does not immediately perform the conversion. The actual type conversion happens when the expression is consumed:

```java
// This creates a casting expression (lazy evaluation)
var castExpression = stringVar.cast(VariableType.INT);

// The actual conversion happens here when consumed:
wf.execute("process-age", castExpression);        // Cast resolved here
intVar.assign(castExpression);                    // Cast resolved here again
```

### Output Assignment
Cast operations produce outputs that can be:
- Assigned to variables: `myVar = sourceVar.cast(INT)`
- Used as task inputs: `wf.execute("task", sourceVar.cast(DOUBLE))`
- Used in any context where a value is consumed

The casting operation creates a new typed value without mutating the source.

## Conversion Rules

**Primitive Types Only**: The following rules apply only to primitive LittleHorse types (`INT`, `DOUBLE`, `STR`, `BOOL`, `BYTES`, `WF_RUN_ID`).

| From Type | To Type | Rule | Examples | Error Messages |
|-----------|---------|------|----------|----------------|
| `INT` | `STR` |  Automatic | `42` → `"42"` | None  |
| `DOUBLE` | `STR` |  Automatic | `42.5` → `"42.5"` | None  |
| `BOOL` | `STR` |  Automatic | `true` → `"true"` | None  |
| `WF_RUN_ID` | `STR` |  Automatic | UUID → string | None  |
| `BYTES` | `STR` |  Automatic | Base64 encoding | None  |
| `INT` | `DOUBLE` |  Automatic | `42` → `42.0` | None  |
| `DOUBLE` | `INT` | 🔧 Manual Cast | ✅ `42.7` → `42` | `No errors but we lose decimal part here` |
| `STR` | `INT` | 🔧 Manual Cast | ✅ `"42"` → `42` ❌ `"abc"` → error | `"Cannot parse 'abc' as INT"` |
| `STR` | `DOUBLE` | 🔧 Manual Cast | ✅ `"42.7"` → `42.7` ❌ `"xyz"` → error | `"Cannot parse 'xyz' as DOUBLE"` |
| `STR` | `BOOL` | 🔧 Manual Cast | ✅ `"true"` → `true` ❌ `"maybe"` → error | `"Cannot parse 'maybe' as BOOL (use 'true'/'false')"` |
| `STR` | `BYTES` | 🔧 Manual Cast | ✅ `"SGVsbG8="` → `Hello` ❌ `"invalid!"` → error | `"Invalid Base64 string: 'invalid!'"` |
| `STR` | `WF_RUN_ID` | 🔧 Manual Cast | ✅ Valid UUID string ❌ `"not-uuid"` → error | `"Invalid UUID format: 'not-uuid'"` |
| `INT` | `BOOL` | ❌ Not Allowed | No clear conversion rule | `"Casting INT to BOOL not supported"` |
| `DOUBLE` | `BOOL` | ❌ Not Allowed | No clear conversion rule | `"Casting DOUBLE to BOOL not supported"` |

**Legend:**
- ✅ Automatic: Server handles conversion automatically
- 🔧 Manual Cast: Requires explicit `.cast()` call
- ❌ Not Allowed: No conversion available

### Excluded Types
- **`JSON_OBJ`**: Complex object conversions excluded until struct definitions are implemented
- **`JSON_ARR`**: Array conversions excluded until struct definitions are implemented
- **Future Proposal**: Complex type casting will be addressed in a separate proposal focused on structured data conversions

## Usage Examples

### Automatic Conversions (No Code Changes Needed)
```java
// This works automatically after server upgrade
public static Workflow getWorkflow() {
    return new WorkflowImpl("casting-example", wf -> {
        WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
        WfRunVariable doubleVar = wf.addVariable("double-var", VariableType.DOUBLE);
        WfRunVariable stringVar = wf.addVariable("string-var", VariableType.STR);
        WfRunVariable booleanVar = wf.addVariable("boolean-var", VariableType.BOOL);

        // Automatic conversions - no casting needed
        wf.execute("string-method", intVar);     // INT → STR (automatic)
        wf.execute("string-method", doubleVar);  // DOUBLE → STR (automatic)
        wf.execute("string-method", booleanVar); // BOOL → STR (automatic)
        wf.execute("double-method", intVar);     // INT → DOUBLE (automatic)
    });
}
```

### Manual Conversions (Explicit Casting)
```java
// For potentially unsafe conversions, explicit casting required
public static Workflow getWorkflow() {
    return new WorkflowImpl("casting-example", wf -> {
        WfRunVariable intVar = wf.addVariable("year-of-birth", VariableType.INT);
        WfRunVariable doubleVar = wf.addVariable("double-var", VariableType.DOUBLE);
        WfRunVariable stringVar = wf.addVariable("string-var", VariableType.STR);

        // Manual casts for potentially unsafe conversions
        wf.execute("int-method", doubleVar.cast(VariableType.INT));    // DOUBLE → INT (loses precision)
        wf.execute("int-method", stringVar.cast(VariableType.INT));    // STR → INT (can fail)
        
        // Casting NodeOutput results
        NodeOutput doubleOutput = wf.execute("double-method", doubleVar); // Returns 3.15
        NodeOutput intOutput = wf.execute("int-method", doubleOutput.cast(VariableType.INT)); // Cast to 3
        
        // IMPORTANT: Casting creates new values, doesn't modify originals
        wf.execute("double-method", doubleOutput); // Original value still 3.15 (unchanged)
        
        // Example: Same source value can be cast to different types
        wf.execute("process-as-int", stringVar.cast(VariableType.INT));      // stringVar → INT
        wf.execute("process-as-double", stringVar.cast(VariableType.DOUBLE)); // stringVar → DOUBLE  
        wf.execute("process-as-string", stringVar);                          // stringVar unchanged (STR)
    });
}
```


**SDK Usage Examples:**
```java
public static Workflow getCastingWorkflow() {
    return new WorkflowImpl("casting-example", wf -> {
        WfRunVariable stringVar = wf.addVariable("user-id", VariableType.STR);
        
        // Cast STR to INT when passing to task (manual casting required)
        wf.execute("process-age", stringVar.cast(VariableType.INT));
    });
}
```

```

### JSON Example with Casting

```json
{
  "nodes": {
    "1-greet-TASK": {
      "task": {
        "taskDefId": {"name": "greet"},
        "variables": [
          {
            "variableName": "input-name",
            "castTo": "INT"
          }
        ]
      }
    }
  }
}
```

This shows casting in task inputs: `"castTo": "INT"` when passing variable to task.

## Implementation

### Protocol Buffer Changes

The casting functionality extends the `VariableAssignment` message with an optional casting field that allows type conversion at the point of value resolution.

#### Extended VariableAssignment with Casting Support

```protobuf
message VariableAssignment {
  // Existing nested messages...
  message FormatString { ... }
  message NodeOutputReference { ... }
  message Expression { ... }

  optional string json_path = 1;

  // The oneof determines where the value is resolved from
  oneof source {
    string variable_name = 2;
    VariableValue literal_value = 3;
    FormatString format_string = 4;
    NodeOutputReference node_output = 5;
    Expression expression = 6;
  }

  // NEW: Optional casting at point of value resolution
  // If specified, the resolved value will be cast to this type before being used.
  // This allows non-mutating type conversions anywhere VariableAssignment is used.
  // IMPORTANT: Original values remain unchanged; casting creates new typed values.
  optional TypeDefinition cast_to = 7;
}
```

#### How Casting Works

**Non-Mutating Type Conversion**: Casting happens at the point of value resolution, making sure that original values are never modified.

```protobuf
// Cast variable to INT when passing to task
TaskNode {
  variables: [
    {
      variable_name: "string-age"
      cast_to: INT
    }
  ]
}

// Cast node output to INT in variable mutation  
VariableMutation {
  lhs_name: "age-value"
  operation: ASSIGN
  rhs_assignment: {
    node_output: { node_name: "get-string-age" }
    cast_to: INT
  }
}
```

### Casting in Variable Mutations

**Java SDK Examples:**
```java
public static Workflow getWorkflow() {
    return new WorkflowImpl("casting-mutations", wf -> {
        WfRunVariable stringVar = wf.addVariable("input-name", VariableType.STR);
        WfRunVariable intVar = wf.addVariable("age", VariableType.INT);
        WfRunVariable doubleVar = wf.addVariable("score", VariableType.DOUBLE);

        // Execute task that returns a string number
        var result = wf.execute("get-age-string", stringVar);  // Returns STR like "25"
        
        // Cast and assign in variable mutations (manual casting required)
        intVar.assign(result.cast(VariableType.INT));          // Cast STR → INT (result unchanged)
        doubleVar.assign(result.cast(VariableType.DOUBLE));    // Cast STR → DOUBLE (result unchanged)
        
        // The original result value remains STR "25" - casting created new typed copies
        wf.execute("process-data", intVar, doubleVar);
    });
}
```

```java
public static Workflow getTaskInputCasting() {
    return new WorkflowImpl("task-input-casting", wf -> {
        WfRunVariable stringVar = wf.addVariable("age-string", VariableType.STR);
        
        // Cast STR to INT when passing to task (manual casting required)
        wf.execute("process-age", stringVar.cast(VariableType.INT));
    });
}
```

```java
// JSON path with casting
var jsonResult = wf.execute("get-user-data", stringVar);  // Returns JSON
intVar.assign(jsonResult.jsonPath("$.age").cast(VariableType.INT));
doubleVar.assign(jsonResult.jsonPath("$.score").cast(VariableType.DOUBLE));
```

## Future Considerations

- **Complex Type Casting**: A separate proposal will address `JSON_OBJ` and `JSON_ARR` conversions once struct definitions are mature



