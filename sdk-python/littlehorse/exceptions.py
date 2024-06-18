from typing import Optional

from littlehorse.model import VariableValue


class TaskSchemaMismatchException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


class OAuthException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


class SerdeException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


class LHTaskException(Exception):
    def __init__(
        self, exception_name: str, message: str, content: Optional[VariableValue] = None
    ):
        self.message = message
        self.exception_name = exception_name
        self.content = content if content is not None else VariableValue()
        super().__init__(self.message)
