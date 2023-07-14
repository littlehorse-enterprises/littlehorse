## E2E tests

1. You need a LH cluster already running
2. Configure your ~/.config/littlehorse.config file
3. Then you can run the e2e test with next commands:

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


### Usage:

```
usage: e2e-tests [OPTIONS] [TESTS]
-h,--help            shows this help message
-t,--threads <arg>   number of threads default 8
```
