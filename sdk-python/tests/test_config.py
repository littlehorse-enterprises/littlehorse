import unittest
import os
import uuid

from littlehorse.config import Config


class TestConfig(unittest.TestCase):
    def tearDown(self):
        os.environ.clear()

    def test_str(self):
        os.environ["LHC_VARIABLE"] = "my-lhc-variable"
        os.environ["LHW_VARIABLE"] = "my-lhw-variable"

        config = Config()

        self.assertEqual(
            str(config), "LHC_VARIABLE=my-lhc-variable\nLHW_VARIABLE=my-lhw-variable"
        )

    def test_shadow_secrets_str(self):
        os.environ["LHC_SECRET"] = "my-secret"
        os.environ["LHW_PASSWORD"] = "my-password"

        config = Config()

        self.assertEqual(str(config), "LHC_SECRET=******\nLHW_PASSWORD=******")

    def test_load_from_env(self):
        os.environ["NOT_A_VALUE"] = "random"
        os.environ["LHC_VARIABLE"] = "my-lhc-variable"
        os.environ["LHW_VARIABLE"] = "my-lhw-variable"

        config = Config()

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

        print("\n\tTemporary config file:", temp_config_file_path)

        config = Config()
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
