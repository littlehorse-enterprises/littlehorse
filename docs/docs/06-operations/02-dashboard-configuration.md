# Dashboard Configurations

:::info
These configurations apply only to the `ghcr.io/littlehorse-enterprises/littlehorse/lh-dashboard` docker image, and not when running the LH Dashboard compiled from source. We have an open ticket in our OSS repo to homologate the configurations and also improve the startup time of the `lh-dashboard` docker image.
:::

## Dashboard

### `LHD_API_HOST`

The bootstrap host for the LH Server.

- **Type:** string
- **Default:** localhost
- **Importance:** high

---

### `LHD_API_PORT`

The bootstrap port for the LH Server.

- **Type:** int
- **Default:** 2023
- **Importance:** high

---

### `LHD_OAUTH_ENABLED`

Enable OAuth2/OpenID.

- **Type:** boolean
- **Default:** false
- **Importance:** high

---

### `LHD_OAUTH_CLIENT_ID`

OAuth2 Client Id. Used by the Dashboard to identify itself at an Authorization Server. Mandatory if `LHD_OAUTH_ENABLED`
is `true`.

- **Type:** string
- **Default:** null
- **Importance:** high

---

### `LHD_OAUTH_CLIENT_SECRET`

OAuth2 Client Secret. Used by the Dashboard to identify itself at an Authorization Server. Mandatory
if `LHD_OAUTH_ENABLED` is `true`.

- **Type:** string
- **Default:** null
- **Importance:** high

---

### `LHD_OAUTH_SERVER_URL`

Authorization Server URL. Mandatory if `LHD_OAUTH_ENABLED` is `true`.

- **Type:** url
- **Default:** null
- **Importance:** high

---

### `LHD_OAUTH_CALLBACK_URL`

Canonical URL of the Dashboard site. Used by the Authorization Server to return the control to the LH Dashboard.
Mandatory if `LHD_OAUTH_ENABLED` is `true`. More
info [here](https://next-auth.js.org/configuration/options#nextauth_url).

- **Type:** url
- **Default:** null
- **Importance:** high

---

### `LHD_OAUTH_CALLBACK_URL_INTERNAL`

Internal URL of the Dashboard server. Used by the Dashboard Server to query itself.
Should only be set when the `LHD_OAUTH_CALLBACK_URL` cannot be reached by the dashboard server. More
info [here](https://next-auth.js.org/configuration/options#nextauth_url_internal).

- **Type:** url
- **Default:** null
- **Importance:** low

---

### `LHD_OAUTH_ENCRYPT_SECRET`

Used by the Dashboard to encrypt the internal JWT.
Mandatory if `LHD_OAUTH_ENABLED` is `true`. More
info [here](https://next-auth.js.org/configuration/options#nextauth_secret).

- **Type:** string
- **Default:** null
- **Importance:** high
