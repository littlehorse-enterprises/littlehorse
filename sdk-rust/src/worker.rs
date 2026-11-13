//! Task worker with bounded execution, streaming polling, and graceful draining.
use crate::{Client, proto::*};
use std::{
    future::Future,
    pin::Pin,
    sync::{
        Arc,
        atomic::{AtomicI32, Ordering},
    },
    time::{Duration, SystemTime},
};
use tokio::{
    sync::{mpsc, watch},
    task::JoinSet,
};
use tokio_stream::wrappers::ReceiverStream;

pub type TaskFuture = Pin<Box<dyn Future<Output = Result<VariableValue, TaskFailure>> + Send>>;
pub type TaskHandler = Arc<dyn Fn(TaskContext) -> TaskFuture + Send + Sync>;
#[derive(Debug)]
pub enum TaskFailure {
    Error(String),
    Exception(Box<LhTaskException>),
    Input(String),
    Output(String),
}
#[derive(Debug, thiserror::Error)]
pub enum WorkerError {
    #[error(transparent)]
    Rpc(#[from] tonic::Status),
    #[error("worker concurrency must be positive")]
    InvalidConcurrency,
    #[error("worker drain deadline expired")]
    DrainTimeout,
    #[error("connection failed: {0}")]
    Connection(String),
    #[error("worker task failed: {0}")]
    Join(#[from] tokio::task::JoinError),
}
#[derive(Clone)]
pub struct Worker {
    pub task_name: String,
    pub worker_id: String,
    pub concurrency: usize,
    pub version: Option<String>,
    pub drain_timeout: Duration,
    pub heartbeat_interval: Duration,
    pub reconnect_delay: Duration,
    health: watch::Sender<WorkerHealth>,
    handler: TaskHandler,
}
impl Worker {
    pub fn new<F, Fut>(task_name: impl Into<String>, handler: F) -> Self
    where
        F: Fn(TaskContext) -> Fut + Send + Sync + 'static,
        Fut: Future<Output = Result<VariableValue, TaskFailure>> + Send + 'static,
    {
        Self {
            task_name: task_name.into(),
            worker_id: uuid::Uuid::new_v4().to_string(),
            concurrency: 8,
            version: None,
            drain_timeout: Duration::from_secs(30),
            heartbeat_interval: Duration::from_secs(10),
            reconnect_delay: Duration::from_secs(1),
            health: watch::channel(WorkerHealth::default()).0,
            handler: Arc::new(move |task| Box::pin(handler(task))),
        }
    }
    /// Poll a server connection until shutdown becomes true, then finish and report in-flight tasks.
    /// Use one instance per server advertised by RegisterTaskWorker in clustered deployments.
    pub async fn run(
        &self,
        client: Client,
        shutdown: watch::Receiver<bool>,
    ) -> Result<(), WorkerError> {
        self.run_with_limit(
            client,
            shutdown,
            Arc::new(tokio::sync::Semaphore::new(self.concurrency)),
        )
        .await
    }
    async fn run_with_limit(
        &self,
        mut client: Client,
        mut shutdown: watch::Receiver<bool>,
        limit: Arc<tokio::sync::Semaphore>,
    ) -> Result<(), WorkerError> {
        if self.concurrency == 0 {
            return Err(WorkerError::InvalidConcurrency);
        }
        let (requests, receiver) = mpsc::channel(self.concurrency);
        let poll = PollTaskRequest {
            task_def_id: Some(TaskDefId {
                name: self.task_name.clone(),
            }),
            client_id: self.worker_id.clone(),
            task_worker_version: self.version.clone(),
        };
        for _ in 0..self.concurrency {
            requests
                .try_send(poll.clone())
                .expect("new channel has concurrency capacity");
        }
        if *shutdown.borrow() {
            return Ok(());
        }
        let mut responses = tokio::select! {
            result = client.poll_task(ReceiverStream::new(receiver)) => result?.into_inner(),
            _ = shutdown.changed() => return Ok(()),
        };
        let mut executions = JoinSet::new();
        let mut outstanding = self.concurrency;
        let mut terminal_error = None;
        loop {
            if *shutdown.borrow() {
                break;
            }
            while outstanding + executions.len() < self.concurrency {
                if requests.send(poll.clone()).await.is_err() {
                    break;
                }
                outstanding += 1;
            }
            tokio::select! {
                biased;
                changed = shutdown.changed() => {
                    if changed.is_err() || *shutdown.borrow() { break; }
                }
                completed = executions.join_next(), if !executions.is_empty() => {
                    match completed.unwrap() {
                        Ok(Ok(())) => {},
                        Ok(Err(error)) => { terminal_error = Some(error); break; },
                        Err(error) => { terminal_error = Some(error.into()); break; },
                    }
                }
                response = responses.message() => {
                    match response {
                        Err(error) => { terminal_error = Some(error.into()); break; },
                        Ok(None) => break,
                        Ok(Some(response)) => {
                            outstanding = outstanding.saturating_sub(1);
                            if let Some(task) = response.result {
                                let client = client.clone(); let handler = self.handler.clone(); let limit = limit.clone();
                                executions.spawn(async move {
                                    let _permit = limit.acquire_owned().await.expect("execution semaphore remains open");
                                    execute(client, handler, task).await
                                });
                            }
                        }
                    }
                }
            }
        }
        drop(requests);
        let drained = tokio::time::timeout(self.drain_timeout, async {
            while let Some(completed) = executions.join_next().await {
                let result = completed
                    .map_err(WorkerError::from)
                    .and_then(|result| result);
                if let Err(error) = result {
                    terminal_error.get_or_insert(error);
                }
            }
        })
        .await;
        if drained.is_err() {
            executions.abort_all();
            while executions.join_next().await.is_some() {}
            return Err(WorkerError::DrainTimeout);
        }
        match terminal_error {
            Some(error) => Err(error),
            None => Ok(()),
        }
    }
}
async fn execute(
    mut client: Client,
    handler: TaskHandler,
    task: ScheduledTask,
) -> Result<(), WorkerError> {
    let task_run_id = task.task_run_id.clone();
    let attempt_number = task.attempt_number;
    let total_checkpoints = Arc::new(AtomicI32::new(task.total_observed_checkpoints));
    let logs = Arc::new(std::sync::Mutex::new(String::new()));
    let (fenced, mut cancellation) = watch::channel(false);
    let context = TaskContext {
        task,
        client: client.clone(),
        logs: logs.clone(),
        fenced,
        next_checkpoint: 0,
        total_checkpoints: total_checkpoints.clone(),
    };
    // Aborting an execution (including a drain timeout) also cancels its handler.
    let mut handler_task = tokio::spawn(async move { handler(context).await });
    let _abort = AbortHandler(handler_task.abort_handle());
    let result = tokio::select! {
        biased;
        _ = cancellation.changed() => {
            if *cancellation.borrow() { return Ok(()); }
            match handler_task.await { Ok(result) => result, Err(error) => Err(TaskFailure::Error(format!("handler panicked: {error}"))) }
        }
        result = &mut handler_task => {
            if *cancellation.borrow() { return Ok(()); }
            match result { Ok(result) => result, Err(error) => Err(TaskFailure::Error(format!("handler panicked: {error}"))) }
        }
    };
    let (status, result) = match result {
        Ok(output) => (
            TaskStatus::TaskSuccess,
            report_task_run::Result::Output(output),
        ),
        Err(TaskFailure::Error(message)) => (
            TaskStatus::TaskFailed,
            report_task_run::Result::Error(LhTaskError {
                r#type: LhErrorType::TaskFailure as i32,
                message,
            }),
        ),
        Err(TaskFailure::Input(message)) => (
            TaskStatus::TaskInputVarSubError,
            report_task_run::Result::Error(LhTaskError {
                r#type: LhErrorType::VarSubError as i32,
                message,
            }),
        ),
        Err(TaskFailure::Output(message)) => (
            TaskStatus::TaskOutputSerdeError,
            report_task_run::Result::Error(LhTaskError {
                r#type: LhErrorType::TaskFailure as i32,
                message,
            }),
        ),
        Err(TaskFailure::Exception(exception)) => (
            TaskStatus::TaskException,
            report_task_run::Result::Exception(*exception),
        ),
    };
    let report = ReportTaskRun {
        task_run_id,
        attempt_number,
        total_checkpoints: total_checkpoints.load(Ordering::SeqCst),
        log_output: Some(
            crate::to_value(&*logs.lock().unwrap()).expect("string log is serializable"),
        ),
        time: Some(SystemTime::now().into()),
        status: status as i32,
        result: Some(result),
    };
    // Retrying the same attempt report is safe; never rerun user code on a reporting error.
    for retry in 0..3 {
        match client.report_task(report.clone()).await {
            Ok(_) => return Ok(()),
            Err(error)
                if retry < 2
                    && matches!(
                        error.code(),
                        tonic::Code::Unavailable | tonic::Code::DeadlineExceeded
                    ) =>
            {
                tokio::time::sleep(Duration::from_millis(100 * (1 << retry))).await
            }
            Err(error) => return Err(error.into()),
        }
    }
    unreachable!()
}

/// Per-attempt context. Checkpoint calls must occur in the same order on every retry.
pub struct TaskContext {
    pub task: ScheduledTask,
    client: Client,
    logs: Arc<std::sync::Mutex<String>>,
    fenced: watch::Sender<bool>,
    next_checkpoint: i32,
    total_checkpoints: Arc<AtomicI32>,
}
impl TaskContext {
    /// Replay a persisted result, or execute and persist the next durable step.
    /// Side effects must be idempotent: a crash between the side effect and persistence
    /// may cause the step to execute again.
    pub async fn checkpoint<F, Fut>(&mut self, operation: F) -> Result<VariableValue, TaskFailure>
    where
        F: FnOnce() -> Fut,
        Fut: Future<Output = Result<VariableValue, TaskFailure>>,
    {
        let checkpoint_number = self.next_checkpoint;
        let mut client = self.client.clone();
        let value = if checkpoint_number < self.task.total_observed_checkpoints {
            client
                .get_checkpoint(CheckpointId {
                    task_run: self.task.task_run_id.clone(),
                    checkpoint_number,
                })
                .await
                .map_err(|error| TaskFailure::Error(error.to_string()))?
                .into_inner()
                .value
                .ok_or_else(|| TaskFailure::Error("checkpoint response has no value".into()))?
        } else {
            let value = operation().await?;
            let response = client
                .put_checkpoint(PutCheckpointRequest {
                    task_run_id: self.task.task_run_id.clone(),
                    task_attempt: self.task.attempt_number,
                    value: Some(value.clone()),
                    logs: None,
                })
                .await
                .map_err(|error| TaskFailure::Error(error.to_string()))?
                .into_inner();
            if response.flow_control_continue_type != 0 {
                self.fenced.send_replace(true);
                return Err(TaskFailure::Error("server fenced this task attempt".into()));
            }
            self.total_checkpoints.fetch_add(1, Ordering::SeqCst);
            value
        };
        self.next_checkpoint += 1;
        Ok(value)
    }
}

struct AbortHandler(tokio::task::AbortHandle);
impl Drop for AbortHandler {
    fn drop(&mut self) {
        self.0.abort();
    }
}
#[derive(Clone, Debug, Default)]
pub struct WorkerHealth {
    pub registered: bool,
    pub cluster_healthy: bool,
    pub assigned_hosts: usize,
    pub connected_hosts: usize,
    pub last_error: Option<String>,
}
impl WorkerHealth {
    pub fn ready(&self) -> bool {
        self.registered
            && self.cluster_healthy
            && self.assigned_hosts > 0
            && self.connected_hosts == self.assigned_hosts
    }
}
impl Worker {
    pub fn health(&self) -> watch::Receiver<WorkerHealth> {
        self.health.subscribe()
    }
    /// Register a typed one-argument task. Use a tuple/struct adapter for multiple fields,
    /// or `new` plus `TaskContext::input` for positional multi-argument handlers.
    pub fn typed<I, O, F, Fut>(task_name: impl Into<String>, handler: F) -> Self
    where
        I: crate::types::LittleHorseValue + Send + 'static,
        O: crate::types::LittleHorseValue + Send + 'static,
        F: Fn(I, TaskContext) -> Fut + Send + Sync + 'static,
        Fut: Future<Output = Result<O, TaskFailure>> + Send + 'static,
    {
        let handler = Arc::new(handler);
        Self::new(task_name, move |context| {
            let input = context.input::<I>(0);
            let handler = handler.clone();
            async move {
                let output = handler(input?, context).await?;
                output
                    .to_lh()
                    .map_err(|error| TaskFailure::Output(error.to_string()))
            }
        })
    }
    pub async fn register(
        &self,
        client: &mut Client,
        inputs: Vec<VariableDef>,
        output: Option<TypeDefinition>,
    ) -> Result<TaskDef, tonic::Status> {
        client
            .put_task_def(PutTaskDefRequest {
                name: self.task_name.clone(),
                input_vars: inputs,
                return_type: Some(ReturnType {
                    return_type: output,
                }),
                description: None,
            })
            .await
            .map(|response| response.into_inner())
    }
    pub async fn register_typed<
        I: crate::types::LittleHorseValue,
        O: crate::types::LittleHorseValue,
    >(
        &self,
        client: &mut Client,
        input_name: &str,
    ) -> Result<TaskDef, tonic::Status> {
        let mut registered = std::collections::HashSet::new();
        for definition in I::registrations().into_iter().chain(O::registrations()) {
            if registered.insert(definition.name.clone()) {
                client.put_struct_def(definition).await?;
            }
        }
        self.register(
            client,
            vec![VariableDef {
                name: input_name.into(),
                type_def: Some(I::type_definition()),
                ..Default::default()
            }],
            if std::any::type_name::<O>() == "()" {
                None
            } else {
                Some(O::type_definition())
            },
        )
        .await
    }
    /// Discover assignments periodically and reconnect failed polling streams. All hosts
    /// share one execution semaphore, so concurrency remains bounded across a rebalance.
    pub async fn run_cluster(
        &self,
        config: crate::ClientConfig,
        mut shutdown: watch::Receiver<bool>,
    ) -> Result<(), WorkerError> {
        if self.concurrency == 0 || config.inflight_tasks == 0 {
            return Err(WorkerError::InvalidConcurrency);
        }
        let mut bootstrap = None;
        let mut connections: std::collections::HashMap<
            String,
            (watch::Sender<bool>, tokio::task::JoinHandle<()>),
        > = std::collections::HashMap::new();
        let mut retired = Vec::new();
        let mut task_guard = AbortWorkers(Vec::new());
        // The explicit worker limit and the configured in-flight ceiling both apply.
        let limit = Arc::new(tokio::sync::Semaphore::new(
            self.concurrency.min(config.inflight_tasks),
        ));
        loop {
            if *shutdown.borrow() {
                break;
            }
            if bootstrap.is_none() {
                let connection = tokio::select! { result = config.clone().connect() => Some(result), _ = shutdown.changed() => None };
                let Some(connection) = connection else {
                    break;
                };
                match connection {
                    Ok(client) => bootstrap = Some(client),
                    Err(error) => {
                        self.health.send_modify(|health| {
                            health.registered = false;
                            health.last_error = Some(error.to_string());
                        });
                    }
                }
            }
            let registration = if let Some(client) = &mut bootstrap {
                tokio::select! {
                    response = client.register_task_worker(RegisterTaskWorkerRequest { task_worker_id: self.worker_id.clone(), task_def_id: Some(TaskDefId { name: self.task_name.clone() }) }) => Some(response),
                    _ = shutdown.changed() => break,
                }
            } else {
                None
            };
            match registration {
                Some(Ok(response)) => {
                    let response = response.into_inner();
                    let mut assigned = std::collections::HashSet::new();
                    for host in response.your_hosts {
                        let host_config = config
                            .for_host(&host)
                            .map_err(|error| WorkerError::Connection(error.to_string()))?;
                        let endpoint = host_config.endpoint.clone();
                        assigned.insert(endpoint.clone());
                        if let Some((_, task)) = connections.get(&endpoint)
                            && task.is_finished()
                        {
                            let (_, task) = connections.remove(&endpoint).unwrap();
                            let _ = task.await;
                        }
                        if let std::collections::hash_map::Entry::Vacant(entry) =
                            connections.entry(endpoint)
                        {
                            let (stop, mut receiver) = watch::channel(false);
                            let worker = self.clone();
                            let limit = limit.clone();
                            let task = tokio::spawn(async move {
                                loop {
                                    if *receiver.borrow() {
                                        break;
                                    }
                                    match host_config.clone().connect().await {
                                        Ok(client) => {
                                            worker
                                                .health
                                                .send_modify(|health| health.connected_hosts += 1);
                                            let result = worker
                                                .run_with_limit(
                                                    client,
                                                    receiver.clone(),
                                                    limit.clone(),
                                                )
                                                .await;
                                            worker.health.send_modify(|health| {
                                                health.connected_hosts =
                                                    health.connected_hosts.saturating_sub(1);
                                                if let Err(error) = &result {
                                                    health.last_error = Some(error.to_string());
                                                }
                                            });
                                        }
                                        Err(error) => {
                                            worker.health.send_modify(|health| {
                                                health.last_error = Some(error.to_string())
                                            });
                                        }
                                    }
                                    tokio::select! { _ = receiver.changed() => {}, _ = tokio::time::sleep(worker.reconnect_delay) => {} }
                                }
                            });
                            task_guard.0.push(task.abort_handle());
                            entry.insert((stop, task));
                        }
                    }
                    let removed: Vec<_> = connections
                        .keys()
                        .filter(|host| !assigned.contains(*host))
                        .cloned()
                        .collect();
                    for host in removed {
                        let (stop, task) = connections.remove(&host).unwrap();
                        stop.send_replace(true);
                        retired.push(task);
                    }
                    self.health.send_modify(|health| {
                        health.registered = true;
                        health.cluster_healthy = response.is_cluster_healthy.unwrap_or(false);
                        health.assigned_hosts = assigned.len();
                    });
                }
                Some(Err(error)) => {
                    self.health.send_modify(|health| {
                        health.registered = false;
                        health.last_error = Some(error.to_string());
                    });
                    bootstrap = None;
                }
                None => {}
            }
            retired.retain(|task| !task.is_finished());
            task_guard.0.retain(|task| !task.is_finished());
            tokio::select! { _ = shutdown.changed() => { if *shutdown.borrow() || shutdown.has_changed().is_err() { break; } }, _ = tokio::time::sleep(if bootstrap.is_some() { self.heartbeat_interval } else { self.reconnect_delay }) => {} }
        }
        for (_, (stop, task)) in connections {
            stop.send_replace(true);
            retired.push(task);
        }
        for task in retired {
            task.await?;
        }
        self.health.send_modify(|health| {
            health.registered = false;
            health.connected_hosts = 0;
        });
        Ok(())
    }
}
impl TaskContext {
    pub fn input<T: crate::types::LittleHorseValue>(&self, index: usize) -> Result<T, TaskFailure> {
        let value = self
            .task
            .variables
            .get(index)
            .and_then(|input| input.value.as_ref())
            .ok_or_else(|| TaskFailure::Input(format!("missing input {index}")))?;
        T::from_lh(value).map_err(|error| TaskFailure::Input(error.to_string()))
    }
    pub fn log(&self, message: impl std::fmt::Display) {
        use std::fmt::Write;
        let _ = write!(self.logs.lock().unwrap(), "{message}");
    }
    pub fn node_run_id(&self) -> Option<&NodeRunId> {
        match self.task.source.as_ref()?.task_run_source.as_ref()? {
            task_run_source::TaskRunSource::TaskNode(source) => source.node_run_id.as_ref(),
            task_run_source::TaskRunSource::UserTaskTrigger(source) => source.node_run_id.as_ref(),
        }
    }
    pub fn wf_run_id(&self) -> Option<&WfRunId> {
        self.task.task_run_id.as_ref()?.wf_run_id.as_ref()
    }
    pub fn idempotency_key(&self) -> Option<String> {
        let task = self.task.task_run_id.as_ref()?;
        Some(format!(
            "{}/{}",
            crate::ids::wf_run_id_string(task.wf_run_id.as_ref()?),
            task.task_guid
        ))
    }
    pub fn user_id(&self) -> Option<&str> {
        match self.task.source.as_ref()?.task_run_source.as_ref()? {
            task_run_source::TaskRunSource::UserTaskTrigger(source) => source.user_id.as_deref(),
            _ => None,
        }
    }
    pub fn user_group(&self) -> Option<&str> {
        match self.task.source.as_ref()?.task_run_source.as_ref()? {
            task_run_source::TaskRunSource::UserTaskTrigger(source) => source.user_group.as_deref(),
            _ => None,
        }
    }
    pub async fn checkpoint_typed<T, F, Fut>(&mut self, operation: F) -> Result<T, TaskFailure>
    where
        T: crate::types::LittleHorseValue,
        F: FnOnce() -> Fut,
        Fut: Future<Output = Result<T, TaskFailure>>,
    {
        let value = self
            .checkpoint(|| async {
                operation()
                    .await?
                    .to_lh()
                    .map_err(|error| TaskFailure::Output(error.to_string()))
            })
            .await?;
        T::from_lh(&value).map_err(|error| TaskFailure::Output(error.to_string()))
    }
}

struct AbortWorkers(Vec<tokio::task::AbortHandle>);
impl Drop for AbortWorkers {
    fn drop(&mut self) {
        for task in &self.0 {
            task.abort();
        }
    }
}
