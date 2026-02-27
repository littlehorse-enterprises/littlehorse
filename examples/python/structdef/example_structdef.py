"""LittleHorse StructDef Example — Python

This example mirrors the Java and Go ``struct-def`` examples.  It defines
three struct types (``Address``, ``Person``, ``ParkingTicketReport``),
registers them as ``StructDef`` metadata on the LH Server, registers the
``TaskDef`` and ``WfSpec``, and finally starts the task workers.

Usage::

    # Start the task workers (blocks forever):
    python example_structdef.py

    # In a separate terminal, submit a WfRun:
    python example_structdef.py run Toyota Camry ABC-123
"""

from __future__ import annotations

import asyncio
import logging
import sys
from pathlib import Path
from typing import Annotated

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.lh_struct import (
    LHStructField,
    lh_struct_def,
    serialize_to_struct,
)
from littlehorse.model import VariableValue
from littlehorse.model.variable_pb2 import Struct
from littlehorse.worker import LHTaskWorker
from littlehorse.workflow import Workflow, WorkflowThread

logging.basicConfig(level=logging.INFO)

# ---------------------------------------------------------------------------
# Struct definitions
# ---------------------------------------------------------------------------


@lh_struct_def(name="address", description="A physical address.")
class Address:
    house_number: int
    street: str
    city: str
    planet: str
    zip_code: int


@lh_struct_def(name="person", description="A person with a name and address.")
class Person:
    first_name: str
    last_name: str
    home_address: Address


@lh_struct_def(
    name="parking-ticket-report",
    description="A parking ticket report for a vehicle.",
)
class ParkingTicketReport:
    vehicle_make: str
    vehicle_model: str
    license_plate_number: Annotated[str, LHStructField(masked=True)]


# ---------------------------------------------------------------------------
# Task functions
# ---------------------------------------------------------------------------


async def get_car_owner(report: ParkingTicketReport) -> Person:
    """Look up the car owner from the report."""
    return _lookup_car_owner_in_db(report.license_plate_number)


async def mail_ticket(person: Person) -> str:
    """Mail a parking ticket to a person."""
    print(f"Notifying {person.first_name} {person.last_name} of parking ticket.")
    return (
        f"Ticket sent to {person.first_name} {person.last_name} "
        f"at {person.home_address.house_number} {person.home_address.street}, "
        f"{person.home_address.city}, {person.home_address.planet} "
        f"{person.home_address.zip_code}"
    )


def _lookup_car_owner_in_db(license_plate: str) -> Person:
    """Simulate a database lookup."""
    return Person(
        first_name="Obi-Wan",
        last_name="Kenobi",
        home_address=Address(
            house_number=124,
            street="Sand Dune Lane",
            city="Anchorhead",
            planet="Tattooine",
            zip_code=97412,
        ),
    )


# ---------------------------------------------------------------------------
# Workflow Definition
# ---------------------------------------------------------------------------

WORKFLOW_NAME = "issue-parking-ticket"
GET_CAR_OWNER_TASK = "get-car-owner"
MAIL_TICKET_TASK = "mail-ticket"


def get_workflow() -> Workflow:
    def my_entrypoint(wf: WorkflowThread) -> None:
        ticket_report = wf.declare_struct(
            "ticket-report", ParkingTicketReport
        ).required()
        car_owner = wf.declare_struct("car-owner", Person)
        car_owner.assign(wf.execute(GET_CAR_OWNER_TASK, ticket_report))
        wf.execute(MAIL_TICKET_TASK, car_owner)

    return Workflow(WORKFLOW_NAME, my_entrypoint)


# ---------------------------------------------------------------------------
# Config
# ---------------------------------------------------------------------------


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------


async def main() -> None:
    config = get_config()
    wf = get_workflow()

    # 1. Register StructDefs (dependencies first)
    littlehorse.create_struct_def(Address, config)
    littlehorse.create_struct_def(Person, config)
    littlehorse.create_struct_def(ParkingTicketReport, config)

    # 2. Register TaskDefs
    littlehorse.create_task_def(get_car_owner, GET_CAR_OWNER_TASK, config)
    littlehorse.create_task_def(mail_ticket, MAIL_TICKET_TASK, config)

    # 3. Register WfSpec
    littlehorse.create_workflow_spec(wf, config)

    # 4. Start task workers
    workers = [
        LHTaskWorker(get_car_owner, GET_CAR_OWNER_TASK, config),
        LHTaskWorker(mail_ticket, MAIL_TICKET_TASK, config),
    ]
    await littlehorse.start(*workers)


async def run_wf(vehicle_make: str, vehicle_model: str, license_plate: str) -> None:
    """Submit a WfRun with the given parking ticket info."""
    config = get_config()
    stub = config.stub()

    report = ParkingTicketReport(
        vehicle_make=vehicle_make,
        vehicle_model=vehicle_model,
        license_plate_number=license_plate,
    )

    report_struct = serialize_to_struct(report)

    result = stub.RunWf(
        littlehorse.model.RunWfRequest(
            wf_spec_name=WORKFLOW_NAME,
            variables={
                "ticket-report": VariableValue(struct=report_struct),
            },
        )
    )
    print(f"Started WfRun: {result}")


if __name__ == "__main__":
    if len(sys.argv) > 1 and sys.argv[1] == "run":
        if len(sys.argv) < 5:
            print("Usage: python example_structdef.py run <make> <model> <plate>")
            sys.exit(1)
        asyncio.run(run_wf(sys.argv[2], sys.argv[3], sys.argv[4]))
    else:
        asyncio.run(main())
