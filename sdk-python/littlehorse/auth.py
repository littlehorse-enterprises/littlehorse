import logging
from typing import Any, Optional
import grpc


# https://grpc.io/docs/guides/auth/#python
class GrpcAuth(grpc.AuthMetadataPlugin):
    def __init__(
        self,
        client_id: Optional[str],
        client_secret: Optional[str],
        authorization_server: Optional[str],
    ) -> None:
        self.client_id = client_id
        self.client_secret = client_secret
        self.authorization_server = authorization_server

    def __call__(self, context: Any, callback: Any) -> None:
        callback((("authorization", self.access_token()),), None)

    def access_token(self) -> str:
        logging.debug("Obtaining OAuth Access Token")
        return "access-token"
