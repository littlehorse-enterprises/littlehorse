# Website

This website is built using [Docusaurus 3](https://docusaurus.io/), a modern static website generator.

They are hosted on `www.littlehorse.dev`.

## Best Practices

### Hyperlinks

First, it's recommended to use _relative_ links (eg. `../08-api.md`) rather than url paths (eg. `/api`). This is because of Docusaurus versioning.

Second, the way the auto-generatd protobuf docs is that the `#` hyperlink is the name of the `rpc`, `message`, or `enum`. Note that VSCode auto-suggests `#rpc-foo-foo` for `rpc foo`, whereas what is correct is `#foo`.

You can see this by looking at the `08-api.md`.

Lastly, when using `npm start` (local devleopment), none of the hyperlinks will work with the `#` tags. To test that:

```
npm run build
npm run serve
```

The `npm run build` shouldn't complain about broken hyperlinks, and when you open the site it should work.

## Local Development

```
npm install
npm start
```
