from datetime import datetime, timedelta
import unittest
from unittest.mock import ANY, call, patch, MagicMock, AsyncMock
import grpc
from littlehorse.exceptions import OAuthException
from littlehorse.client_interceptors import (
    AccessToken,
    OAuthCredentialsProvider,
    MetadataInterceptor,
    RetryInterceptor,
    TENANT_ID_HEADER,
    STATUS_DETAILS_HEADER,
    AsyncUnaryUnaryMetadataInterceptor,
    AsyncUnaryUnaryRetryInterceptor,
    AsyncStreamStreamMetadataInterceptor,
    AsyncUnaryStreamMetadataInterceptor,
    AsyncStreamUnaryMetadataInterceptor,
    retry_delay_seconds,
)


def encode_varint(value: int) -> bytes:
    output = bytearray()
    while value >= 0x80:
        output.append((value & 0x7F) | 0x80)
        value >>= 7
    output.append(value)
    return bytes(output)


def encode_field(field_number: int, wire_type: int, value: bytes) -> bytes:
    return encode_varint((field_number << 3) | wire_type) + value


def encode_length_delimited(field_number: int, value: bytes) -> bytes:
    return encode_field(field_number, 2, encode_varint(len(value)) + value)


def encode_retry_info_status_details(delay_seconds: int, delay_nanos: int = 0) -> bytes:
    duration = encode_field(1, 0, encode_varint(delay_seconds))
    if delay_nanos > 0:
        duration += encode_field(2, 0, encode_varint(delay_nanos))

    retry_info = encode_length_delimited(1, duration)
    any_message = encode_length_delimited(1, b"type.googleapis.com/google.rpc.RetryInfo") + encode_length_delimited(2, retry_info)
    return encode_field(1, 0, encode_varint(8)) + encode_length_delimited(3, any_message)


class FakeMetadataEntry:
    def __init__(self, key: str, value: bytes):
        self.key = key
        self.value = value


class FakeRpcError(grpc.RpcError):
    def __init__(self, code: grpc.StatusCode, trailing_metadata=None):
        self._code = code
        self._trailing_metadata = tuple(trailing_metadata or ())

    def code(self):
        return self._code

    def trailing_metadata(self):
        return self._trailing_metadata


class AwaitableOutcome:
    def __init__(self, response=None, error=None):
        self._response = response
        self._error = error

    def __await__(self):
        async def _run():
            if self._error is not None:
                raise self._error
            return self._response

        return _run().__await__()


class TestAccessToken(unittest.TestCase):
    def test_is_expired(self):
        expiries = datetime.now() - timedelta(days=1)
        access_token = AccessToken({"expires_at": str(expiries.timestamp())})
        self.assertTrue(access_token.is_expired())

    def test_not_is_expired(self):
        expiries = datetime.now() + timedelta(days=1)
        access_token = AccessToken({"expires_at": str(expiries.timestamp())})
        self.assertFalse(access_token.is_expired())


class TestOAuthCredentialsProvider(unittest.TestCase):
    def test_oauth_token_endpoint_is_not_set(self):
        grpc_auth = OAuthCredentialsProvider("some-id", "some-secret", None)
        with self.assertRaises(OAuthException) as exception_context:
            grpc_auth.access_token()
        self.assertEqual(
            "LHC_OAUTH_ACCESS_TOKEN_URL required",
            str(exception_context.exception),
        )

    def test_oauth_client_id_not_set(self):
        grpc_auth = OAuthCredentialsProvider(None, "some-secret", "http://my-endpoint/")
        with self.assertRaises(OAuthException) as exception_context:
            grpc_auth.access_token()
        self.assertEqual(
            "LHC_OAUTH_CLIENT_ID required",
            str(exception_context.exception),
        )

    def test_oauth_client_secret_not_set(self):
        grpc_auth = OAuthCredentialsProvider("some-id", None, "http://my-endpoint/")
        with self.assertRaises(OAuthException) as exception_context:
            grpc_auth.access_token()
        self.assertEqual(
            "LHC_OAUTH_CLIENT_SECRET required",
            str(exception_context.exception),
        )

    @patch("littlehorse.client_interceptors.OAuth2Session")
    def test_get_not_expired_access_token(self, session_class_mock):
        expiries = datetime.now() + timedelta(days=1)
        session_class_mock.return_value.fetch_token.return_value = {
            "expires_at": expiries.timestamp()
        }

        my_client = "my-client"
        my_password = "my-password"
        my_endpoint = "http://my-endpoint/"
        grpc_auth = OAuthCredentialsProvider(my_client, my_password, my_endpoint)

        token1 = grpc_auth.access_token()
        token2 = grpc_auth.access_token()

        session_class_mock.return_value.fetch_token.assert_called_once_with(
            url=ANY, grant_type="client_credentials"
        )
        self.assertIs(token1, token2)

    @patch("littlehorse.client_interceptors.OAuth2Session")
    def test_get_expired_access_token(self, session_class_mock):
        expiries = datetime.now() - timedelta(days=1)
        session_class_mock.return_value.fetch_token.return_value = {
            "expires_at": expiries.timestamp()
        }

        my_client = "my-client"
        my_password = "my-password"
        my_endpoint = "http://my-endpoint/"
        grpc_auth = OAuthCredentialsProvider(my_client, my_password, my_endpoint)

        token1 = grpc_auth.access_token()
        token2 = grpc_auth.access_token()

        session_class_mock.return_value.fetch_token.assert_has_calls(
            [
                call(url=ANY, grant_type="client_credentials"),
                call(url=ANY, grant_type="client_credentials"),
            ]
        )
        self.assertIsNot(token1, token2)


class TestMetadataInterceptor(unittest.TestCase):
    def setUp(self):
        self.tenant_a_interceptor = MetadataInterceptor("A")

    def assert_tenant_a_header(self, client_call_details, request):
        self.assertEqual(client_call_details.metadata[TENANT_ID_HEADER], "A")

    def test_add_tenant_metadata_to_unary_unary(self):
        request_details = MagicMock()
        request_details.return_value.metadata = None
        request_details.return_value.method = "my-method"
        request_details.return_value.timeout = 0
        request_details.return_value.credentials = None
        request_details.return_value.wait_for_ready = None
        self.tenant_a_interceptor.intercept_unary_unary(
            self.assert_tenant_a_header, request_details, None
        )

    def test_add_tenant_metadata_to_stream_stream(self):
        request_details = MagicMock()
        request_details.return_value.metadata = None
        request_details.return_value.method = "my-method"
        request_details.return_value.timeout = 0
        request_details.return_value.credentials = None
        request_details.return_value.wait_for_ready = None
        self.tenant_a_interceptor.intercept_stream_stream(
            self.assert_tenant_a_header, request_details, None
        )


class TestRetryInterceptor(unittest.TestCase):
    def setUp(self):
        self.error = FakeRpcError(
            grpc.StatusCode.RESOURCE_EXHAUSTED,
            [
                FakeMetadataEntry(
                    STATUS_DETAILS_HEADER,
                    encode_retry_info_status_details(1, 500_000_000),
                )
            ],
        )

    def test_extract_retry_delay(self):
        self.assertEqual(retry_delay_seconds(self.error), 1.5)

    @patch("littlehorse.client_interceptors.time.sleep")
    def test_retry_unary_call(self, sleep_mock):
        interceptor = RetryInterceptor()
        outcomes = [self.error, "ok"]

        def continuation(details, request):
            return outcomes.pop(0)

        result = interceptor.intercept_unary_unary(continuation, MagicMock(), None)

        self.assertEqual(result, "ok")
        sleep_mock.assert_called_once_with(1.5)


class TestAsyncMetadataInterceptor(unittest.IsolatedAsyncioTestCase):
    def setUp(self):
        self.unary_unary_interceptor = AsyncUnaryUnaryMetadataInterceptor("B")
        self.retry_interceptor = AsyncUnaryUnaryRetryInterceptor()
        self.stream_stream_interceptor = AsyncStreamStreamMetadataInterceptor("B")
        self.unary_stream_interceptor = AsyncUnaryStreamMetadataInterceptor("B")
        self.stream_unary_interceptor = AsyncStreamUnaryMetadataInterceptor("B")

    async def assert_tenant_b_header(self, client_call_details, request):
        self.assertEqual(client_call_details.metadata[TENANT_ID_HEADER], "B")

    async def test_add_tenant_metadata_to_unary_unary(self):
        request_details = MagicMock()
        request_details.return_value.metadata = None
        request_details.return_value.method = "my-method"
        request_details.return_value.timeout = 0
        request_details.return_value.credentials = None
        request_details.return_value.wait_for_ready = None
        await self.unary_unary_interceptor.intercept_unary_unary(
            self.assert_tenant_b_header, request_details, MagicMock()
        )

    async def test_add_tenant_metadata_to_stream_stream(self):
        request_details = MagicMock()
        request_details.return_value.metadata = None
        request_details.return_value.method = "my-method"
        request_details.return_value.timeout = 0
        request_details.return_value.credentials = None
        request_details.return_value.wait_for_ready = None
        await self.stream_stream_interceptor.intercept_stream_stream(
            self.assert_tenant_b_header, request_details, None
        )

    async def test_add_tenant_metadata_to_unary_stream(self):
        request_details = MagicMock()
        request_details.return_value.metadata = None
        request_details.return_value.method = "my-method"
        request_details.return_value.timeout = 0
        request_details.return_value.credentials = None
        request_details.return_value.wait_for_ready = None
        await self.unary_stream_interceptor.intercept_unary_stream(
            self.assert_tenant_b_header, request_details, None
        )

    async def test_add_tenant_metadata_to_stream_unary(self):
        request_details = MagicMock()
        request_details.return_value.metadata = None
        request_details.return_value.method = "my-method"
        request_details.return_value.timeout = 0
        request_details.return_value.credentials = None
        request_details.return_value.wait_for_ready = None
        await self.stream_unary_interceptor.intercept_stream_unary(
            self.assert_tenant_b_header, request_details, None
        )

    @patch("littlehorse.client_interceptors.asyncio.sleep", new_callable=AsyncMock)
    async def test_retry_async_unary_call(self, sleep_mock):
        error = FakeRpcError(
            grpc.StatusCode.RESOURCE_EXHAUSTED,
            [
                FakeMetadataEntry(
                    STATUS_DETAILS_HEADER,
                    encode_retry_info_status_details(2),
                )
            ],
        )
        outcomes = [AwaitableOutcome(error=error), AwaitableOutcome(response="ok")]

        async def continuation(details, request):
            return outcomes.pop(0)

        result = await self.retry_interceptor.intercept_unary_unary(
            continuation, MagicMock(), None
        )

        self.assertEqual(result, "ok")
        sleep_mock.assert_awaited_once_with(2.0)


if __name__ == "__main__":
    unittest.main()
