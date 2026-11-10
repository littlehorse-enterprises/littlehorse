use littlehorse::{
    ClientConfig, from_value,
    proto::{
        little_horse_server::{LittleHorse, LittleHorseServer},
        *,
    },
    to_value,
    worker::Worker,
};
use std::sync::Arc;
use tokio::sync::{mpsc, watch};
use tokio_stream::wrappers::{ReceiverStream, TcpListenerStream};
use tonic::{Request, Response, Status};

struct TestServer {
    reports: mpsc::Sender<ReportTaskRun>,
}
#[tonic::async_trait]
impl LittleHorse for TestServer {
    async fn poll_task(
        &self,
        request: Request<tonic::Streaming<PollTaskRequest>>,
    ) -> Result<
        Response<
            std::pin::Pin<
                Box<
                    dyn tokio_stream::Stream<Item = Result<PollTaskResponse, Status>>
                        + Send
                        + 'static,
                >,
            >,
        >,
        Status,
    > {
        assert_eq!(request.metadata().get("tenantid").unwrap(), "test-tenant");
        assert_eq!(
            request.metadata().get("authorization").unwrap(),
            "Bearer test-token"
        );
        let mut input = request.into_inner();
        let (sender, receiver) = mpsc::channel(1);
        tokio::spawn(async move {
            let poll = input.message().await.unwrap().unwrap();
            assert_eq!(poll.task_def_id.unwrap().name, "test-task");
            sender
                .send(Ok(PollTaskResponse {
                    result: Some(ScheduledTask {
                        attempt_number: 2,
                        total_observed_checkpoints: 1,
                        ..Default::default()
                    }),
                }))
                .await
                .unwrap();
            // Keep the stream alive until the worker closes its request stream.
            while input.message().await.unwrap().is_some() {}
        });
        Ok(Response::new(Box::pin(ReceiverStream::new(receiver))))
    }
    async fn get_checkpoint(
        &self,
        request: Request<CheckpointId>,
    ) -> Result<Response<Checkpoint>, Status> {
        assert_eq!(request.into_inner().checkpoint_number, 0);
        Ok(Response::new(Checkpoint {
            value: Some(to_value(&"replayed").unwrap()),
            ..Default::default()
        }))
    }
    async fn put_checkpoint(
        &self,
        request: Request<PutCheckpointRequest>,
    ) -> Result<Response<PutCheckpointResponse>, Status> {
        let checkpoint = request.into_inner();
        assert_eq!(checkpoint.task_attempt, 2);
        assert_eq!(
            from_value::<String>(checkpoint.value.as_ref().unwrap()).unwrap(),
            "fresh"
        );
        Ok(Response::new(PutCheckpointResponse {
            flow_control_continue_type: 0,
            created_checkpoint: None,
        }))
    }
    async fn report_task(&self, request: Request<ReportTaskRun>) -> Result<Response<()>, Status> {
        self.reports.send(request.into_inner()).await.unwrap();
        Ok(Response::new(()))
    }
}
#[tokio::test]
async fn worker_streams_and_reports_results_with_authentication() {
    let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
    let address = listener.local_addr().unwrap();
    let (reports, mut received) = mpsc::channel(1);
    let (server_shutdown, server_receiver) = tokio::sync::oneshot::channel();
    let server = tokio::spawn(
        tonic::transport::Server::builder()
            .add_service(LittleHorseServer::new(TestServer { reports }))
            .serve_with_incoming_shutdown(TcpListenerStream::new(listener), async {
                server_receiver.await.ok();
            }),
    );
    let client = ClientConfig {
        endpoint: format!("http://{address}"),
        tenant_id: Some("test-tenant".into()),
        bearer_token: Some("test-token".into()),
        tls: None,
        ..Default::default()
    }
    .connect()
    .await
    .unwrap();
    let calls = Arc::new(std::sync::atomic::AtomicUsize::new(0));
    let worker = Worker::new("test-task", {
        let calls = calls.clone();
        move |mut context| {
            let calls = calls.clone();
            async move {
                assert_eq!(context.task.attempt_number, 2);
                let replayed = context
                    .checkpoint(|| async { panic!("persisted checkpoint must not reexecute") })
                    .await?;
                assert_eq!(from_value::<String>(&replayed).unwrap(), "replayed");
                let fresh = context
                    .checkpoint(|| async { Ok(to_value(&"fresh").unwrap()) })
                    .await?;
                assert_eq!(from_value::<String>(&fresh).unwrap(), "fresh");
                calls.fetch_add(1, std::sync::atomic::Ordering::SeqCst);
                Ok(to_value(&"done").unwrap())
            }
        }
    });
    let (shutdown, receiver) = watch::channel(false);
    let running = tokio::spawn(async move { worker.run(client, receiver).await });
    let report = tokio::time::timeout(std::time::Duration::from_secs(5), received.recv())
        .await
        .unwrap()
        .unwrap();
    assert_eq!(report.attempt_number, 2);
    assert_eq!(report.total_checkpoints, 2);
    assert_eq!(report.status, TaskStatus::TaskSuccess as i32);
    let Some(report_task_run::Result::Output(output)) = report.result else {
        panic!("expected output")
    };
    assert_eq!(from_value::<String>(&output).unwrap(), "done");
    assert_eq!(calls.load(std::sync::atomic::Ordering::SeqCst), 1);
    shutdown.send(true).unwrap();
    running.await.unwrap().unwrap();
    server_shutdown.send(()).unwrap();
    server.await.unwrap().unwrap();
}
