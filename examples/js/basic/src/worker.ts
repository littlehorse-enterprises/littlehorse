import { LHTaskWorker, Workflow, createTaskWorker } from "littlehorse-client";
import { z } from "zod";
import { loadConfig } from "./config";

const config = loadConfig();

function greeting(name: string): string {
  console.log("Executing task greet");
  return `hello there, ${name}`;
}

async function ensureTaskDef(worker: LHTaskWorker) {
  if (!(await worker.doesTaskDefExist())) await worker.registerTaskDef();
}

async function main() {
  const greeter = createTaskWorker(greeting, "greet", config, {
    inputVars: { name: z.string() },
    description: "This task greets the user by name.",
  });
  await ensureTaskDef(greeter);

  // The wfsdk compiles this closure into a PutWfSpecRequest. It never runs the
  // workflow itself — the server does that.
  const wf = Workflow.newWorkflow("example-basic", (thread) => {
    const theName = thread.declareStr("input-name").searchable();
    thread.execute("greet", theName);
  });
  await wf.registerWfSpec(config);

  await greeter.start();
  console.log("ready: polling for greet tasks");
  console.log("run the workflow:  lhctl run example-basic input-name Obi-Wan");

  const shutdown = async () => {
    await greeter.close();
    process.exit(0);
  };
  process.on("SIGINT", shutdown);
  process.on("SIGTERM", shutdown);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
