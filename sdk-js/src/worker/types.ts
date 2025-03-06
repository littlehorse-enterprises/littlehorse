export enum TaskWorkerHealthReason {
  HEALTHY = 'HEALTHY',
  UNHEALTHY = 'UNHEALTHY',
  SERVER_REBALANCING = 'SERVER_REBALANCING'
}

export interface TaskWorkerHealth {
  healthy: boolean;
  reason: TaskWorkerHealthReason;
}

export interface TaskWorkerConfig {
  /**
   * Unique identifier for this task worker instance
   */
  taskWorkerId: string;
  
  /**
   * Optional version information for the task worker
   */
  workerVersion?: string;
  
  /**
   * Maximum number of tasks that can be executed concurrently
   * @default 10
   */
  maxInflightTasks?: number;
  
  /**
   * Number of worker threads to use per host
   * @default 1
   */
  workerThreads?: number;
}
