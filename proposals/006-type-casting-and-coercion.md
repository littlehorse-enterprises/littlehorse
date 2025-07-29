# Type Casting and Coercion in LittleHorse

- [Type Casting and Coercion in LittleHorse](#type-casting-and-coercion-in-littlehorse)
  - [Motivation](#motivation)
    - [Current Problem](#current-problem)
    - [Why We Need Type Casting](#why-we-need-type-casting)
    - [Goals](#goals)
  - [Proposed Solution](#proposed-solution)
    - [Implicit Type Coercion](#implicit-type-coercion)
    - [Explicit Type Casting](#explicit-type-casting)
    - [Type Safety Guarantees](#type-safety-guarantees)
  - [Implementation Details](#implementation-details)
    - [Coercion Rules Matrix](#coercion-rules-matrix)
    - [SDK Changes](#sdk-changes)
    - [Server-Side Validation](#server-side-validation)
    - [Error Handling](#error-handling)
  - [Migration Strategy](#migration-strategy)
  - [Future Considerations](#future-considerations)

## Motivation

### Current Problem

LittleHorse currently has strict typing without automatic type coercion, leading to runtime errors that are confusing to users. For example:

```java
// This fails because calculate-age returns INT but greet expects STR for age parameter
var age = wf.execute("calculate-age", birthYear);  // Returns INT
wf.execute("greet", name, age);                    // Expects (STR, STR) but gets (STR, INT)
```

The error messages are cryptic:
- `Input variable 0 needs to be INT but cannot be!`
- `Task input variable with name arg1 at position 1 expects type STR but is type INT`

### Why We Need Type Casting

1. **Usability**: Common type conversions (like `INT` to `STR`) should be automatic and safe
2. **Consistency**: Match expectations from programming languages where basic type coercion is common
3. **Backwards Compatibility**: Enable gradual migration without breaking existing workflows
4. **Developer Experience**: Reduce friction and make workflows more intuitive to write

### Goals

1. **Safe by Default**: Only allow coercions that don't lose information or cause runtime errors
2. **Explicit When Dangerous**: Require explicit casting for potentially lossy operations
3. **Predictable**: Clear, documented rules for when coercion happens
4. **Performance**: Minimal overhead for type checking and conversion
5. **Debuggable**: Clear error messages when coercion fails

## Proposed Solution

### Implicit Type Coercion (Safe Automatic Conversions)

We propose implementing automatic type conversions for safe, lossless operations:

#### Safe Automatic Conversions (No Data Loss)
- `INT` → `DOUBLE` (safe: `42` becomes `42.0`)
- `INT` → `STR` (safe: `42` becomes `"42"`)
- `DOUBLE` → `STR` (safe: `42.5` becomes `"42.5"`)
- `BOOL` → `STR` (safe: `true` becomes `"true"`)
- `WF_RUN_ID` → `STR` (safe: UUID becomes string)
- `BYTES` → `STR` (safe: converts to Base64 string)

#### Example:
```java
// This should work automatically with server-side conversion
// No SDK changes required - existing code benefits immediately
WfRunVariable birthYear = wf.addVariable("year-of-birth", VariableType.INT);
var age = wf.execute("calculate-age", birthYear);  // Returns INT
wf.execute("greet", name, age);                    // Server AUTO-converts INT age to STR
```

### Immediate Benefits for Existing Code

Your current `BasicExample` would work immediately after server upgrade:

```java
// This exact code (unchanged) would work after server implements widening conversions
public static Workflow getWorkflow() {
    return new WorkflowImpl(
        "example-basic",
        wf -> {
            WfRunVariable birthYear = wf.addVariable("year-of-birth", VariableType.INT).searchable();
            WfRunVariable name = wf.addVariable("name", VariableType.STR).searchable();
            var age = wf.execute("calculate-age", birthYear);  // Returns INT
            wf.execute("greet", name, age);                    // Server converts INT → STR automatically
        }
    );
}

// Task remains unchanged - server handles the conversion
@LHTaskMethod("greet")
public String greeting(String name, String age) {  // Still expects String age
    var message = "hello there, " + name + ". You are " + age + " years old.";
    return message;
}
```

### Explicit Type Casting (Potentially Unsafe Conversions)

For conversions that might lose data or fail, require explicit casting:

#### Explicit Casts Required (Potential Data Loss or Failure)
- `DOUBLE` → `INT` (loses decimal part: `42.7` becomes `42`)
- `STR` → `INT` (can fail: `"abc"` can't become a number)
- `STR` → `DOUBLE` (can fail: `"abc"` can't become a number)
- `STR` → `BOOL` (can fail: `"maybe"` can't become true/false)
- `STR` → `BYTES` (can fail: invalid Base64 strings)
- `STR` → `WF_RUN_ID` (can fail: invalid UUID strings)

#### SDK API (Optional Future Enhancement):
```java
// These APIs can be added to future SDK versions without breaking existing code
// Existing SDKs will continue to work - the server handles conversions automatically

// Java SDK - optional convenience methods for explicit casting
var ageDouble = wf.execute("get-age-precise");    // Returns DOUBLE
var ageInt = wf.cast(ageDouble, VariableType.INT); // Optional explicit cast (loses decimal part)
wf.execute("process-age", ageInt);

// Alternative fluent API (optional)
var ageInt = wf.execute("get-age-precise").castTo(VariableType.INT);

// Current code continues to work unchanged:
var ageDouble = wf.execute("get-age");           // Returns INT  
wf.execute("process-precise-age", ageDouble);    // Server automatically converts INT → DOUBLE
```

### Type Safety Guarantees

1. **Server-side Validation**: Server validates all type conversions during workflow execution
2. **Runtime Safety**: Failed conversions result in clear error messages, not crashes  
3. **Backwards Compatibility**: Existing workflows that work today continue to work
4. **Progressive Enhancement**: New workflows benefit from automatic casting without code changes

## Implementation Details

### Coercion Rules Matrix (Simple Rules)

Following the principle of safe automatic conversions:

**Type Safety Hierarchy (safe → less safe):**
```
BOOL → INT → DOUBLE → STR
     ↘ WF_RUN_ID ↗
BYTES ────────────→ STR
```

| From Type | To Type | Rule | Why | Examples |
|-----------|---------|------|-----|----------|
| `INT` | `DOUBLE` | ✅ Automatic | No data lost | `42` → `42.0` |
| `INT` | `STR` | ✅ Automatic | Always works | `42` → `"42"` |
| `BOOL` | `INT` | ✅ Automatic | Standard conversion | `true` → `1`, `false` → `0` |
| `BOOL` | `DOUBLE` | ✅ Automatic | No data lost | `true` → `1.0` |
| `BOOL` | `STR` | ✅ Automatic | Always works | `true` → `"true"` |
| `WF_RUN_ID` | `STR` | ✅ Automatic | Always works | UUID → string |
| `BYTES` | `STR` | ✅ Automatic | Always works | Base64 encoding |
| `DOUBLE` | `INT` | 🔧 Explicit Cast | Loses decimal part | `42.7` → `42` |
| `STR` | `INT` | 🔧 Explicit Cast | Can fail with bad input | `"42"` → `42`, `"abc"` → ERROR |
| `STR` | `DOUBLE` | 🔧 Explicit Cast | Can fail with bad input | `"42.7"` → `42.7`, `"abc"` → ERROR |
| `STR` | `BOOL` | 🔧 Explicit Cast | Can fail with bad input | `"true"` → `true`, `"maybe"` → ERROR |
| `STR` | `BYTES` | 🔧 Explicit Cast | Can fail with bad input | Valid Base64 → bytes, invalid → ERROR |
| `STR` | `WF_RUN_ID` | 🔧 Explicit Cast | Can fail with bad input | Valid UUID → WF_RUN_ID, invalid → ERROR |
| `INT` | `BOOL` | ❌ Not Allowed | No clear rule | - |
| `DOUBLE` | `BOOL` | ❌ Not Allowed | No clear rule | - |

Legend:
- ✅ Automatic: Safe conversion, happens automatically
- 🔧 Explicit Cast: Potentially unsafe, requires explicit cast
- ❌ Not Allowed: No conversion available

### SDK Changes (Additive Only - No Breaking Changes)

#### Java SDK (New Methods Added, Existing Methods Unchanged)

```java
// EXISTING APIs remain unchanged - full backwards compatibility
public interface WfRunVariable {
    // All existing methods continue to work exactly as before
    // ... existing methods unchanged ...
    
    // NEW convenience methods added (backwards compatible)
    WfRunVariable castTo(VariableType targetType);
    WfRunVariable castTo(VariableType targetType, CastOptions options);
}

// EXISTING WorkflowBuilder unchanged
public interface WorkflowBuilder {
    // All existing methods continue to work exactly as before
    // ... existing methods unchanged ...
    
    // NEW casting utilities added (backwards compatible)
    WfRunVariable cast(WfRunVariable source, VariableType targetType);
    WfRunVariable cast(WfRunVariable source, VariableType targetType, CastOptions options);
}

// NEW class - doesn't affect existing code
public class CastOptions {
    private boolean failOnError = true;
    private WfRunVariable defaultValue = null;
    
    public static CastOptions withDefault(WfRunVariable defaultValue) {
        return new CastOptions().setFailOnError(false).setDefaultValue(defaultValue);
    }
}
```

#### Enhanced Error Messages (Server-Side)

```java
// Server responses improve without requiring SDK changes

// Before: "Input variable 0 needs to be INT but cannot be!"
// After: "Task 'calculate-age' parameter 'birthYear' expects INT but received STR '1990'. Consider using string parsing or updating the task to accept STR."

// Before: "Task input variable with name arg1 at position 1 expects type STR but is type INT"  
// After: "Task 'greet' parameter 'age' expects STR but received INT '25'. This conversion is performed automatically by the server."
```

### Server-Side Changes (Backwards Compatible)

#### Enhanced VariableValueModel (Internal Changes Only)

```java
public class VariableValueModel {
    // EXISTING methods unchanged - full backwards compatibility
    public VariableValueModel coerceToType(VariableType otherType) throws LHVarSubError {
        // Existing implementation continues to work
        return coerceToType(otherType, false); // Default to automatic widening
    }
    
    // NEW method added - doesn't break existing calls
    public VariableValueModel coerceToType(VariableType otherType, boolean isExplicitCast) throws LHVarSubError {
        if (!isCoercionAllowed(getType(), otherType, isExplicitCast)) {
            throw new LHVarSubError(null, formatCoercionError(getType(), otherType, isExplicitCast));
        }
        
        return performCoercion(otherType);
    }
    
    // NEW internal helper methods - don't affect existing APIs
    private boolean isCoercionAllowed(VariableType from, VariableType to, boolean isExplicitCast) {
        if (from == to) return true;
        
        // Safe automatic conversions
        if (!isExplicitCast) {
            return SAFE_AUTOMATIC_CONVERSIONS.contains(new TypePair(from, to));
        }
        
        // Explicit casts (potentially unsafe)
        return EXPLICIT_CAST_CONVERSIONS.contains(new TypePair(from, to));
    }
    
    // NEW configuration constants - internal implementation
    private static final Set<TypePair> SAFE_AUTOMATIC_CONVERSIONS = Set.of(
        new TypePair(VariableType.INT, VariableType.DOUBLE),
        new TypePair(VariableType.INT, VariableType.STR),
        new TypePair(VariableType.BOOL, VariableType.INT),
        new TypePair(VariableType.BOOL, VariableType.DOUBLE),
        new TypePair(VariableType.BOOL, VariableType.STR),
        new TypePair(VariableType.DOUBLE, VariableType.STR),
        new TypePair(VariableType.WF_RUN_ID, VariableType.STR),
        new TypePair(VariableType.BYTES, VariableType.STR)
    );
    
    private static final Set<TypePair> EXPLICIT_CAST_CONVERSIONS = Set.of(
        new TypePair(VariableType.DOUBLE, VariableType.INT),
        new TypePair(VariableType.STR, VariableType.INT),
        new TypePair(VariableType.STR, VariableType.DOUBLE),
        new TypePair(VariableType.STR, VariableType.BOOL),
        new TypePair(VariableType.STR, VariableType.BYTES),
        new TypePair(VariableType.STR, VariableType.WF_RUN_ID)
    );
}
```

#### Type Checking During WfSpec Registration (Enhanced, Backwards Compatible)

```java
// Enhanced server-side validation - existing protobuf messages unchanged
// All existing client workflows continue to work

public class TaskNodeModel {
    // EXISTING validate() method enhanced internally, signature unchanged
    public void validate() throws LHValidationError {
        TaskDefModel taskDef = getTaskDef();
        
        for (int i = 0; i < variables.size(); i++) {
            VariableAssignmentModel assignment = variables.get(i);
            TypeDefinitionModel expectedType = taskDef.getInputVars().get(i).getTypeDef();
            TypeDefinitionModel actualType = assignment.getType();
            
    // NEW: Check if safe automatic conversion is available
    if (!isAssignmentValid(actualType, expectedType)) {
        if (isSafeAutomaticConversionAvailable(actualType.getType(), expectedType.getType())) {
            // Allow it - server will handle conversion automatically
            continue;
        }
        
        String suggestion = getSuggestion(actualType, expectedType);
        throw new LHValidationError(
            String.format("Task '%s' parameter %d expects %s but received %s. %s",
                taskDef.getName(), i, expectedType.getType(), actualType.getType(), suggestion)
        );
    }
        }
    }
    
    // NEW helper methods - internal implementation
    private String getSuggestion(TypeDefinitionModel from, TypeDefinitionModel to) {
        if (isSafeAutomaticConversionAvailable(from.getType(), to.getType())) {
            return "This safe conversion will be performed automatically by the server.";
        } else if (isExplicitCastAvailable(from.getType(), to.getType())) {
            return "This conversion requires explicit handling in your task implementation (potential data loss or failure).";
        } else {
            return "No conversion available between these types.";
        }
    }
}
```

### Error Handling (Server-Side)

#### Graceful Conversion Failures

```java
// Server-side handling - existing SDK error handling continues to work

// Current behavior: Type mismatch causes workflow failure
// New behavior: Server attempts widening conversion, fails gracefully with better errors

// For narrowing conversions that can't be done automatically:
// Server provides clear error messages explaining the limitation
// Users can update their task implementations to handle the conversion
```

## Implementation Strategy

### Backwards Compatible Implementation (No Migration Required)

All changes will be implemented with **strict backwards compatibility**. Existing clients continue to work unchanged, and no migration is required.

### Server Changes (Backwards Compatible)
1. **Enhanced type conversion in VariableValueModel**: Server automatically performs widening conversions during workflow execution
2. **Improved error messages**: Better error responses, but existing client error handling continues to work
3. **Enhanced validation**: Server provides better validation, but existing protobuf messages remain unchanged

### SDK Changes (Additive Only)
1. **New convenience methods**: SDKs can add new casting methods without breaking existing APIs
2. **Enhanced builders**: New overloads and fluent APIs, existing methods remain unchanged
3. **Optional features**: All new features are opt-in, existing code paths unaffected

### Compatibility Guarantee
- **Protocol Buffer compatibility**: No breaking changes to existing messages
- **Existing workflows work unchanged**: All currently working workflows continue to work
- **Existing SDK versions supported**: Old SDK versions remain fully functional with new server
- **No client updates required**: Users can upgrade server and benefit immediately

### Phase 1: Server-Side Safe Conversions (LH 1.1.0)
1. **Server-only implementation**: All safe automatic conversions handled during workflow execution
2. **Zero breaking changes**: Existing SDKs and workflows work unchanged
3. **Immediate benefit**: Users get automatic type conversion without any code changes
4. **Backwards compatible**: All existing protobuf messages and APIs remain the same

### Phase 2: Enhanced Error Messages (LH 1.2.0) 
1. **Improved server error responses**: Better error messages in existing error response format
2. **Backwards compatible**: Existing client error handling continues to work unchanged
3. **Additive SDK features**: SDKs can add convenience casting APIs without breaking existing methods

### Phase 3: Additive SDK Enhancements (LH 1.3.0+)
1. **New convenience APIs**: SDKs add `.castTo()` methods alongside existing methods
2. **Enhanced validation**: New SDK versions can add compile-time validation as opt-in feature
3. **Full compatibility**: All existing SDK versions continue to work with enhanced server

### No Migration Required
- **Existing deployments**: Continue working immediately after server upgrade
- **Mixed versions**: Old and new SDK versions can work with the same server
- **Gradual adoption**: Teams can upgrade SDKs at their own pace or never upgrade
- **Zero breaking changes**: No API changes, no protobuf changes, no behavior changes for existing code

### Server Configuration Options

```properties
# littlehorse.config (server-side configuration)
lh.server.types.safe-conversions.enabled=true
lh.server.types.safe-conversions.bool-to-int=true
lh.server.types.safe-conversions.int-to-double=true
lh.server.types.safe-conversions.int-to-string=true
lh.server.types.validation.strict-mode=false
```

### Backwards Compatibility Guarantee

1. **No breaking changes**: All existing workflows, SDKs, and client code continue to work unchanged
2. **No migration required**: Users can upgrade server and benefit immediately without any code changes
3. **Mixed environments**: Old and new SDK versions can coexist with the enhanced server
4. **Additive only**: All changes are additions to existing functionality, not modifications
5. **Protocol compatibility**: No changes to protobuf messages or network protocol

## Future Considerations

### Struct Support
When `Struct` and `StructDef` are implemented:
- Field-level type coercion within structs
- Structural type compatibility (duck typing)
- Schema evolution with type migration

### Advanced Casting
- Custom cast functions for user-defined types
- Locale-aware string conversions
- Precision control for numeric conversions

### Performance Optimizations
- Compile-time type inference
- Cached coercion functions
- Zero-copy conversions where possible

### Integration with Strong Typing Initiative
This proposal complements proposal 002 (Moving Towards Strong Typing) by:
- Providing a smooth migration path from loose to strict typing using Java semantics
- Maintaining user-friendly APIs while enforcing type safety with familiar casting rules
- Enabling gradual adoption of stronger type constraints following established Java patterns

### Java-Style Type Hierarchy Examples

#### Safe Automatic Conversions (No Cast Required)
```java
// Simple rule: if it's always safe and never fails, it's automatic
WfRunVariable isActive = wf.addVariable("active", VariableType.BOOL);
WfRunVariable count = wf.addVariable("count", VariableType.INT);

// These work automatically (safe conversions)
wf.execute("process-int", isActive);      // BOOL → INT (true=1, false=0)
wf.execute("process-double", count);      // INT → DOUBLE (42 → 42.0)
wf.execute("process-string", count);      // INT → STR (42 → "42")
wf.execute("log-message", isActive);      // BOOL → STR (true → "true")
```

#### Explicit Casts Required (Potentially Unsafe)
```java
// Rule: if data might be lost or conversion might fail, explicit cast required
WfRunVariable price = wf.addVariable("price", VariableType.DOUBLE);
WfRunVariable ageStr = wf.addVariable("age-str", VariableType.STR);

// These require explicit casts (potentially unsafe)
var priceInt = wf.cast(price, VariableType.INT);           // DOUBLE → INT (loses decimal: 42.7 → 42)
var age = wf.cast(ageStr, VariableType.INT);               // STR → INT (can fail: "abc" → ERROR)
var isValid = wf.cast(ageStr, VariableType.BOOL);          // STR → BOOL (can fail: "maybe" → ERROR)
```

---

**Related Issues:**
- Implements type casting mentioned in Proposal 002 Future Work
- Addresses user experience issues with current strict typing
- Provides foundation for advanced type system features

**Implementation Priority:** High - addresses immediate user pain points while supporting long-term typing goals
