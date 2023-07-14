## E2E tests

1. You need a LH cluster already running
2. Then you can run the e2e test with next commands:

Run all e2e tests:

```
./gradlew e2e-test:run
```

Run specific e2e tests:

```
./gradlew e2e-test:run --args="AASequential ABIntInputVars"
```

Run e2e tests with multiple threads:

```
./gradlew e2e-test:run --args="-t 4"
```

Help:

```
./gradlew e2e-test:run --args="-h"
```

### Configurations

Default configuration:
```
LHC_API_HOST=localhost
LHC_API_PORT=2023
LHW_TASK_WORKER_VERSION=lh.integration-tests.local
```

Also, you can configure your `~/.config/littlehorse.config` file
if your server's configuration if different to the default values.


### Usage:

```
usage: e2e-tests [OPTIONS] [TESTS]
-h,--help            shows this help message
-t,--threads <arg>   number of threads default 8
```
