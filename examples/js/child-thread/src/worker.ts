import {
  LHTaskWorker,
  Workflow,
  createTaskWorker,
  spawnedThreadsOf,
} from "littlehorse-client";
import { z } from "zod";
import { loadConfig } from "./config";

const config = loadConfig();

function parentTask1(input: number): number {
  console.log("Executing parent-task-1");
  return input * 2;
}

function childTask1(input: number): number {
  console.log("Executing child-task-1");
  return input + 1;
}

function childTask2(): string {
  console.log("Executing child-task-2");
  return "child done";
}

function grandchildTask(input: number): string {
  console.log("Executing grandchild-task");
  return `grandchild received: ${input}`;
}

function parentTask2(): string {
  console.log("Executing parent-task-2");
  return "hello, there!";
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef();
}

async function main() {
  const workers = [
    createTaskWorker(parentTask1, "parent-task-1", config, {
      inputVars: { input: z.number().int() },
    }),
    createTaskWorker(childTask1, "child-task-1", config, {
      inputVars: { input: z.number().int() },
    }),
    createTaskWorker(childTask2, "child-task-2", config, { inputVars: {} }),
    createTaskWorker(grandchildTask, "grandchild-task", config, {
      inputVars: { input: z.number().int() },
    }),
    createTaskWorker(parentTask2, "parent-task-2", config, { inputVars: {} }),
  ];
  for (const w of workers) await ensureTaskDef(w);

  const wf = Workflow.newWorkflow("example-child-thread", (thread) => {
    const parentVar = thread.declareInt("parent-var");

    parentVar.assign(thread.execute("parent-task-1", parentVar));

    // Each spawned thread is a real ThreadRun on the server, running in parallel.
    const childThread = thread.spawnThread(
      (child) => {
        const childVar = child.declareInt("child-var");
        childVar.assign(child.execute("child-task-1", childVar));

        const grandchildThread = child.spawnThread(
          (grandchild) => {
            const grandchildVar = grandchild.declareInt("grandchild-var");
            grandchild.execute("grandchild-task", grandchildVar);
          },
          "spawned-grandchild-thread",
          { "grandchild-var": childVar },
        );

        child.waitForThreads(spawnedThreadsOf(grandchildThread));

        child.execute("child-task-2");
      },
      "spawned-thread",
      { "child-var": parentVar },
    );

    thread.waitForThreads(spawnedThreadsOf(childThread));

    thread.execute("parent-task-2");
  });
  await wf.registerWfSpec(config);

  for (const w of workers) await w.start();
  console.log(
    "ready: polling for parent-task-1, child-task-1, child-task-2, grandchild-task, parent-task-2 tasks",
  );
  console.log("run the workflow:  lhctl run example-child-thread parent-var 2");

  const shutdown = async () => {
    await Promise.all(workers.map((w) => w.close()));
    process.exit(0);
  };
  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
