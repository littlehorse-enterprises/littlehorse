from grpc._channel import _InactiveRpcError


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


class GrpcException(Exception):
    def __init__(self, original: _InactiveRpcError) -> None:
        self.code = original.code()
        self.details = original.details()
        self.message = f"{self.details} ({self.code.name})"
        super().__init__(self.message)
