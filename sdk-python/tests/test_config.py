import unittest
import os
from unittest.mock import ANY, mock_open, patch
import uuid

from littlehorse.config import LHConfig


class TestLHConfig(unittest.TestCase):
    def tearDown(self):
        os.environ.clear()

    def test_str(self):
        os.environ["LHC_VARIABLE"] = "my-lhc-variable"
        os.environ["LHW_VARIABLE"] = "my-lhw-variable"

        config = LHConfig()

        self.assertEqual(
            str(config), "LHC_VARIABLE=my-lhc-variable\nLHW_VARIABLE=my-lhw-variable"
        )

    def test_shadow_secrets_str(self):
        os.environ["LHC_SECRET"] = "my-secret"
        os.environ["LHW_PASSWORD"] = "my-password"

        config = LHConfig()

        self.assertEqual(str(config), "LHC_SECRET=******\nLHW_PASSWORD=******")

    def test_load_from_env(self):
        os.environ["NOT_A_VALUE"] = "random"
        os.environ["LHC_VARIABLE"] = "my-lhc-variable"
        os.environ["LHW_VARIABLE"] = "my-lhw-variable"

        config = LHConfig()

        self.assertDictEqual(
            config.configs,
            {"LHC_VARIABLE": "my-lhc-variable", "LHW_VARIABLE": "my-lhw-variable"},
        )

    def test_load_from_file(self):
        os.environ["LHC_VARIABLE"] = "my-lhc-variable-from-env"
        os.environ["LHC_VARIABLE_2"] = "my-lhc-variable-2-from-env"
        os.environ["LHW_VARIABLE"] = "my-lhw-variable-from-env"

        temp_config_file_path = f"/tmp/sdk-python-test-{uuid.uuid4()}.config"

        with open(temp_config_file_path, "w") as file_input:
            file_input.write("LHC_VARIABLE=my-lhc-variable-from-file\n")
            file_input.write("LHW_VARIABLE=my-lhw-variable-from-file\n")
            file_input.write("NOT_A_VALUE=random\n")
            file_input.write("LHC_VARIABLE_FROM_FILE=my-lhc-variable-from-file\n")
            file_input.write("LHW_VARIABLE_FROM_FILE=my-lhw-variable-from-file\n")

        config = LHConfig()
        config.load(temp_config_file_path)

        self.assertDictEqual(
            config.configs,
            {
                "LHC_VARIABLE": "my-lhc-variable-from-env",
                "LHC_VARIABLE_2": "my-lhc-variable-2-from-env",
                "LHW_VARIABLE": "my-lhw-variable-from-env",
                "LHC_VARIABLE_FROM_FILE": "my-lhc-variable-from-file",
                "LHW_VARIABLE_FROM_FILE": "my-lhw-variable-from-file",
            },
        )

    def test_get_or_default(self):
        os.environ["LHC_VARIABLE"] = "my-lhc-variable"
        config = LHConfig()

        self.assertEqual(config.get("LHC_VARIABLE"), "my-lhc-variable")
        self.assertEqual(
            config.get_or_set_default("LHC_DEFAULT_VARIABLE", "my-default-value"),
            "my-default-value",
        )
        self.assertEqual(config.get("LHC_DEFAULT_VARIABLE"), "my-default-value")
        self.assertIsNone(config.get("LHC_UNSET_VARIABLE"))

    def test_get_bootstrap_server(self):
        config = LHConfig()
        self.assertEqual(config.bootstrap_server, "localhost:2023")

    def test_get_bootstrap_server_with_dns(self):
        os.environ["LHC_API_HOST"] = "my-dns"
        config = LHConfig()
        self.assertEqual(config.bootstrap_server, "my-dns:2023")

    def test_get_bootstrap_server_with_port(self):
        os.environ["LHC_API_PORT"] = "5050"
        config = LHConfig()
        self.assertEqual(config.bootstrap_server, "localhost:5050")

    def test_is_secure(self):
        os.environ["LHC_API_PROTOCOL"] = "TLS"
        config = LHConfig()
        self.assertTrue(config.is_secure())

    def test_raise_exp_if_protocol_is_invalid_in_is_secure(self):
        os.environ["LHC_API_PROTOCOL"] = "NOT_A_PROTOCOL"
        config = LHConfig()

        with self.assertRaises(ValueError) as exception_context:
            config.is_secure()

        self.assertEqual(
            "Invalid protocol: NOT_A_PROTOCOL",
            str(exception_context.exception),
        )

    def test_is_secure_lower_case(self):
        os.environ["LHC_API_PROTOCOL"] = "tls"
        config = LHConfig()
        self.assertTrue(config.is_secure())

    def test_is_not_secure(self):
        config = LHConfig()
        self.assertFalse(config.is_secure())

    def test_needs_credentials(self):
        os.environ["LHC_OAUTH_CLIENT_ID"] = "my-client_id"
        config = LHConfig()
        self.assertTrue(config.has_authentication())

    def test_does_not_need_credentials(self):
        config = LHConfig()
        self.assertFalse(config.has_authentication())

    @patch("littlehorse.config.grpc")
    def test_establish_insecure_channel(self, grpc_package_mock):
        config = LHConfig()
        config.establish_channel()
        grpc_package_mock.insecure_channel.assert_called_once_with(
            "localhost:2023",
            options=[
                ("grpc.keepalive_time_ms", 45000),
                ("grpc.keepalive_timeout_ms", 5000),
                ("grpc.keepalive_permit_without_calls", True),
                ("grpc.http2.max_pings_without_data", 0),
            ],
        )

    @patch("littlehorse.config.grpc")
    def test_establish_insecure_channel_with_custom_server(self, grpc_package_mock):
        config = LHConfig()
        config.establish_channel("192.10.10.20:5555")
        grpc_package_mock.insecure_channel.assert_called_once_with(
            "192.10.10.20:5555",
            options=[
                ("grpc.keepalive_time_ms", 45000),
                ("grpc.keepalive_timeout_ms", 5000),
                ("grpc.keepalive_permit_without_calls", True),
                ("grpc.http2.max_pings_without_data", 0),
            ],
        )

    @patch("builtins.open", new_callable=mock_open, read_data="data")
    @patch("littlehorse.config.grpc")
    def test_establish_secure_channel(self, grpc_package_mock, mock_file):
        os.environ["LHC_API_PROTOCOL"] = "TLS"
        config = LHConfig()
        config.establish_channel()
        grpc_package_mock.secure_channel.assert_called_once_with(
            "localhost:2023",
            ANY,
            options=[
                ("grpc.keepalive_time_ms", 45000),
                ("grpc.keepalive_timeout_ms", 5000),
                ("grpc.keepalive_permit_without_calls", True),
                ("grpc.http2.max_pings_without_data", 0),
            ],
        )

    @patch("builtins.open", new_callable=mock_open, read_data="data")
    @patch("littlehorse.config.grpc")
    def test_establish_secure_channel_with_custom_server(
        self, grpc_package_mock, mock_file
    ):
        os.environ["LHC_API_PROTOCOL"] = "TLS"
        config = LHConfig()
        config.establish_channel(server="192.10.10.20:5555")
        grpc_package_mock.secure_channel.assert_called_once_with(
            "192.10.10.20:5555",
            ANY,
            options=[
                ("grpc.keepalive_time_ms", 45000),
                ("grpc.keepalive_timeout_ms", 5000),
                ("grpc.keepalive_permit_without_calls", True),
                ("grpc.http2.max_pings_without_data", 0),
            ],
        )

    @patch("builtins.open", new_callable=mock_open, read_data="data")
    @patch("littlehorse.config.OAuthCredentialsProvider")
    @patch("littlehorse.config.grpc")
    def test_establish_secure_channel_and_oauth(
        self, grpc_package_mock, grpc_auth_class_mock, mock_file
    ):
        os.environ["LHC_API_PROTOCOL"] = "TLS"
        os.environ["LHC_OAUTH_CLIENT_ID"] = "my-client_id"
        config = LHConfig()
        config.establish_channel()
        grpc_package_mock.metadata_call_credentials.assert_called_once_with(
            grpc_auth_class_mock.return_value
        )
        grpc_package_mock.secure_channel.assert_called_once_with(
            "localhost:2023",
            ANY,
            options=[
                ("grpc.keepalive_time_ms", 45000),
                ("grpc.keepalive_timeout_ms", 5000),
                ("grpc.keepalive_permit_without_calls", True),
                ("grpc.http2.max_pings_without_data", 0),
            ],
        )


if __name__ == "__main__":
    unittest.main()
