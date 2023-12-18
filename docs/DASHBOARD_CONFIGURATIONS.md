# Dashboard Configurations

<!-- TOC -->
* [Dashboard Configurations](#dashboard-configurations)
  * [Dashboard](#dashboard)
    * [`LHD_API_HOST`](#lhd_api_host)
    * [`LHD_API_PORT`](#lhd_api_port)
    * [`LHD_OAUTH_ENABLED`](#lhd_oauth_enabled)
    * [`LHD_OAUTH_CLIENT_ID`](#lhd_oauth_client_id)
    * [`LHD_OAUTH_CLIENT_SECRET`](#lhd_oauth_client_secret)
    * [`LHD_OAUTH_SERVER_URL`](#lhd_oauth_server_url)
    * [`LHD_OAUTH_CALLBACK_URL`](#lhd_oauth_callback_url)
    * [`LHD_OAUTH_ENCRYPT_SECRET`](#lhd_oauth_encrypt_secret)
<!-- TOC -->

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

### `LHD_OAUTH_ENCRYPT_SECRET`

Used by the Dashboard to encrypt the internal JWT.
Mandatory if `LHD_OAUTH_ENABLED` is `true`. . More
info [here](https://next-auth.js.org/configuration/options#nextauth_secret).

- **Type:** string
- **Default:** null
- **Importance:** high
