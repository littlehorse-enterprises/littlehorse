use littlehorse::{
    ClientConfig, Expression, Workflow, from_value,
    proto::*,
    to_value,
    worker::{TaskFailure, Worker},
    workflow::{primitive_type, task},
};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error + Send + Sync>> {
    let mut client = ClientConfig::from_env()?.connect().await?;
    client
        .put_task_def(PutTaskDefRequest {
            name: "rust-greet".into(),
            input_vars: vec![VariableDef {
                name: "name".into(),
                type_def: Some(primitive_type(VariableType::Str)),
                ..Default::default()
            }],
            return_type: Some(ReturnType {
                return_type: Some(primitive_type(VariableType::Str)),
            }),
            ..Default::default()
        })
        .await?;
    let mut workflow = Workflow::new("rust-quickstart");
    workflow.add_node(
        "entrypoint",
        "start",
        node::Node::Entrypoint(EntrypointNode {}),
    )?;
    workflow.add_node(
        "entrypoint",
        "greet",
        task("rust-greet", [Expression::literal(&"world")?]),
    )?;
    workflow.add_node(
        "entrypoint",
        "end",
        node::Node::Exit(ExitNode {
            result: Some(exit_node::Result::ReturnContent(
                Expression::output("greet").0,
            )),
        }),
    )?;
    workflow.connect("entrypoint", "start", "greet", None, vec![])?;
    workflow.connect("entrypoint", "greet", "end", None, vec![])?;
    client.put_wf_spec(workflow.build()?).await?;
    let worker = Worker::new("rust-greet", |context| async move {
        let value = context
            .task
            .variables
            .first()
            .and_then(|variable| variable.value.as_ref())
            .ok_or_else(|| TaskFailure::Error("missing name input".into()))?;
        let name: String =
            from_value(value).map_err(|error| TaskFailure::Error(error.to_string()))?;
        to_value(&format!("Hello, {name}!")).map_err(|error| TaskFailure::Error(error.to_string()))
    });
    let (shutdown, receiver) = tokio::sync::watch::channel(false);
    let worker_task = tokio::spawn({
        let client = client.clone();
        async move { worker.run(client, receiver).await }
    });
    let run = client
        .run_wf(RunWfRequest {
            wf_spec_name: "rust-quickstart".into(),
            ..Default::default()
        })
        .await?
        .into_inner();
    println!("Workflow started: {:?}", run.id);
    tokio::signal::ctrl_c().await?;
    shutdown.send(true)?;
    worker_task.await??;
    Ok(())
}
