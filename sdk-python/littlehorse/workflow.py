from typing import Any, Callable


class NodeOutput:
    def __init__(self) -> None:
        pass


class ThreadBuilder:
    def __init__(self) -> None:
        pass

    def execute(self, task_name: str, *args: Any) -> NodeOutput:
        print(task_name)
        print(args)
        return NodeOutput()


class Workflow:
    def __init__(self, name: str, callable: Callable[[ThreadBuilder], None]) -> None:
        if name is None:
            raise ValueError("Name cannot be None")
        self.thread_builder = ThreadBuilder()
        callable(self.thread_builder)


if __name__ == "__main__":

    def my_thread_builder(thread: ThreadBuilder) -> None:
        thread.execute("my-task", "1")

    wf = Workflow("my-wf", my_thread_builder)
