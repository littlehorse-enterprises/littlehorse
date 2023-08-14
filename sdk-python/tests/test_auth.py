from datetime import datetime, timedelta
import unittest
from unittest.mock import ANY, call, patch

from littlehorse.auth import AccessToken, GrpcAuth, OAuthException


class TestAccessToken(unittest.TestCase):
    def test_is_expired(self):
        expiries = datetime.now() - timedelta(days=1)
        access_token = AccessToken({"expires_at": expiries.timestamp()})
        self.assertTrue(access_token.is_expired())

    def test_not_is_expired(self):
        expiries = datetime.now() + timedelta(days=1)
        access_token = AccessToken({"expires_at": expiries.timestamp()})
        self.assertFalse(access_token.is_expired())


class TestGrpcAuth(unittest.TestCase):
    def test_discover_endpoint_is_not_set(self):
        grpc_auth = GrpcAuth(None, None, None)
        self.assertRaises(OAuthException, grpc_auth.issuer)

    @patch("littlehorse.auth.requests")
    def test_hit_well_known_endpoint(self, requests_package_mock):
        my_endpoint = "http://my-endpoint/"
        grpc_auth = GrpcAuth(None, None, my_endpoint)

        issuer1 = grpc_auth.issuer()
        issuer2 = grpc_auth.issuer()

        requests_package_mock.get.assert_called_once_with(
            "http://my-endpoint/.well-known/openid-configuration"
        )
        self.assertIs(issuer1, issuer2)

    @patch("littlehorse.auth.requests")
    @patch("littlehorse.auth.OAuth2Session")
    def test_get_not_expired_access_token(
        self, session_class_mock, requests_package_mock
    ):
        expiries = datetime.now() + timedelta(days=1)
        session_class_mock.return_value.fetch_token.return_value = {
            "expires_at": expiries.timestamp()
        }

        my_client = "my-client"
        my_password = "my-password"
        my_endpoint = "http://my-endpoint/"
        grpc_auth = GrpcAuth(my_client, my_password, my_endpoint)

        token1 = grpc_auth.access_token()
        token2 = grpc_auth.access_token()

        session_class_mock.return_value.fetch_token.assert_called_once_with(
            url=ANY, grant_type="client_credentials"
        )
        self.assertIs(token1, token2)

    @patch("littlehorse.auth.requests")
    @patch("littlehorse.auth.OAuth2Session")
    def test_get_expired_access_token(
        self, session_class_mock, requests_package_mock
    ):
        expiries = datetime.now() - timedelta(days=1)
        session_class_mock.return_value.fetch_token.return_value = {
            "expires_at": expiries.timestamp()
        }

        my_client = "my-client"
        my_password = "my-password"
        my_endpoint = "http://my-endpoint/"
        grpc_auth = GrpcAuth(my_client, my_password, my_endpoint)

        token1 = grpc_auth.access_token()
        token2 = grpc_auth.access_token()

        session_class_mock.return_value.fetch_token.assert_has_calls(
            [
                call(url=ANY, grant_type="client_credentials"),
                call(url=ANY, grant_type="client_credentials"),
            ]
        )
        self.assertIsNot(token1, token2)
