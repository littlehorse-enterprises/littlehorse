mod support;
// Run with: cargo test --test live_server -- --ignored
use littlehorse::{
    ClientConfig, Expression, Workflow, from_value,
    proto::*,
    to_value,
    worker::Worker,
    workflow::{primitive_type, task},
};

#[tokio::test]
#[ignore = "requires LittleHorse v1.3.1 on localhost:2023"]
async fn executes_a_workflow_with_a_rust_worker()
-> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    let mut client = tokio::time::timeout(std::time::Duration::from_secs(120), async {
        loop {
            if let Ok(mut client) = ClientConfig::from_env()?.connect().await
                && client.whoami(()).await.is_ok()
            {
                return Ok::<_, Box<dyn std::error::Error + Send + Sync>>(client);
            }
            tokio::time::sleep(std::time::Duration::from_millis(500)).await;
        }
    })
    .await??;
    let task_name = format!("rust-test-{}", uuid::Uuid::new_v4());
    client
        .put_task_def(PutTaskDefRequest {
            name: task_name.clone(),
            input_vars: vec![],
            return_type: Some(ReturnType {
                return_type: Some(primitive_type(VariableType::Str)),
            }),
            ..Default::default()
        })
        .await?;
    let workflow_name = format!("rust-test-{}", uuid::Uuid::new_v4());
    let mut workflow = Workflow::new(&workflow_name);
    workflow.add_node(
        "entrypoint",
        "start",
        node::Node::Entrypoint(EntrypointNode {}),
    )?;
    workflow.add_node("entrypoint", "task", task(&task_name, []))?;
    workflow.add_node(
        "entrypoint",
        "end",
        node::Node::Exit(ExitNode {
            result: Some(exit_node::Result::ReturnContent(
                Expression::output("task").0,
            )),
        }),
    )?;
    workflow.connect("entrypoint", "start", "task", None, vec![])?;
    workflow.connect("entrypoint", "task", "end", None, vec![])?;
    let registered = client.put_wf_spec(workflow.build()?).await?.into_inner();
    let worker = Worker::new(task_name, |_| async {
        Ok(to_value(&"rust-worker-output").unwrap())
    });
    let (shutdown, receiver) = tokio::sync::watch::channel(false);
    let worker_task = tokio::spawn({
        let client = client.clone();
        async move { worker.run(client, receiver).await }
    });
    let run = support::start_registered(&mut client, registered).await?;
    let run_id = run.id.unwrap();
    let result = tokio::time::timeout(std::time::Duration::from_secs(30), async {
        loop {
            let run = client.get_wf_run(run_id.clone()).await?.into_inner();
            if run.status == LhStatus::Completed as i32 {
                return Ok::<_, tonic::Status>(run);
            }
            assert_ne!(
                run.status,
                LhStatus::Error as i32,
                "workflow failed: {run:?}"
            );
            tokio::time::sleep(std::time::Duration::from_millis(100)).await;
        }
    })
    .await;
    shutdown.send(true)?;
    worker_task.await??;
    let run = result??;
    let output = run.thread_runs[0]
        .output
        .as_ref()
        .expect("missing workflow output");
    assert_eq!(from_value::<String>(output)?, "rust-worker-output");
    client
        .delete_wf_run(DeleteWfRunRequest { id: Some(run_id) })
        .await?;
    Ok(())
}
