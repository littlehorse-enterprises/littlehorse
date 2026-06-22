import asyncio
import logging
import random
from pathlib import Path
from typing import Annotated

import littlehorse
from littlehorse.config import LHConfig
from littlehorse.model import (
    CorrelatedEventConfig,
    LHErrorType,
    PutExternalEventDefRequest,
)
from littlehorse.worker import LHTaskWorker, LHType, WorkerContext
from littlehorse.workflow import Workflow, WorkflowThread

logging.basicConfig(level=logging.INFO)


def get_config() -> LHConfig:
    config = LHConfig()
    config_path = Path.home().joinpath(".config", "littlehorse.config")
    if config_path.exists():
        config.load(config_path)
    return config


def get_workflow() -> Workflow:
    def quickstart_workflow(wf: WorkflowThread) -> None:
        full_name = wf.declare_str("full-name").searchable().required()
        email = wf.declare_str("email").searchable().required()
        ssn = wf.declare_int("ssn").masked().required()
        identity_verified = wf.declare_bool("identity-verified").searchable()

        wf.execute("verify-identity", full_name, email, ssn, retries=3)

        identity_verification_result = wf.wait_for_event(
            "identity-verified",
            timeout=60 * 5,
            correlation_id=email,
        )

        def handle_error(handler: WorkflowThread) -> None:
            handler.execute("notify-customer-not-verified", full_name, email)
            handler.fail("customer-not-verified", "Unable to verify customer identity in time.")

        wf.handle_error(identity_verification_result, handle_error, LHErrorType.TIMEOUT)
        identity_verified.assign(identity_verification_result)

        def if_body(body: WorkflowThread) -> None:
            body.execute("notify-customer-verified", full_name, email)

        def else_body(body: WorkflowThread) -> None:
            body.execute("notify-customer-not-verified", full_name, email)

        wf.do_if(identity_verified.is_equal_to(True), if_body, else_body)

    return Workflow("quickstart", quickstart_workflow)


async def verify_identity(
    full_name: str,
    email: str,
    ssn: Annotated[int, LHType(name="ssn", masked=True)],
    ctx: WorkerContext,
) -> str:
    del ctx
    if random.random() < 0.25:
        raise RuntimeError("The external identity verification API is down")
    return f"Successfully called external API to request verification for {full_name} at {email}"


async def notify_customer_verified(full_name: str, email: str, ctx: WorkerContext) -> str:
    del ctx
    return f"Notification sent to customer {full_name} at {email} that their identity has been verified"


async def notify_customer_not_verified(full_name: str, email: str, ctx: WorkerContext) -> str:
    del ctx
    return f"Notification sent to customer {full_name} at {email} that their identity has not been verified"


async def main() -> None:
    config = get_config()
    workflow = get_workflow()

    config.stub().PutExternalEventDef(
        PutExternalEventDefRequest(
            correlated_event_config=CorrelatedEventConfig(),
            name="identity-verified",
        )
    )

    littlehorse.create_task_def(verify_identity, "verify-identity", config)
    littlehorse.create_task_def(notify_customer_verified, "notify-customer-verified", config)
    littlehorse.create_task_def(notify_customer_not_verified, "notify-customer-not-verified", config)
    littlehorse.create_workflow_spec(workflow, config)

    logging.info("Registered quickstart metadata and starting task workers.")
    await littlehorse.start(
        LHTaskWorker(verify_identity, "verify-identity", config),
        LHTaskWorker(notify_customer_verified, "notify-customer-verified", config),
        LHTaskWorker(notify_customer_not_verified, "notify-customer-not-verified", config),
    )


if __name__ == "__main__":
    asyncio.run(main())
