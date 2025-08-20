# Cleaning Up the `examples` Directory

## Cleanup Script

Run the following command from the root of the repository to delete all directories under `examples/` except for the official ones:, in case we have the examples dir somhow.

```sh
find examples -mindepth 1 -maxdepth 1 -type d ! -name 'go' ! -name 'java' ! -name 'python' ! -name 'dotnet' ! -name 'docker-compose' -exec rm -rf {} +
```

- This will keep only the `go`, `java`, `python`, `dotnet`, and `docker-compose` directories.
- All other directories in `examples/` will be deleted.

