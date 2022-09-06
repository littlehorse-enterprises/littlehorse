"""
This module provides a Python Task Executor which gives At-Least-Once guarantees for
task execution.
"""
import argparse
from contextlib import closing
import os
from executor.worker import PythonTaskWorker


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument("--task_def_name")
    parser.add_argument("--num_threads", type=int)
    parser.add_argument("--app_id")
    parser.add_argument("--app_instance_id")
    parser.add_argument("--poll_period", type=float)
    parser.add_argument("--bootstrap_servers")

    args = parser.parse_args()
    arg_dict = vars(args)

    with closing(PythonTaskWorker(**arg_dict)) as worker:
        worker.run()
