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
    def __init__(self, exception_name: str, message: str):
        self.message = message
        self.exception_name = exception_name
        super().__init__(self.message)
