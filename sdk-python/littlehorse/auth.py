import asyncio
from datetime import datetime
from typing import Any, Optional, cast
import time
from authlib.integrations.requests_client import OAuth2Session
import grpc
from grpc.aio import ClientCallDetails

from littlehorse.exceptions import OAuthException

TENANT_ID_HEADER = "tenantid"
STATUS_DETAILS_HEADER = "grpc-status-details-bin"


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


def _call_details_with_tenant(
    tenant_id: Optional[str], client_call_details: Any
) -> ClientCallDetails:
    metadata: list[tuple[str, str | bytes]] = []
    if client_call_details.metadata is not None:
        metadata = [(entry.key, entry.value) for entry in client_call_details.metadata]
    if tenant_id:
        metadata.append(
            (
                TENANT_ID_HEADER,
                tenant_id,
            )
        )
    return ClientCallDetails(
        client_call_details.method,
        client_call_details.timeout,
        cast(Any, grpc.aio.Metadata(*metadata)),
        client_call_details.credentials,
        client_call_details.wait_for_ready,
    )


def _read_varint(data: bytes, offset: int) -> tuple[int, int]:
    value = 0
    shift = 0
    while True:
        current = data[offset]
        offset += 1
        value |= (current & 0x7F) << shift
        if current < 0x80:
            return value, offset
        shift += 7


def _skip_field(data: bytes, offset: int, wire_type: int) -> int:
    if wire_type == 0:
        _, offset = _read_varint(data, offset)
        return offset
    if wire_type == 2:
        length, offset = _read_varint(data, offset)
        return offset + length
    raise ValueError(f"Unsupported wire type: {wire_type}")


def _parse_duration_seconds(duration_bytes: bytes) -> Optional[float]:
    offset = 0
    seconds = 0
    nanos = 0

    while offset < len(duration_bytes):
        key, offset = _read_varint(duration_bytes, offset)
        field_number = key >> 3
        wire_type = key & 0x07

        if field_number == 1 and wire_type == 0:
            seconds, offset = _read_varint(duration_bytes, offset)
        elif field_number == 2 and wire_type == 0:
            nanos, offset = _read_varint(duration_bytes, offset)
        else:
            offset = _skip_field(duration_bytes, offset, wire_type)

    delay = seconds + (nanos / 1_000_000_000)
    return delay if delay > 0 else None


def _parse_retry_info_seconds(retry_info_bytes: bytes) -> Optional[float]:
    offset = 0
    while offset < len(retry_info_bytes):
        key, offset = _read_varint(retry_info_bytes, offset)
        field_number = key >> 3
        wire_type = key & 0x07

        if field_number == 1 and wire_type == 2:
            length, offset = _read_varint(retry_info_bytes, offset)
            duration_bytes = retry_info_bytes[offset : offset + length]
            offset += length
            return _parse_duration_seconds(duration_bytes)

        offset = _skip_field(retry_info_bytes, offset, wire_type)

    return None


def _parse_any_retry_delay_seconds(any_bytes: bytes) -> Optional[float]:
    offset = 0
    type_url: Optional[str] = None
    value: Optional[bytes] = None

    while offset < len(any_bytes):
        key, offset = _read_varint(any_bytes, offset)
        field_number = key >> 3
        wire_type = key & 0x07

        if field_number == 1 and wire_type == 2:
            length, offset = _read_varint(any_bytes, offset)
            type_url = any_bytes[offset : offset + length].decode("utf-8")
            offset += length
        elif field_number == 2 and wire_type == 2:
            length, offset = _read_varint(any_bytes, offset)
            value = any_bytes[offset : offset + length]
            offset += length
        else:
            offset = _skip_field(any_bytes, offset, wire_type)

    if type_url is None or value is None or not type_url.endswith("/google.rpc.RetryInfo"):
        return None

    return _parse_retry_info_seconds(value)


def retry_delay_seconds(error: grpc.RpcError) -> Optional[float]:
    if error.code() != grpc.StatusCode.RESOURCE_EXHAUSTED:
        return None

    trailing_metadata = cast(Any, error.trailing_metadata())
    if trailing_metadata is None:
        return None

    for entry in trailing_metadata:
        key = entry.key
        value = entry.value
        if key != STATUS_DETAILS_HEADER or not isinstance(value, bytes):
            continue

        offset = 0
        while offset < len(value):
            field_key, offset = _read_varint(value, offset)
            field_number = field_key >> 3
            wire_type = field_key & 0x07

            if field_number == 3 and wire_type == 2:
                length, offset = _read_varint(value, offset)
                detail_bytes = value[offset : offset + length]
                offset += length
                delay = _parse_any_retry_delay_seconds(detail_bytes)
                if delay is not None:
                    return delay
            else:
                offset = _skip_field(value, offset, wire_type)

    return None


class MetadataInterceptor(
    grpc.UnaryUnaryClientInterceptor, grpc.StreamStreamClientInterceptor
):
    def __init__(self, tenant_id: Optional[str]):
        self.tenant_id = tenant_id

    def intercept_unary_unary(
        self, continuation: Any, client_call_details: Any, request: Any
    ) -> Any:
        new_details = _call_details_with_tenant(self.tenant_id, client_call_details)
        return continuation(new_details, request)

    def intercept_stream_stream(
        self, continuation: Any, client_call_details: Any, request_iterator: Any
    ) -> Any:
        new_details = _call_details_with_tenant(self.tenant_id, client_call_details)
        return continuation(new_details, request_iterator)


class RetryInterceptor(grpc.UnaryUnaryClientInterceptor):
    def intercept_unary_unary(
        self, continuation: Any, client_call_details: Any, request: Any
    ) -> Any:
        while True:
            outcome = continuation(client_call_details, request)
            if not isinstance(outcome, grpc.RpcError):
                return outcome

            delay = retry_delay_seconds(outcome)
            if delay is None:
                return outcome

            time.sleep(delay)


class AsyncUnaryUnaryMetadataInterceptor(grpc.aio.UnaryUnaryClientInterceptor):
    def __init__(self, tenant_id: Optional[str]):
        self.tenant_id = tenant_id

    async def intercept_unary_unary(
        self, continuation: Any, client_call_details: Any, request: Any
    ) -> Any:
        new_details = _call_details_with_tenant(self.tenant_id, client_call_details)
        return await continuation(new_details, request)


class AsyncUnaryUnaryRetryInterceptor(grpc.aio.UnaryUnaryClientInterceptor):
    async def intercept_unary_unary(
        self, continuation: Any, client_call_details: Any, request: Any
    ) -> Any:
        while True:
            call_or_response = await continuation(client_call_details, request)
            if not hasattr(call_or_response, "__await__"):
                return call_or_response

            try:
                return await call_or_response
            except grpc.RpcError as error:
                delay = retry_delay_seconds(error)
                if delay is None:
                    raise

                await asyncio.sleep(delay)


class AsyncStreamStreamMetadataInterceptor(grpc.aio.StreamStreamClientInterceptor):
    def __init__(self, tenant_id: Optional[str]):
        self.tenant_id = tenant_id

    async def intercept_stream_stream(
        self,
        continuation: Any,
        client_call_details: Any,
        request_iterator: Any,
    ) -> Any:
        new_details = _call_details_with_tenant(self.tenant_id, client_call_details)
        return await continuation(new_details, request_iterator)


class AsyncStreamUnaryMetadataInterceptor(grpc.aio.StreamUnaryClientInterceptor):
    def __init__(self, tenant_id: Optional[str]):
        self.tenant_id = tenant_id

    async def intercept_stream_unary(
        self,
        continuation: Any,
        client_call_details: Any,
        request_iterator: Any,
    ) -> Any:
        new_details = _call_details_with_tenant(self.tenant_id, client_call_details)
        return await continuation(new_details, request_iterator)


class AsyncUnaryStreamMetadataInterceptor(grpc.aio.UnaryStreamClientInterceptor):
    def __init__(self, tenant_id: Optional[str]):
        self.tenant_id = tenant_id

    async def intercept_unary_stream(
        self,
        continuation: Any,
        client_call_details: Any,
        request: Any,
    ) -> Any:
        new_details = _call_details_with_tenant(self.tenant_id, client_call_details)
        return await continuation(new_details, request)
