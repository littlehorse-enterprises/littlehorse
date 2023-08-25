import unittest
import os
from unittest.mock import ANY, mock_open, patch
import uuid

from littlehorse.config import LHConfig


class TestConfig(unittest.TestCase):
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
        os.environ["LHC_CA_CERT"] = "my-path"
        config = LHConfig()
        self.assertTrue(config.is_secure())

    def test_is_not_secure(self):
        config = LHConfig()
        self.assertFalse(config.is_secure())

    def test_needs_credentials(self):
        os.environ["LHC_OAUTH_CLIENT_ID"] = "my-client_id"
        config = LHConfig()
        self.assertTrue(config.needs_credentials())

    def test_does_not_need_credentials(self):
        config = LHConfig()
        self.assertFalse(config.needs_credentials())

    @patch("littlehorse.config.grpc")
    def test_establish_insecure_channel(self, grpc_package_mock):
        config = LHConfig()
        config.establish_channel()
        grpc_package_mock.insecure_channel.assert_called_once_with("localhost:2023")

    @patch("littlehorse.config.grpc")
    def test_establish_insecure_channel_with_custom_server(self, grpc_package_mock):
        config = LHConfig()
        config.establish_channel("192.10.10.20:5555")
        grpc_package_mock.insecure_channel.assert_called_once_with("192.10.10.20:5555")

    @patch("builtins.open", new_callable=mock_open, read_data="data")
    @patch("littlehorse.config.grpc")
    def test_establish_secure_channel(self, grpc_package_mock, mock_file):
        os.environ["LHC_CA_CERT"] = "my-path"
        config = LHConfig()
        config.establish_channel()
        grpc_package_mock.secure_channel.assert_called_once_with("localhost:2023", ANY)

    @patch("builtins.open", new_callable=mock_open, read_data="data")
    @patch("littlehorse.config.grpc")
    def test_establish_secure_channel_with_custom_server(
        self, grpc_package_mock, mock_file
    ):
        os.environ["LHC_CA_CERT"] = "my-path"
        config = LHConfig()
        config.establish_channel(server="192.10.10.20:5555")
        grpc_package_mock.secure_channel.assert_called_once_with(
            "192.10.10.20:5555", ANY
        )

    @patch("builtins.open", new_callable=mock_open, read_data="data")
    @patch("littlehorse.config.GrpcAuth")
    @patch("littlehorse.config.grpc")
    def test_establish_secure_channel_and_oauth(
        self, grpc_package_mock, grpc_auth_class_mock, mock_file
    ):
        os.environ["LHC_CA_CERT"] = "my-path"
        os.environ["LHC_OAUTH_CLIENT_ID"] = "my-client_id"
        config = LHConfig()
        config.establish_channel()
        grpc_package_mock.metadata_call_credentials.assert_called_once_with(
            grpc_auth_class_mock.return_value
        )
        grpc_package_mock.secure_channel.assert_called_once_with("localhost:2023", ANY)


if __name__ == "__main__":
    unittest.main()
