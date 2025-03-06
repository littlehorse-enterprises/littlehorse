import { Client } from 'nice-grpc';
import { LHConfig } from '../LHConfig';
import { LHErrorType, TaskStatus, VariableType } from '../proto';
import { TaskDefId } from '../proto/object_id';
import {
  LittleHorseDefinition,
  PollTaskRequest,
  ReportTaskRun,
  ScheduledTask
} from '../proto/service';
import { VariableValue } from '../proto/variable';
import { WorkerContext } from './WorkerContext';

export type TaskFunction = (context: WorkerContext, ...args: any[]) => Promise<any>;

/**
 * Class to manage LittleHorse task workers
 */
export class LHTaskWorker {
  private client: Client<LittleHorseDefinition>;
  private taskDefId: TaskDefId;
  private taskFn: TaskFunction;
  private taskWorkerId: string;
  private running = false;
  private pollingIntervals: NodeJS.Timeout[] = [];
  private config: LHConfig;
  
  /**
   * Creates a new LittleHorse task worker
   * @param taskFn - The function that will execute the task
   * @param taskDefName - The name of the task definition
   * @param config - Configuration for the LH client
   */
  constructor(
    taskFn: TaskFunction,
    taskDefName: string,
    config: LHConfig
  ) {
    this.taskFn = taskFn;
    this.taskDefId = { name: taskDefName };
    this.config = config;
    this.client = config.getClient();
    
    // Generate a default worker ID
    this.taskWorkerId = `worker-${Date.now()}-${Math.random().toString(36).substring(2, 10)}`;
  }
  
  /**
   * Register the task definition with the LH server
   */
  async registerTaskDef(): Promise<void> {
    try {
      await this.client.putTaskDef({
        name: this.taskDefId.name,
        inputVars: [
          { 
            name: 'input',
            type: VariableType.STR
          }
        ]
      });
      console.log(`Created TaskDef: ${this.taskDefId.name}`);
    } catch (err) {
      console.error('Failed to register task definition:', err);
      throw err;
    }
  }
  
  /**
   * Start the worker to begin polling for tasks
   * @param threads - Number of polling threads to use (default: 2)
   */
  async start(threads = 2): Promise<void> {
    if (this.running) return;
    
    this.running = true;
    console.log(`Starting task worker for ${this.taskDefId.name} with ID: ${this.taskWorkerId}`);
    
    // Start polling threads using setInterval
    for (let i = 0; i < threads; i++) {
      this.startPollingThread(i);
    }
    
    console.log(`LHTaskWorker started with ${threads} threads`);
  }
  
  /**
   * Start a polling thread using setInterval
   */
  private startPollingThread(threadId: number): void {
    const interval = setInterval(async () => {
      if (!this.running) {
        clearInterval(interval);
        return;
      }
      
      try {
        await this.pollOnce(threadId);
      } catch (error) {
        console.error(`Error polling:`, error);
      }
    }, 5000); // Poll every 5 seconds
    
    this.pollingIntervals.push(interval);
  }
  
  /**
   * Perform a single poll operation
   */
  private async pollOnce(threadId: number): Promise<void> {
    const request: PollTaskRequest = {
      taskDefId: this.taskDefId,
      clientId: `${this.taskWorkerId}-${threadId}`,
      taskWorkerVersion: undefined,
    };
    
    // Create an async generator that yields the poll request
    const pollStream = async function*() {
      yield request;
    };
    
    try {
      for await (const response of this.client.pollTask(pollStream())) {
        if (response.result) {
          await this.processTask(response.result);
        }
      }
    } catch (error) {
      console.error(`Error in poll stream:`, error);
    }
  }
  
  /**
   * Process a received task
   */
  private async processTask(scheduledTask: ScheduledTask): Promise<void> {
    console.log(`Processing task: ${scheduledTask.taskRunId?.taskGuid}`);
    
    try {
      // Create task context
      const context = new WorkerContext(scheduledTask);
      
      // Extract variables
      const inputVariables = scheduledTask.variables.map((v) => {
        return this.extractVariableValue(v.value);
      });
      
      // Execute the task function
      const result = await this.taskFn(context, ...inputVariables);
      
      // Report success
      await this.reportTaskSuccess(scheduledTask, result);
    } catch (err) {
      console.error(`Error executing task:`, err);
      // Report failure
      await this.reportTaskFailure(scheduledTask, err);
    }
  }
  
  /**
   * Extract a JavaScript value from a VariableValue
   */
  private extractVariableValue(value: VariableValue | undefined): any {
    if (!value) return null;
    
    if (value.str !== undefined) return value.str;
    if (value.int !== undefined) return Number(value.int);
    if (value.double !== undefined) return value.double;
    if (value.bool !== undefined) return value.bool;
    if (value.jsonObj !== undefined) {
      try {
        return JSON.parse(value.jsonObj);
      } catch (e) {
        return value.jsonObj;
      }
    }
    
    return null;
  }
  
  /**
   * Report successful task execution
   */
  private async reportTaskSuccess(scheduledTask: ScheduledTask, result: any): Promise<void> {
    try {
      const taskRun: ReportTaskRun = {
        taskRunId: scheduledTask.taskRunId,
        attemptNumber: scheduledTask.attemptNumber,
        status: TaskStatus.TASK_SUCCESS,
        output: this.serializeTaskOutput(result),
        time: new Date().toISOString(),
      };
      
      await this.client.reportTask(taskRun);
      console.log(`Task ${scheduledTask.taskRunId?.taskGuid} completed successfully`);
    } catch (err) {
      console.error('Error reporting task success:', err);
    }
  }
  
  /**
   * Report task execution failure
   */
  private async reportTaskFailure(scheduledTask: ScheduledTask, error: any): Promise<void> {
    try {
      const taskRun: ReportTaskRun = {
        taskRunId: scheduledTask.taskRunId,
        attemptNumber: scheduledTask.attemptNumber,
        status: TaskStatus.TASK_FAILED,
        error: {
          type: LHErrorType.TASK_ERROR,
          message: error.message || String(error),
        },
        time: new Date().toISOString(),
      };
      
      await this.client.reportTask(taskRun);
      console.log(`Task ${scheduledTask.taskRunId?.taskGuid} failed: ${error.message || error}`);
    } catch (err) {
      console.error('Error reporting task failure:', err);
    }
  }
  
  /**
   * Stop the worker and close all connections
   */
  stop(): void {
    this.running = false;
    
    // Clear all polling intervals
    for (const interval of this.pollingIntervals) {
      clearInterval(interval);
    }
    
    this.pollingIntervals = [];
    
    console.log('Worker stopped');
  }
  
  /**
   * Serialize task output to appropriate format
   */
  private serializeTaskOutput(value: any): VariableValue {
    if (value === null || value === undefined) {
      return {};
    }
    
    if (typeof value === 'string') {
      return { str: value };
    }
    
    if (typeof value === 'number') {
      if (Number.isInteger(value)) {
        return { int: value };
      }
      return { double: value };
    }
    
    if (typeof value === 'boolean') {
      return { bool: value };
    }
    
    // For objects, arrays and other complex types
    return { jsonObj: JSON.stringify(value) };
  }
  
  /**
   * Get the health status of this worker
   */
  health() {
    return { 
      healthy: this.running,
      reason: this.running ? 'HEALTHY' : 'UNHEALTHY'
    };
  }
}