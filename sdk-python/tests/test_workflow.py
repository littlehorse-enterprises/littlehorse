import unittest

from littlehorse.workflow import ThreadBuilder, Workflow


class TestWfRunVariable(unittest.TestCase):
    pass


class TestWorkflow(unittest.TestCase):
    def test_entrypoint_is_a_function(self):
        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", "")
        self.assertEqual(
            "Object is not a ThreadInitializer",
            str(exception_context.exception),
        )

    def test_entrypoint_has_one_parameter(self):
        def my_entrypoint(thread: ThreadBuilder, another: str) -> None:
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            "ThreadInitializer receives only one parameter",
            str(exception_context.exception),
        )

    def test_entrypoint_receives_thread_builder(self):
        def my_entrypoint(thread: str) -> None:
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            "ThreadInitializer receives a ThreadBuilder",
            str(exception_context.exception),
        )

    def test_entrypoint_returns_none(self):
        def my_entrypoint(thread: ThreadBuilder):
            pass

        with self.assertRaises(TypeError) as exception_context:
            Workflow("my-wf", my_entrypoint)
        self.assertEqual(
            "ThreadInitializer returns None",
            str(exception_context.exception),
        )

    def test_validate_entrypoint(self):
        def my_entrypoint(thread: ThreadBuilder) -> None:
            pass

        Workflow("my-wf", my_entrypoint)


if __name__ == "__main__":
    unittest.main()
