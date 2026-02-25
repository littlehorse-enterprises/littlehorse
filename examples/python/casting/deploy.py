from littlehorse.config import LHConfig
import littlehorse
from examples.python.casting.workflow import get_workflow


def main() -> None:
    cfg = LHConfig()
    wf = get_workflow()

    littlehorse.create_workflow_spec(wf, cfg)


if __name__ == "__main__":
    main()
