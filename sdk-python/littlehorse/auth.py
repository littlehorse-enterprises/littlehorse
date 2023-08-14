from datetime import datetime
import logging
from typing import Any, Optional
from authlib.integrations.requests_client import OAuth2Session
import grpc
import requests


class Issuer:
    def __init__(self, data: dict[str, str]) -> None:
        self.data = data

    def token_endpoint(self) -> str:
        return self.data["token_endpoint"]

    def __str__(self) -> str:
        return self.data["issuer"]


class AccessToken:
    def __init__(self, data: dict[str, str]) -> None:
        self.data = data

    def token(self) -> str:
        return self.data["access_token"]

    def __str__(self) -> str:
        return self.token()

    def is_expired(self) -> bool:
        return self.expiration() < datetime.now()

    def expiration(self) -> datetime:
        return datetime.fromtimestamp(float(self.data["expires_at"]))


class OAuthException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


# https://grpc.io/docs/guides/auth/#python
# https://docs.authlib.org/en/latest/client/oauth2.html#oauth2session-for-client-credentials
class GrpcAuth(grpc.AuthMetadataPlugin):
    _log = logging.getLogger("GrpcAuth")

    def __init__(
        self,
        client_id: Optional[str],
        client_secret: Optional[str],
        authorization_server: Optional[str],
    ) -> None:
        self.client_id = client_id
        self.client_secret = client_secret
        self.authorization_server = authorization_server

        self._token: Optional[AccessToken] = None
        self._issuer: Optional[Issuer] = None

    def __call__(self, context: Any, callback: Any) -> None:
        access_token = self.access_token()
        callback((("authorization", access_token.token()),), None)

    def issuer(self) -> Issuer:
        if self.authorization_server is None:
            raise OAuthException("LHC_OAUTH_AUTHORIZATION_SERVER required")

        if self._issuer is None:
            well_known_response = requests.get(
                f"{self.authorization_server.rstrip('/')}/.well-known/openid-configuration"
            )
            self._issuer = Issuer(well_known_response.json())

        return self._issuer

    def access_token(self) -> AccessToken:
        if self._token is None or self._token.is_expired():
            self._log.debug("Obtaining a new access token")
            issuer = self.issuer()

            client = OAuth2Session(
                client_id=self.client_id,
                client_secret=self.client_secret,
                scope="openid",
            )

            token_data = client.fetch_token(
                url=issuer.token_endpoint(),
                grant_type="client_credentials",
            )

            self._token = AccessToken(token_data)
            self._log.debug("New token expires at: %s", self._token.expiration())

        return self._token


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    client_id = "worker"
    client_secret = "40317ab43bd34a9e93499c7ea03ad398"

    grpc_auth = GrpcAuth(client_id, client_secret, "http://localhost:8888/realms/lh")

    issuer = grpc_auth.issuer()
    print("Issuer:", issuer)

    access_token = grpc_auth.access_token()
    print("Access token:", access_token)
    print("Expired:", access_token.is_expired())
