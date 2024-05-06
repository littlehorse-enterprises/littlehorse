# LH Dashboard

## Development

Create a copy of `.env-sample` as `.env-local` and modify it accordingly to your littlehorse-server configuration.

Then simply run

```shell
npm install
npm run dev
```

The application will start with watch mode on [http://localhost:3000](http://localhost:3000)

### LH Server without authentication

If you don't have a lh-server running, you can use this command:

```shell
docker run --rm -d -p 2023:2023 ghcr.io/littlehorse-enterprises/littlehorse/lh-standalone:master
```

Asuming your lh-server is running on `localhost:2023` create a `.env.local` file with

```env
LHC_API_HOST=localhost
LHC_API_PORT=2023
```

### LH Server with authentication

TODO
