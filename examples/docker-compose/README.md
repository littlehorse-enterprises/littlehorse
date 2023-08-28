# Docker Compose Sandbox

This example is intended to be used in development environments.

Run docker compose:

```
docker compose up -d
```

Check your `~/.config/littlehorse.config` file:

```
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

You can confirm that the Server is running via:

```
lhctl search wfSpec
```

Result:

```
{
  "results": []
}
```
