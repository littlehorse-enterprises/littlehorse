from datetime import datetime, timedelta
import unittest
from unittest.mock import ANY, call, patch
from littlehorse.exceptions import OAuthException
from littlehorse.auth import AccessToken, OAuthCredentialsProvider


class TestAccessToken(unittest.TestCase):
    def test_is_expired(self):
        expiries = datetime.now() - timedelta(days=1)
        access_token = AccessToken({"expires_at": expiries.timestamp()})
        self.assertTrue(access_token.is_expired())

    def test_not_is_expired(self):
        expiries = datetime.now() + timedelta(days=1)
        access_token = AccessToken({"expires_at": expiries.timestamp()})
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

    @patch("littlehorse.auth.OAuth2Session")
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

    @patch("littlehorse.auth.OAuth2Session")
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


if __name__ == "__main__":
    unittest.main()
