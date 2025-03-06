import { LHConfig } from '../src/LHConfig';
import { LHTaskWorker } from '../src/worker/LHTaskWorker';
import { WorkerContext } from '../src/worker/WorkerContext';

// Define your task function
// This simple task takes a name and returns a greeting
async function greetingTask(context: WorkerContext, name: string): Promise<string> {
  console.log(`Executing greeting task for name: ${name}`);
  console.log(`Task context: ${JSON.stringify(context)}`);
  return `Hello, ${name}!`;
}

// Create a function to start the worker
async function startWorker() {
  // Create a configuration (uses defaults if nothing specified)
  const config = new LHConfig();
  
  // Create a worker that handles the "greet" task
  const worker = new LHTaskWorker(
    greetingTask,
    'greet',
    config
  );
  
  // Optionally register the task definition if it doesn't exist
  try {
    await worker.registerTaskDef();
    console.log('Task definition registered successfully');
  } catch (error) {
    console.warn('Warning: Could not register task definition', error);
    // Continue anyway, as the task def might already exist
  }
  
  // Start the worker
  console.log('Starting task worker for "greet" task...');
  await worker.start();
  console.log('Worker started successfully!');
  
  // Set up graceful shutdown
  process.on('SIGINT', async () => {
    console.log('Shutting down worker...');
    await worker.stop();
    console.log('Worker shutdown complete');
    process.exit(0);
  });
  
  // Periodically log worker health
  setInterval(() => {
    const health = worker.health();
    if (!health.healthy) {
      console.log(`Worker health: Unhealthy (${health.reason})`);
    }
  }, 10000);
}

// Start the worker and handle any errors
startWorker().catch(error => {
  console.error('Failed to start worker:', error);
  process.exit(1);
}); 