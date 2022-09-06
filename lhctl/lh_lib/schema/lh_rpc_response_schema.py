from enum import Enum
from typing import Generic, Optional, TypeVar
from lh_sdk.utils import LHBaseModel


class ResponseStatusEnum(Enum):
    OK = "OK"
    VALIDATION_ERROR = "VALIDATION_ERROR"
    OBJECT_NOT_FOUND = "OBJECT_NOT_FOUND"
    INTERNAL_ERROR = "INTERNAL_ERROR"


T = TypeVar('T')

class LHRPCResponseSchema(LHBaseModel, Generic[T]):
    message: Optional[str] = None
    status: Optional[ResponseStatusEnum] = None
    object_id: Optional[str] = None
    result: Optional[T] = None
