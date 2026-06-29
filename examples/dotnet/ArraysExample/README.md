# Arrays Example (.NET)

This example mirrors the Java `examples/java/arrays` sample using the .NET SDK.

## What it does

- Declares a native LittleHorse ARRAY variable in the workflow (`Long` elements)
- Executes `produce-array` to return a native LH Array
- Assigns that output to the workflow array variable
- Executes `consume-array` with the array variable as input

## Source files

- `Program.cs`: workflow and worker bootstrapping
- `ArrayWorker.cs`: task implementations with `[LHType(isLHArray = true)]`

## Run

```bash
cd examples/dotnet/ArraysExample
dotnet build
dotnet run
```

In another terminal, run the workflow:

```bash
lhctl run example-arrays
```

## Notes

- `produce-array` is annotated with `[LHType(masked: false, isLHArray: true)]` so return type is a native LH Array.
- `consume-array` parameter is annotated with `[LHType(masked: false, isLHArray: true)]` so input is read as native LH Array.
