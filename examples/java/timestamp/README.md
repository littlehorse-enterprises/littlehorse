# Example: Timestamp (Java)

This example demonstrates declaring and using a timestamp variable inside a LittleHorse workflow and the TaskWorker implementations that consume and produce different timestamp representations.

What it demonstrates

- How to declare a timestamp workflow variable (`publish-date`) with a default `Instant` value.
- Passing that timestamp into tasks and converting it to multiple Java timestamp types inside a task.
- Returning the current date/time from a task and logging combined information from several timestamp representations.

Tasks in this example

- `publish-book` — creates a `Book` object and populates multiple timestamp, that Littlehorse supports representations:
  - `java.util.Date`
  - `java.time.Instant`
  - `com.google.protobuf.Timestamp` (protobuf)
  - `java.sql.Timestamp`
  - `java.time.LocalDateTime`

- `get-current-date` — returns the current date/time (as `java.util.Date`).

- `print-book-details` — receives the `Book` and the current date, then logs the published book data and the current timestamp.

Files of interest

- `TimestampExample.java` — workflow definition, variables, and worker registration.
- `Worker.java` — implementations of the three tasks mentioned above.
- `Book.java` — a plain Java object that stores the several timestamp representations.

How to run locally

1. From the repository root, run the app:

```bash
./gradlew :example-timestamp:run
```

2. Trigger the workflow (example using `lhctl`):

```bash
lhctl run example-timestamp book-name "My Book" publish-date 1997-06-26T12:12:12Z
```

Notes

- This example is intended for demonstration and testing of the different timestamp representations supported by the SDK and server. If your code needs to perform calculations with timestamps, prefer working with `Instant` (or protobuf `Timestamp`) and convert to other types only for serialization or display.
- The `Book` class no longer contains a random helper method; its purpose is to hold the several timestamp fields.
