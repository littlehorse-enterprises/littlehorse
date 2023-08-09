import os
from jproperties import Properties

PREFIXES = ("LHC_", "LHW_")


class Config:
    """Littlehorse Client/Worker configuration.
    A property configured using an environment property
    overrides the value provided using a worker.config file.
    """

    def __init__(self) -> None:
        self.configs = {
            key.upper(): value
            for key, value in os.environ.items()
            if key.startswith(PREFIXES)
        }

    def __str__(self) -> str:
        return "\n".join(
            [
                f"{key}={'******' if 'SECRET' in key or 'PASSWORD' in key else value}"
                for key, value in self.configs.items()
            ]
        )

    def load(self, file_path: str) -> None:
        """Load configurations from properties file.

        Args:
            file_path (str): Path to the properties file
        """
        properties = Properties()
        with open(file_path, "rb") as file_input:
            properties.load(file_input, "utf-8")

        new_configs = {
            key.upper(): value.data
            for key, value in properties.items()
            if key.startswith(PREFIXES)
        }
        new_configs.update(self.configs)

        self.configs = new_configs


if __name__ == "__main__":
    config = Config()
