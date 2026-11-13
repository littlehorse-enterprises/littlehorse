use littlehorse::{
    ClientConfig,
    proto::{
        little_horse_server::{LittleHorse, LittleHorseServer},
        *,
    },
    worker::{TaskFailure, Worker, WorkerError},
};
use std::sync::{
    Arc,
    atomic::{AtomicBool, AtomicUsize, Ordering},
};
use tokio::sync::{mpsc, watch};
use tokio_stream::wrappers::{ReceiverStream, TcpListenerStream};
use tonic::{Request, Response, Status};
type Stream =
    std::pin::Pin<Box<dyn tokio_stream::Stream<Item = Result<PollTaskResponse, Status>> + Send>>;
struct Fixture {
    reports: mpsc::Sender<ReportTaskRun>,
    retries: Arc<AtomicUsize>,
    fence: bool,
    host: LhHostInfo,
    assigned: Arc<AtomicBool>,
}
#[tonic::async_trait]
impl LittleHorse for Fixture {
    async fn register_task_worker(
        &self,
        _: Request<RegisterTaskWorkerRequest>,
    ) -> Result<Response<RegisterTaskWorkerResponse>, Status> {
        Ok(Response::new(RegisterTaskWorkerResponse {
            your_hosts: if self.assigned.load(Ordering::SeqCst) {
                vec![self.host.clone()]
            } else {
                vec![]
            },
            is_cluster_healthy: Some(true),
        }))
    }
    async fn poll_task(
        &self,
        request: Request<tonic::Streaming<PollTaskRequest>>,
    ) -> Result<Response<Stream>, Status> {
        let mut requests = request.into_inner();
        let (sender, receiver) = mpsc::channel(1);
        tokio::spawn(async move {
            if requests.message().await.ok().flatten().is_some() {
                let _ = sender
                    .send(Ok(PollTaskResponse {
                        result: Some(ScheduledTask::default()),
                    }))
                    .await;
            }
            while requests.message().await.ok().flatten().is_some() {}
        });
        Ok(Response::new(Box::pin(ReceiverStream::new(receiver))))
    }
    #[allow(deprecated)] // fetch_update supports the declared MSRV; try_update is newer.
    async fn report_task(&self, request: Request<ReportTaskRun>) -> Result<Response<()>, Status> {
        if self
            .retries
            .fetch_update(Ordering::SeqCst, Ordering::SeqCst, |remaining| {
                remaining.checked_sub(1)
            })
            .is_ok()
        {
            return Err(Status::unavailable("retry report"));
        }
        self.reports.send(request.into_inner()).await.unwrap();
        Ok(Response::new(()))
    }
    async fn put_checkpoint(
        &self,
        _: Request<PutCheckpointRequest>,
    ) -> Result<Response<PutCheckpointResponse>, Status> {
        Ok(Response::new(PutCheckpointResponse {
            flow_control_continue_type: i32::from(self.fence),
            created_checkpoint: None,
        }))
    }
}
struct Harness {
    config: ClientConfig,
    reports: mpsc::Receiver<ReportTaskRun>,
    assigned: Arc<AtomicBool>,
    server: tokio::task::JoinHandle<Result<(), tonic::transport::Error>>,
}
impl Drop for Harness {
    fn drop(&mut self) {
        self.server.abort();
    }
}
async fn start(retries: usize, fence: bool) -> Harness {
    let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
    let address = listener.local_addr().unwrap();
    let (reports, received) = mpsc::channel(10);
    let assigned = Arc::new(AtomicBool::new(true));
    let fixture = Fixture {
        reports,
        retries: Arc::new(AtomicUsize::new(retries)),
        fence,
        host: LhHostInfo {
            host: "127.0.0.1".into(),
            port: i32::from(address.port()),
        },
        assigned: assigned.clone(),
    };
    let server = tokio::spawn(
        tonic::transport::Server::builder()
            .add_service(LittleHorseServer::new(fixture))
            .serve_with_incoming(TcpListenerStream::new(listener)),
    );
    Harness {
        config: ClientConfig {
            endpoint: format!("http://{address}"),
            ..Default::default()
        },
        reports: received,
        assigned,
        server,
    }
}
#[tokio::test]
async fn transient_report_failures_do_not_reexecute_user_code() {
    let mut fixture = start(2, false).await;
    let calls = Arc::new(AtomicUsize::new(0));
    let worker = Worker::new("retry-report", {
        let calls = calls.clone();
        move |context| {
            calls.fetch_add(1, Ordering::SeqCst);
            async move {
                context.log("worked");
                Ok(VariableValue::default())
            }
        }
    });
    let client = fixture.config.clone().connect().await.unwrap();
    let (shutdown, receiver) = watch::channel(false);
    let task = tokio::spawn(async move { worker.run(client, receiver).await });
    let report = tokio::time::timeout(std::time::Duration::from_secs(5), fixture.reports.recv())
        .await
        .unwrap()
        .unwrap();
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    assert_eq!(report.status, TaskStatus::TaskSuccess as i32);
    assert_eq!(
        littlehorse::from_value::<String>(report.log_output.as_ref().unwrap()).unwrap(),
        "worked"
    );
    shutdown.send(true).unwrap();
    task.await.unwrap().unwrap();
}
#[tokio::test]
async fn business_exceptions_and_panics_have_distinct_results() {
    for panic in [false, true] {
        let mut fixture = start(0, false).await;
        let worker = Worker::new("failures", move |_| async move {
            if panic {
                panic!("handler panic");
            }
            Err(TaskFailure::Exception(Box::new(LhTaskException {
                name: "declined".into(),
                message: "card declined".into(),
                content: None,
            })))
        });
        let client = fixture.config.clone().connect().await.unwrap();
        let (shutdown, receiver) = watch::channel(false);
        let task = tokio::spawn(async move { worker.run(client, receiver).await });
        let report =
            tokio::time::timeout(std::time::Duration::from_secs(5), fixture.reports.recv())
                .await
                .unwrap()
                .unwrap();
        assert_eq!(
            report.status,
            if panic {
                TaskStatus::TaskFailed
            } else {
                TaskStatus::TaskException
            } as i32
        );
        shutdown.send(true).unwrap();
        task.await.unwrap().unwrap();
    }
}
#[tokio::test]
async fn fencing_cancels_execution_without_reporting_a_stale_attempt() {
    let mut fixture = start(0, true).await;
    let (started, mut reached) = mpsc::channel(1);
    let worker = Worker::new("fenced", move |mut context| {
        let started = started.clone();
        async move {
            let result = context
                .checkpoint(|| async { Ok(VariableValue::default()) })
                .await;
            started.send(()).await.unwrap();
            // Even if user code catches the fencing error, the runtime must discard this result.
            assert!(result.is_err());
            Ok(VariableValue::default())
        }
    });
    let client = fixture.config.clone().connect().await.unwrap();
    let (shutdown, receiver) = watch::channel(false);
    let task = tokio::spawn(async move { worker.run(client, receiver).await });
    tokio::time::timeout(std::time::Duration::from_secs(5), reached.recv())
        .await
        .unwrap();
    shutdown.send(true).unwrap();
    task.await.unwrap().unwrap();
    assert!(fixture.reports.try_recv().is_err());
}
#[tokio::test]
async fn drain_deadline_cancels_handlers_and_returns_an_error() {
    let fixture = start(0, false).await;
    let (started, mut received) = mpsc::channel(1);
    let mut worker = Worker::new("drain", move |_| {
        let started = started.clone();
        async move {
            started.send(()).await.unwrap();
            std::future::pending().await
        }
    });
    worker.drain_timeout = std::time::Duration::from_millis(20);
    let client = fixture.config.clone().connect().await.unwrap();
    let (shutdown, receiver) = watch::channel(false);
    let task = tokio::spawn(async move { worker.run(client, receiver).await });
    tokio::time::timeout(std::time::Duration::from_secs(5), received.recv())
        .await
        .unwrap();
    shutdown.send(true).unwrap();
    assert!(matches!(
        task.await.unwrap(),
        Err(WorkerError::DrainTimeout)
    ));
}
#[tokio::test]
async fn cluster_registration_tracks_assignment_removal() {
    let mut fixture = start(0, false).await;
    let mut worker = Worker::new("cluster", |_| async { Ok(VariableValue::default()) });
    worker.heartbeat_interval = std::time::Duration::from_millis(20);
    let mut health = worker.health();
    let config = fixture.config.clone();
    let (shutdown, receiver) = watch::channel(false);
    let task = tokio::spawn(async move { worker.run_cluster(config, receiver).await });
    tokio::time::timeout(std::time::Duration::from_secs(5), fixture.reports.recv())
        .await
        .unwrap()
        .unwrap();
    assert!(health.borrow().registered);
    fixture.assigned.store(false, Ordering::SeqCst);
    tokio::time::timeout(std::time::Duration::from_secs(5), async {
        loop {
            if health.borrow().assigned_hosts == 0 && health.borrow().connected_hosts == 0 {
                break;
            }
            health.changed().await.unwrap();
        }
    })
    .await
    .unwrap();
    shutdown.send(true).unwrap();
    task.await.unwrap().unwrap();
}
