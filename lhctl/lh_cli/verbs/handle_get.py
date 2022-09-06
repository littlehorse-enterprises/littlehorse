from argparse import _SubParsersAction, ArgumentParser, Namespace
import json
from typing import Any, Generic, Mapping, TypeVar

from lh_lib.client import LHClient
from lh_lib.schema import RESOURCE_TYPES
from lh_lib.schema.lh_rpc_response_schema import LHRPCResponseSchema
from lh_lib.schema.wf_run_schema import ThreadRunSchema, WFRunSchema
from lh_lib.schema.wf_spec_schema import (
    WFSpecSchema, 
    ExternalEventDefSchema,
    TaskDefSchema,
)
from lh_lib.utils.printer import Printer
from lh_sdk.utils import stringify


T = TypeVar('T')


class GettableResource(Generic[T]):
    def print_resource(self, response: LHRPCResponseSchema[T]):
        print(f"Resource Id: \t\t{response.object_id}")
        print(f"Response Status: \t{response.status}")
        print(f"Response Message: \t{response.message}")

        if response.result is None:
            return

        r = response.result
        if hasattr(r, "status"):
            print(f"Resource Status: \t{r.status}") # type: ignore
        if hasattr(r, "name"):
            print(f"Resource Name: \t\t{r.name}") # type: ignore


class GETWFRun(GettableResource[WFRunSchema]):
    def print_resource(self, response: LHRPCResponseSchema[WFRunSchema]):
        wf_run = response.result
        printer = Printer()
        if wf_run is None:
            printer.print("No resources found!")
            return
        printer.print("WFRun Status:", wf_run.status.value)

        if wf_run.start_time is not None and wf_run.end_time is not None:
            runtime = wf_run.end_time - wf_run.start_time
            printer.print("Execution time: ", runtime)

        if wf_run.error_code is not None:
            printer.print("Error Code:", wf_run.error_code)
        if wf_run.error_message is not None:
            printer.print("Error Message:", wf_run.error_message)
        printer.print("Threads:")
        printer.indent()
        for trun in wf_run.thread_runs:
            self._print_thread_run(printer, trun)
    
    def _print_thread_run(self, printer: Printer, trun: ThreadRunSchema):
        print("--->>")
        printer.print("Id: ", trun.id)
        if trun.is_interrupt_thread:
            printer.print("Interrupt thread!")
        printer.print("Status: ", trun.status.value)
        if trun.error_message is not None:
            printer.print("Error Message:", trun.error_message)

        printer.print("Tasks:")
        printer.indent()

        for task in trun.task_runs:
            if task.stdout is None:
                adjusted_stdout = None
            else:
                adjusted_stdout = stringify(task.stdout).rstrip("\n")

            if task.stderr is not None:
                adjusted_stdout = str(adjusted_stdout) + ' ||| Stderr:' + stringify(
                    task.stderr
                )

            if task.failure_message is not None:
                adjusted_stdout = adjusted_stdout or ''
                adjusted_stdout += "|||ERROR MESSAGE: " + task.failure_message

            printer.print(f"{task.node_name}: {adjusted_stdout}")
        printer.unindent()

        up_next = trun.up_next
        if len(up_next) > 0:
            next_edge = up_next[0]
            printer.print(f"Waiting on node {next_edge.edge.sink_node_name}")

        if trun.variables is not None and len(trun.variables) > 0:
            printer.print("Variables:")
        printer.indent()
        for varname in (trun.variables or {}).keys():
            assert trun.variables is not None
            printer.print(varname, ": ", json.dumps(trun.variables[varname]))
        printer.unindent()


class GETWFSpec(GettableResource[WFSpecSchema]):
    # Just print basic stuff for now
    pass


class GETTaskDef(GettableResource[TaskDefSchema]):
    # Just print basic stuff for now
    pass


class GETExternalEventDef(GettableResource[ExternalEventDefSchema]):
    # Just print basic stuff for now
    pass


GETTABLE_RESOURCES: Mapping[str, GettableResource] = {
    "WFRun": GETWFRun(),
    "WFSpec": GETWFSpec(),
    "TaskDef": GETTaskDef(),
    "ExternalEventDef": GETExternalEventDef(),
}


class GETHandler:
    def __init__(self):
        pass

    def init_subparsers(self, base_subparsers: _SubParsersAction):
        parser: ArgumentParser = base_subparsers.add_parser(
            "get",
            help="Get information about a specified Resource Name and Resource Type."
        )

        parser.add_argument(
            "resource_type",
            choices=[k for k in GETTABLE_RESOURCES.keys()],
            help="Resource Type to Get."
        )

        parser.add_argument(
            "resource_id",
            help="Specific Id or Name of resource to get."
        )
        parser.add_argument(
            "--raw-json", "-r",
            action="store_true",
            help="Print out raw JSON response. If this flag is false: Print summary.",
        )

        parser.set_defaults(func=self.get_resource)

    def get_resource(self, ns: Namespace, client: LHClient):
        rt_name: str = ns.resource_type
        resource_id: str = ns.resource_id

        rt_schema = RESOURCE_TYPES[rt_name]

        response: LHRPCResponseSchema = client.get_resource_by_name_or_id(
            rt_schema,
            resource_id,
        )

        if ns.raw_json:
            print(response.json(by_alias=True))
        else:
            GETTABLE_RESOURCES[rt_name].print_resource(response)
