use littlehorse::{
    ClientConfig, Workflow,
    proto::{
        little_horse_server::{LittleHorse, LittleHorseServer},
        *,
    },
};
use std::sync::{
    Arc,
    atomic::{AtomicUsize, Ordering},
};
use tokio_stream::wrappers::TcpListenerStream;
use tonic::{Request, Response, Status};
struct Metadata {
    lookups: Arc<AtomicUsize>,
    transient_misses: usize,
}
#[tonic::async_trait]
impl LittleHorse for Metadata {
    async fn get_latest_user_task_def(
        &self,
        request: Request<GetLatestUserTaskDefRequest>,
    ) -> Result<Response<UserTaskDef>, Status> {
        let attempt = self.lookups.fetch_add(1, Ordering::SeqCst);
        assert_eq!(request.into_inner().name, "approval");
        if attempt < self.transient_misses {
            return Err(Status::not_found("new definition not visible yet"));
        }
        Ok(Response::new(UserTaskDef {
            name: "approval".into(),
            version: 0,
            ..Default::default()
        }))
    }
    async fn put_wf_spec(
        &self,
        request: Request<PutWfSpecRequest>,
    ) -> Result<Response<WfSpec>, Status> {
        let request = request.into_inner();
        let versions: Vec<_> = request
            .thread_specs
            .values()
            .flat_map(|thread| thread.nodes.values())
            .filter_map(|node| match &node.node {
                Some(node::Node::UserTask(task)) => Some(task.user_task_def_version),
                _ => None,
            })
            .collect();
        assert_eq!(
            versions
                .iter()
                .filter(|version| **version == Some(0))
                .count(),
            2
        );
        assert_eq!(
            versions
                .iter()
                .filter(|version| **version == Some(7))
                .count(),
            1
        );
        assert_eq!(
            self.lookups.load(Ordering::SeqCst),
            self.transient_misses + 1
        );
        Ok(Response::new(WfSpec {
            id: Some(WfSpecId {
                name: request.name,
                major_version: 0,
                revision: 0,
            }),
            ..Default::default()
        }))
    }
}
#[tokio::test]
async fn registration_pins_latest_once_preserves_explicit_versions_and_leaves_compile_offline() {
    for transient_misses in [0, 1] {
        let listener = tokio::net::TcpListener::bind("127.0.0.1:0").await.unwrap();
        let address = listener.local_addr().unwrap();
        let lookups = Arc::new(AtomicUsize::new(0));
        let server = tokio::spawn(
            tonic::transport::Server::builder()
                .add_service(LittleHorseServer::new(Metadata {
                    lookups: lookups.clone(),
                    transient_misses,
                }))
                .serve_with_incoming(TcpListenerStream::new(listener)),
        );
        let mut client = ClientConfig {
            endpoint: format!("http://{address}"),
            ..Default::default()
        }
        .connect()
        .await
        .unwrap();
        let workflow = Workflow::define("approval-test", |thread| {
            thread.user_task("approval", Some("alice".into()), None, None);
            thread.user_task("approval", Some("alice".into()), None, None);
            let explicit = thread.user_task("approval", Some("alice".into()), None, None);
            let Some(node::Node::UserTask(task)) = &mut thread.node_mut(&explicit)?.node else {
                unreachable!()
            };
            task.user_task_def_version = Some(7);
            Ok(())
        })
        .unwrap();
        let offline = workflow.compile().unwrap();
        assert_eq!(lookups.load(Ordering::SeqCst), 0);
        workflow.register(&mut client).await.unwrap();
        assert_eq!(workflow.compile().unwrap(), offline);
        server.abort();
    }
}
