# Arrays Example (.NET)

This example mirrors the Java `examples/java/arrays` sample using the .NET SDK.

## What it does

- Declares a native LittleHorse ARRAY input variable in the workflow (`Long` elements)
- Executes `produce-array` to get another native LH Array
- Merges the two Arrays using `EXTEND` (native Array concatenation)
- Executes `consume-array` with the merged Array as input

## Source files

- `Program.cs`: workflow and worker bootstrapping
- `ArrayWorker.cs`: task implementations with `[LHType(isLHArray = true)]`

## Run

```bash
cd examples/dotnet/ArraysExample
dotnet build
dotnet run
```

In another terminal, run the workflow. `my-array` is a typed `Array<INT>` input variable;
provide it as a JSON array (elements are coerced to the declared element type). The workflow
then appends `[1, 2, 3]` (from `produce-array`) to it:

```bash
# Input [10, 20, 30] is merged with [1, 2, 3] => [10, 20, 30, 1, 2, 3]
lhctl run example-arrays my-array '[10, 20, 30]'
```

## Notes

- `my-array` has no default, so the JSON array input is required.
- `EXTEND` on a native Array concatenates when the right-hand side is an Array of the same type.
- `consume-array` parameter is annotated with `[LHType(masked: false, isLHArray: true)]` so input is read as native LH Array.
