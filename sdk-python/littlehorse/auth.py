from datetime import datetime
from typing import Any, Optional
from authlib.integrations.requests_client import OAuth2Session
import grpc
from littlehorse.exceptions import OAuthException


class AccessToken:
    def __init__(self, data: dict[str, str]) -> None:
        self.data = data

    @property
    def token(self) -> str:
        return self.data["access_token"]

    def __str__(self) -> str:
        return self.token

    def is_expired(self) -> bool:
        return self.expiration < datetime.now()

    @property
    def expiration(self) -> datetime:
        return datetime.fromtimestamp(float(self.data["expires_at"]))


# https://grpc.io/docs/guides/auth/#python
# https://docs.authlib.org/en/latest/client/oauth2.html#oauth2session-for-client-credentials
class OAuthCredentialsProvider(grpc.AuthMetadataPlugin):
    def __init__(
        self,
        client_id: Optional[str],
        client_secret: Optional[str],
        token_endpoint_url: Optional[str],
    ) -> None:
        self.client_id = client_id
        self.client_secret = client_secret
        self.token_endpoint_url = token_endpoint_url

        self._token: Optional[AccessToken] = None

    def __call__(self, context: Any, callback: Any) -> None:
        access_token = self.access_token()
        callback((("authorization", access_token.token),), None)

    def access_token(self) -> AccessToken:
        if self._token is None or self._token.is_expired():
            if self.token_endpoint_url is None:
                raise OAuthException("LHC_OAUTH_ACCESS_TOKEN_URL required")

            if self.client_id is None:
                raise OAuthException("LHC_OAUTH_CLIENT_ID required")

            if self.client_secret is None:
                raise OAuthException("LHC_OAUTH_CLIENT_SECRET required")

            client = OAuth2Session(
                client_id=self.client_id, client_secret=self.client_secret
            )

            token_data = client.fetch_token(
                url=self.token_endpoint_url,
                grant_type="client_credentials",
            )

            self._token = AccessToken(token_data)

        return self._token
