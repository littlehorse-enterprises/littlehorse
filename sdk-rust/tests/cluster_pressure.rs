//! Real streaming transports exercise multiple assigned hosts and rebalances under load.
use littlehorse::{
    ClientConfig,
    proto::{
        little_horse_server::{LittleHorse, LittleHorseServer},
        *,
    },
    worker::{Worker, WorkerError},
};
use std::{
    collections::BTreeSet,
    sync::{
        Arc, Mutex,
        atomic::{AtomicUsize, Ordering},
    },
    time::Duration,
};
use tokio::sync::{Semaphore, mpsc, watch};
use tokio_stream::wrappers::{ReceiverStream, TcpListenerStream};
use tonic::{Request, Response, Status};
type Stream =
    std::pin::Pin<Box<dyn tokio_stream::Stream<Item = Result<PollTaskResponse, Status>> + Send>>;
struct State {
    hosts: Mutex<Vec<LhHostInfo>>,
    polled: mpsc::Sender<u16>,
    reports: mpsc::Sender<ReportTaskRun>,
}
struct Host {
    state: Arc<State>,
    port: u16,
    remaining: Arc<AtomicUsize>,
    failures: Arc<AtomicUsize>,
}
fn decrement(counter: &AtomicUsize) -> bool {
    let mut value = counter.load(Ordering::SeqCst);
    while value > 0 {
        match counter.compare_exchange_weak(value, value - 1, Ordering::SeqCst, Ordering::SeqCst) {
            Ok(_) => return true,
            Err(actual) => value = actual,
        }
    }
    false
}
#[tonic::async_trait]
impl LittleHorse for Host {
    async fn register_task_worker(
        &self,
        _: Request<RegisterTaskWorkerRequest>,
    ) -> Result<Response<RegisterTaskWorkerResponse>, Status> {
        Ok(Response::new(RegisterTaskWorkerResponse {
            your_hosts: self.state.hosts.lock().unwrap().clone(),
            is_cluster_healthy: Some(true),
        }))
    }
    async fn poll_task(
        &self,
        request: Request<tonic::Streaming<PollTaskRequest>>,
    ) -> Result<Response<Stream>, Status> {
        self.state.polled.send(self.port).await.unwrap();
        if decrement(&self.failures) {
            return Err(Status::unavailable("injected dropped polling connection"));
        }
        let (sender, receiver) = mpsc::channel(1);
        let mut input = request.into_inner();
        let remaining = self.remaining.clone();
        let port = self.port;
        tokio::spawn(async move {
            let mut index = 0;
            while input.message().await.ok().flatten().is_some() {
                if decrement(&remaining) {
                    let task = ScheduledTask {
                        task_run_id: Some(TaskRunId {
                            wf_run_id: None,
                            task_guid: format!("{port}-{index}"),
                        }),
                        ..Default::default()
                    };
                    index += 1;
                    if sender
                        .send(Ok(PollTaskResponse { result: Some(task) }))
                        .await
                        .is_err()
                    {
                        break;
                    }
                }
            }
        });
        Ok(Response::new(Box::pin(ReceiverStream::new(receiver))))
    }
    async fn report_task(&self, request: Request<ReportTaskRun>) -> Result<Response<()>, Status> {
        self.state.reports.send(request.into_inner()).await.unwrap();
        Ok(Response::new(()))
    }
}
struct Cluster {
    state: Arc<State>,
    config: ClientConfig,
    hosts: Vec<LhHostInfo>,
    polled: mpsc::Receiver<u16>,
    reports: mpsc::Receiver<ReportTaskRun>,
    servers: Vec<tokio::task::JoinHandle<Result<(), tonic::transport::Error>>>,
}
impl Drop for Cluster {
    fn drop(&mut self) {
        for server in &self.servers {
            server.abort();
        }
    }
}
async fn cluster(jobs_per_host: usize, failures: usize) -> Cluster {
    let (polled, received) = mpsc::channel(64);
    let (reports, completed) = mpsc::channel(64);
    let state = Arc::new(State {
        hosts: Mutex::new(vec![]),
        polled,
        reports,
    });
    let mut hosts = vec![];
    let mut servers = vec![];
    for _ in 0..2 {
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let port = listener.local_addr().unwrap().port();
        hosts.push(LhHostInfo {
            host: "127.0.0.1".into(),
            port: i32::from(port),
        });
        let service = Host {
            state: state.clone(),
            port,
            remaining: Arc::new(AtomicUsize::new(jobs_per_host)),
            failures: Arc::new(AtomicUsize::new(failures)),
        };
        servers.push(tokio::spawn(
            tonic::transport::Server::builder()
                .add_service(LittleHorseServer::new(service))
                .serve_with_incoming(TcpListenerStream::new(listener)),
        ));
    }
    *state.hosts.lock().unwrap() = hosts.clone();
    Cluster {
        state,
        config: ClientConfig {
            endpoint: format!("http://127.0.0.1:{}", hosts[0].port),
            inflight_tasks: 1,
            ..Default::default()
        },
        hosts,
        polled: received,
        reports: completed,
        servers,
    }
}
fn bounded_worker(
    gate: Arc<Semaphore>,
    active: Arc<AtomicUsize>,
    peak: Arc<AtomicUsize>,
    started: mpsc::Sender<()>,
) -> Worker {
    let mut worker = Worker::new("pressure", move |_| {
        let gate = gate.clone();
        let active = active.clone();
        let peak = peak.clone();
        let started = started.clone();
        async move {
            let concurrent = active.fetch_add(1, Ordering::SeqCst) + 1;
            peak.fetch_max(concurrent, Ordering::SeqCst);
            started.send(()).await.unwrap();
            gate.acquire().await.unwrap().forget();
            active.fetch_sub(1, Ordering::SeqCst);
            Ok(VariableValue::default())
        }
    });
    worker.concurrency = 4;
    worker.heartbeat_interval = Duration::from_millis(10);
    worker.reconnect_delay = Duration::from_millis(10);
    worker
}
#[tokio::test]
async fn configured_inflight_limit_applies_across_hosts_and_reconnects() {
    let mut cluster = cluster(4, 1).await;
    let gate = Arc::new(Semaphore::new(0));
    let active = Arc::new(AtomicUsize::new(0));
    let peak = Arc::new(AtomicUsize::new(0));
    let (started, mut entered) = mpsc::channel(16);
    let worker = bounded_worker(gate.clone(), active, peak.clone(), started);
    let config = cluster.config.clone();
    let (shutdown, receiver) = watch::channel(false);
    let running = tokio::spawn(async move { worker.run_cluster(config, receiver).await });
    tokio::time::timeout(Duration::from_secs(5), entered.recv())
        .await
        .unwrap()
        .unwrap();
    let mut polled = BTreeSet::new();
    tokio::time::timeout(Duration::from_secs(5), async {
        while polled.len() < 2 {
            polled.insert(cluster.polled.recv().await.unwrap());
        }
    })
    .await
    .unwrap();
    assert!(
        tokio::time::timeout(Duration::from_millis(50), entered.recv())
            .await
            .is_err(),
        "in-flight setting of one must prevent a second handler from entering"
    );
    gate.add_permits(8);
    let mut completed = BTreeSet::new();
    tokio::time::timeout(Duration::from_secs(5), async {
        for _ in 0..8 {
            completed.insert(
                cluster
                    .reports
                    .recv()
                    .await
                    .unwrap()
                    .task_run_id
                    .unwrap()
                    .task_guid,
            );
        }
    })
    .await
    .unwrap();
    assert_eq!(completed.len(), 8);
    assert_eq!(peak.load(Ordering::SeqCst), 1);
    shutdown.send(true).unwrap();
    running.await.unwrap().unwrap();
}
#[tokio::test]
async fn reassignment_drains_old_host_work_before_new_work_uses_the_slot() {
    let mut cluster = cluster(1, 0).await;
    *cluster.state.hosts.lock().unwrap() = vec![cluster.hosts[0].clone()];
    let gate = Arc::new(Semaphore::new(0));
    let peak = Arc::new(AtomicUsize::new(0));
    let (started, mut entered) = mpsc::channel(4);
    let worker = bounded_worker(
        gate.clone(),
        Arc::new(AtomicUsize::new(0)),
        peak.clone(),
        started,
    );
    let mut health = worker.health();
    let config = cluster.config.clone();
    let (shutdown, receiver) = watch::channel(false);
    let running = tokio::spawn(async move { worker.run_cluster(config, receiver).await });
    tokio::time::timeout(Duration::from_secs(5), entered.recv())
        .await
        .unwrap()
        .unwrap();
    *cluster.state.hosts.lock().unwrap() = vec![cluster.hosts[1].clone()];
    tokio::time::timeout(Duration::from_secs(5), async {
        while cluster.polled.recv().await.unwrap() != cluster.hosts[1].port as u16 {}
    })
    .await
    .unwrap();
    assert_eq!(peak.load(Ordering::SeqCst), 1);
    gate.add_permits(2);
    tokio::time::timeout(Duration::from_secs(5), async {
        for _ in 0..2 {
            cluster.reports.recv().await.unwrap();
        }
        loop {
            if health.borrow().ready() {
                break;
            }
            health.changed().await.unwrap();
        }
    })
    .await
    .unwrap();
    shutdown.send(true).unwrap();
    running.await.unwrap().unwrap();
    assert_eq!(peak.load(Ordering::SeqCst), 1);
}
#[tokio::test]
async fn zero_configured_inflight_limit_is_rejected() {
    let mut cluster = cluster(0, 0).await;
    cluster.config.inflight_tasks = 0;
    let (_shutdown, receiver) = watch::channel(false);
    assert!(matches!(
        Worker::new("zero", |_| async { Ok(VariableValue::default()) })
            .run_cluster(cluster.config.clone(), receiver)
            .await,
        Err(WorkerError::InvalidConcurrency)
    ));
}
