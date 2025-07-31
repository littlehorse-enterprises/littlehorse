# Type Casting and Coercion in LittleHorse

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
            var result = wf.execute("int-method", intVar); // this returns an INT
            wf.execute("string-method", result);           // ERROR: expects STR but gets INT
        }
    );
}
```

**Error Message:**
```
Exception in thread "main" io.grpc.StatusRuntimeException: INVALID_ARGUMENT: 
PutWfSpecRequest is invalid: ThreadSpec entrypoint invalid: Node 2-string-method-TASK invalid: 
Task input variable with name arg0 at position 0 expects type STR but is type INT
```

**Example 2: INT → DOUBLE conversion needed**
```java
public static Workflow getDoubleErrorWorkflow() {
    return new WorkflowImpl(
        "double-error-example",
        wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            // This workflow fails at registration
            wf.execute("double-method", intVar); // ERROR at registration: expects DOUBLE but gets INT
        }
    );
}
```

**Error Message** (occurs at **workflow registration**):
```
Exception in thread "main" io.grpc.StatusRuntimeException: INVALID_ARGUMENT: 
PutWfSpecRequest is invalid: ThreadSpec entrypoint invalid: Node 1-double-method-TASK invalid: 
Input variable 0 needs to be DOUBLE but cannot be!
```

**Example 3: Runtime INT → DOUBLE conversion error**
```java
public static Workflow getDoubleRuntimeErrorWorkflow() {
    return new WorkflowImpl(
        "double-runtime-error-example",
        wf -> {
            WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
            var result = wf.execute("int-method", intVar); // this returns an INT
            wf.execute("double-method", result);           // ERROR: expects DOUBLE but gets INT
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
- **INT → DOUBLE**: Integers automatically widen to doubles

### 2. Manual Conversions (Explicit Casting)
- All other primitive conversions require explicit `.cast()` calls
- Provides safety for potentially unsafe conversions

## Conversion Rules

**Primitive Types Only**: The following rules apply only to primitive LittleHorse types (`INT`, `DOUBLE`, `STR`, `BOOL`, `BYTES`, `WF_RUN_ID`).

| From Type | To Type | Rule | Examples |
|-----------|---------|------|----------|
| `INT` | `STR` | ✅ Automatic | `42` → `"42"` |
| `DOUBLE` | `STR` | ✅ Automatic | `42.5` → `"42.5"` |
| `BOOL` | `STR` | ✅ Automatic | `true` → `"true"` |
| `WF_RUN_ID` | `STR` | ✅ Automatic | UUID → string |
| `BYTES` | `STR` | ✅ Automatic | Base64 encoding |
| `INT` | `DOUBLE` | ✅ Automatic | `42` → `42.0` |
| `DOUBLE` | `INT` | 🔧 Manual Cast | `42.7` → `42` (loses precision) |
| `STR` | `INT` | 🔧 Manual Cast | `"42"` → `42` (can fail) |
| `STR` | `DOUBLE` | 🔧 Manual Cast | `"42.7"` → `42.7` (can fail) |
| `STR` | `BOOL` | 🔧 Manual Cast | `"true"` → `true` (can fail) |
| `STR` | `BYTES` | 🔧 Manual Cast | Base64 decode (can fail) |
| `STR` | `WF_RUN_ID` | 🔧 Manual Cast | String → UUID (can fail) |
| `INT` | `BOOL` | ❌ Not Allowed | No clear conversion rule |
| `DOUBLE` | `BOOL` | ❌ Not Allowed | No clear conversion rule |

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
        
        // Important: Casting creates new values, doesn't modify originals
        wf.execute("double-method", doubleOutput); // Original value still 3.15
    });
}

// Conversions that should fail (no clear conversion rule)
// wf.execute("boolean-method", intVar.cast(VariableType.BOOL)); // ❌ Not allowed
```

## Implementation

### SDK Changes (Backwards Compatible)
```java
// Add cast() methods to existing interfaces
public interface WfRunVariable {
    // Existing methods unchanged...
    WfRunVariable cast(VariableType targetType);  // NEW - Returns new variable, doesn't modify original
}

public interface NodeOutput {
    // Existing methods unchanged...
    NodeOutput cast(VariableType targetType);     // NEW - Returns new output, doesn't modify original
}
```

**Important**: Casting operations are **immutable** - they create new values without modifying the original variable or output. This ensures data integrity and prevents unexpected side effects.

### Server-Side Changes
- **Automatic conversions**: Server performs safe conversions during task execution
- **Enhanced validation**: Better error messages for type mismatches
- **Backwards compatible**: All existing workflows continue to work unchanged

### Error Messages (Improved)
```java
// Before: "Task input variable with name arg1 at position 1 expects type STR but is type INT"
// An error should't happen here because of automatic casting to STR

// For manual casts:
// "Task 'process-age' parameter 'age' expects INT but received STR 'abc'. Use .cast(INT) or fix the input."
```

## Benefits

1. **Immediate Relief**: Existing code with INT→STR and INT→DOUBLE issues works automatically
2. **Type Safety**: Potentially unsafe conversions require explicit intent
3. **Backwards Compatible**: Zero breaking changes, existing workflows continue working
4. **Better UX**: Clear error messages guide users to solutions
5. **Performance**: Automatic conversions are fast, explicit casts only when needed

## Future Considerations

- **Complex Type Casting**: A separate proposal will address `JSON_OBJ` and `JSON_ARR` conversions once struct definitions are mature



