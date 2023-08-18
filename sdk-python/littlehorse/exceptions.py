class TaskSchemaMismatchException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


class InvalidTaskDefNameException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


class OAuthException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)


class UnknownApiException(Exception):
    def __init__(self, message: str):
        self.message = message
        super().__init__(self.message)
