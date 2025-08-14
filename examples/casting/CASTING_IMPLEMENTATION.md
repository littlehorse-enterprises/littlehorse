# LittleHorse Manual Type Casting Implementation

## 🎯 Overview

This implementation adds comprehensive manual type casting support to LittleHorse, distinguishing between **automatic** and **manual** casting operations to provide better type safety and explicit control over type conversions.

## 🔧 Architecture

### Core Components

1. **`TypeCastingUtils`** - Centralized casting logic utility class
   - Located: `io.littlehorse.common.util.TypeCastingUtils`
   - Contains all casting validation and runtime execution logic

2. **`VariableAssignmentModel`** - Refactored to use `TypeCastingUtils`
   - Validates task parameter assignments
   - Supports explicit `.cast()` calls

3. **`VariableMutationModel`** - Enhanced with casting validation
   - Validates variable assignments via `WfRunVariable.assign()`
   - Enforces manual casting requirements

4. **`TypeDefinitionModel`** - Streamlined to delegate to `TypeCastingUtils`
   - Maintains backward compatibility
   - Runtime casting execution

## 📋 Casting Rules

### Automatic Casting (No `.cast()` required)
```java
// Any primitive type → STR
intVar → stringMethod        ✅ Automatic
doubleVar → stringMethod     ✅ Automatic  
boolVar → stringMethod       ✅ Automatic

// INT → DOUBLE
intVar → doubleMethod        ✅ Automatic
```

### Manual Casting (Requires `.cast()` call)
```java
// STR → Other primitives
stringVar.cast(INT) → intMethod         ✅ Manual
stringVar.cast(DOUBLE) → doubleMethod   ✅ Manual
stringVar.cast(BOOL) → boolMethod       ✅ Manual
stringVar.cast(BYTES) → bytesMethod     ✅ Manual
stringVar.cast(WF_RUN_ID) → wfRunMethod ✅ Manual

// DOUBLE → INT
doubleVar.cast(INT) → intMethod         ✅ Manual
```

## 🚨 Validation Features

### Task Parameter Validation
```java
// ❌ FAILS - STR → INT requires manual cast
wf.execute("int-method", stringVar);

// ✅ WORKS - Explicit cast provided
wf.execute("int-method", stringVar.cast(VariableType.INT));
```

### Variable Assignment Validation
```java
WfRunVariable intVar = wf.addVariable("int-var", VariableType.INT);
var stringResult = wf.execute("string-method", someValue);

// ❌ FAILS - STR → INT requires manual cast
intVar.assign(stringResult);

// ✅ WORKS - Explicit cast provided
intVar.assign(stringResult.cast(VariableType.INT));
```

### Node Output Validation
For task results and expressions where source type is unknown:
- **Conservative approach**: Requires explicit casting for target types that commonly need manual conversion
- **Permissive for automatic cases**: Allows INT task results → DOUBLE methods

## 🧪 Test Workflows

### 1. `auto-casting-workflow`
Demonstrates all automatic casting scenarios:
- Primitive → STR conversions
- INT → DOUBLE conversions
- Works with literals, variables, and task results

### 2. `manual-casting-workflow`
Comprehensive manual casting examples:
- STR → INT/DOUBLE/BOOL conversions
- DOUBLE → INT conversions
- Complex chaining scenarios

### 3. `simple-casting-workflow`
Minimal example focusing on:
- Variable assignment validation
- Basic manual casting patterns

### 4. `casting-summary-workflow` 🎯
**Comprehensive demonstration** including:
- All automatic casting cases
- All manual casting cases
- Variable assignment validation
- Edge cases and complex scenarios
- Task result casting (NODE_OUTPUT)
- Chained casting operations

## 🔍 Edge Cases Handled

1. **Chained Task Results**
   ```java
   var step1 = wf.execute("string-method", doubleVar);    // DOUBLE → STR
   var step2 = wf.execute("double-method", step1.cast(VariableType.DOUBLE)); // STR → DOUBLE
   wf.execute("int-method", step2.cast(VariableType.INT)); // DOUBLE → INT
   ```

2. **Dynamic Assignment Validation**
   ```java
   var dynamicResult = wf.execute("string-method", 12345);
   intVar.assign(dynamicResult.cast(VariableType.INT)); // STR task result → INT
   ```

3. **Boolean Conversions**
   ```java
   var boolAsString = wf.execute("string-method", true);   // BOOL → STR
   boolVar.assign(boolAsString.cast(VariableType.BOOL));   // STR → BOOL
   ```

4. **Complex Nested Casting**
   ```java
   wf.execute("double-method", 
       wf.execute("string-method", intVar).cast(VariableType.DOUBLE) // INT → STR → DOUBLE
   );
   ```

## 🎮 Usage Examples

### Run the examples:
```bash
# Basic automatic casting
lhctl run auto-casting-workflow

# Manual casting with .cast() calls
lhctl run manual-casting-workflow

# Simple validation demo
lhctl run simple-casting-workflow

# Comprehensive feature demo
lhctl run casting-summary-workflow
```

### Inspect workflow specifications:
```bash
lhctl get wfSpec casting-summary-workflow
```

## 🏗️ Implementation Benefits

1. **Type Safety**: Prevents implicit conversions that could cause runtime errors
2. **Explicit Intent**: `.cast()` calls make type conversions obvious in code
3. **Centralized Logic**: All casting rules in one utility class
4. **Comprehensive Validation**: Covers both task parameters and variable assignments
5. **Backward Compatibility**: Existing automatic casting continues to work
6. **Better Error Messages**: Clear feedback when casting is required
7. **Runtime Safety**: Proper error handling for invalid cast operations

## 🚀 Key Features

- ✅ **Automatic casting** for safe conversions (primitive → STR, INT → DOUBLE)
- ✅ **Manual casting** with `.cast()` for potentially unsafe conversions
- ✅ **Validation at registration time** prevents invalid workflows
- ✅ **Runtime casting execution** with detailed error messages
- ✅ **Support for all assignment types** (task parameters and variable mutations)
- ✅ **Edge case handling** for complex scenarios
- ✅ **Centralized casting logic** for maintainability

This implementation provides a robust, type-safe casting system that gives developers explicit control over type conversions while maintaining backward compatibility with existing automatic casting behavior.
