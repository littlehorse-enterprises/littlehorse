import random
import time


OBI_GREETING = "Hello, there!"
VADER_GREETING = "We would be honored if you would join us!"
GRIEVOUS_GREETING = "GENERAL KENOBI!"


def echo_task(thing: str) -> str:
    return thing


def hello_there(name: str) -> str:
    return f"Hello there, {name}!"


def slow_task() -> str:
    time.sleep(5)
    return OBI_GREETING


def unreliable_task() -> str:
    assert random.random() > 0.5
    return VADER_GREETING


def increment(foo: int) -> int:
    return foo + 1


def big_blob_task() -> dict:
    return {
        "some_blob": {
            "some_int": 1,
            "some_float": 2.5,
            "some_bool": False,
        },
        "some_list": [1, 2, 3, 4],
        "some_str": "Hello, there!",
    }


def echo_int(foo: int) -> int:
    return foo


def echo_float(foo: float) -> float:
    return foo
