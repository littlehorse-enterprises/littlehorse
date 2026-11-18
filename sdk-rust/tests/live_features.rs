mod support;
// Server-backed behavioral coverage, run automatically in the integration CI job.
use littlehorse::{
    Client, ClientConfig, EventOptions, LittleHorseValue, StructSchema, TaskOptions, Variable,
    Workflow,
    proto::*,
    schema::UserTaskSchema,
    types::Inline,
    worker::{TaskFailure, Worker},
};
use std::{
    collections::HashMap,
    sync::{
        Arc,
        atomic::{AtomicUsize, Ordering},
    },
    time::Duration,
};
type Error = Box<dyn std::error::Error + Send + Sync>;
async fn ready() -> Result<Client, Error> {
    tokio::time::timeout(Duration::from_secs(120), async {
        loop {
            if let Ok(mut client) = ClientConfig::from_env()?.connect().await
                && client.whoami(()).await.is_ok()
            {
                return Ok(client);
            }
            tokio::time::sleep(Duration::from_millis(200)).await;
        }
    })
    .await?
}
fn unique(prefix: &str) -> String {
    format!("rust-{prefix}-{}", uuid::Uuid::new_v4())
}
async fn run(client: &mut Client, workflow: Workflow) -> Result<WfRunId, Error> {
    let registered = workflow.register(client).await?;
    Ok(support::start_registered(client, registered)
        .await?
        .id
        .unwrap())
}
async fn completed(client: &mut Client, id: WfRunId) -> Result<VariableValue, Error> {
    tokio::time::timeout(Duration::from_secs(30), async {
        loop {
            let run = client.get_wf_run(id.clone()).await?.into_inner();
            if run.status == LhStatus::Completed as i32 {
                return Ok(run.thread_runs[0].output.clone().unwrap_or_default());
            }
            if run.status == LhStatus::Error as i32 {
                return Err(format!("workflow failed: {run:?}").into());
            }
            tokio::time::sleep(Duration::from_millis(100)).await;
        }
    })
    .await?
}
#[tokio::test]
#[ignore = "requires live LittleHorse"]
async fn branches_loops_and_parallel_threads_execute_correctly() -> Result<(), Error> {
    let mut client = ready().await?;
    let workflow = Workflow::define(unique("control"), |thread| {
        let count =
            thread.declare(Variable::typed::<i64>("count").default_value(0_i64.to_lh().unwrap()));
        thread.while_loop(count.expression().less_than(3), |thread| {
            thread.mutate(&count, VariableMutationType::Add, 1.into());
            Ok(())
        })?;
        thread.if_else(
            count.expression().equals(3),
            |thread| {
                thread.assign(&count, count.expression().plus(100));
                Ok(())
            },
            |thread| {
                thread.fail("wrong-loop-result", "count must equal three", None);
                Ok(())
            },
        )?;
        let child = thread.spawn("child", HashMap::new(), |thread| {
            thread.return_value("child-result".into());
            Ok(())
        })?;
        thread.wait_for_threads(&[child], WaitForThreadsStrategy::WaitForAll);
        thread.return_value(count.expression());
        Ok(())
    })?;
    let id = run(&mut client, workflow).await?;
    assert_eq!(i64::from_lh(&completed(&mut client, id).await?)?, 103);
    Ok(())
}
#[tokio::test]
#[ignore = "requires live LittleHorse"]
#[allow(deprecated)] // Exercise compatibility with legacy primitive forms.
async fn events_and_human_tasks_resume_workflows() -> Result<(), Error> {
    let mut client = ready().await?;
    let event = unique("event");
    client
        .put_external_event_def(PutExternalEventDefRequest {
            name: event.clone(),
            content_type: Some(ReturnType {
                return_type: Some(String::type_definition()),
            }),
            ..Default::default()
        })
        .await?;
    let workflow = Workflow::define(unique("event-wf"), |thread| {
        let output = thread.wait_for_event(EventOptions::new(&event).timeout(30.into()));
        thread.return_value(output.expression());
        Ok(())
    })?;
    let id = run(&mut client, workflow).await?;
    client
        .put_external_event(PutExternalEventRequest {
            wf_run_id: Some(id.clone()),
            external_event_def_id: Some(ExternalEventDefId { name: event }),
            content: Some("paid".to_string().to_lh()?),
            ..Default::default()
        })
        .await?;
    assert_eq!(String::from_lh(&completed(&mut client, id).await?)?, "paid");
    let form = unique("approval");
    client
        .put_user_task_def(
            UserTaskSchema::new(&form)
                .field(UserTaskSchema::typed_field::<bool>("approved", true)?)
                .compile()?,
        )
        .await?;
    let workflow = Workflow::define(unique("approval-wf"), |thread| {
        let output = thread.user_task(
            &form,
            Some("alice".into()),
            None,
            Some("Review request".into()),
        );
        thread.return_value(output.expression());
        Ok(())
    })?;
    let id = run(&mut client, workflow).await?;
    let task = tokio::time::timeout(Duration::from_secs(10), async {
        loop {
            let tasks = client
                .list_user_task_runs(ListUserTaskRunRequest {
                    wf_run_id: Some(id.clone()),
                    ..Default::default()
                })
                .await?
                .into_inner();
            if let Some(task) = tasks.results.into_iter().next() {
                return Ok::<_, tonic::Status>(task);
            }
            tokio::time::sleep(Duration::from_millis(100)).await;
        }
    })
    .await??;
    client
        .complete_user_task_run(CompleteUserTaskRunRequest {
            user_task_run_id: task.id,
            user_id: "alice".into(),
            results: HashMap::from([("approved".into(), true.to_lh()?)]),
            ..Default::default()
        })
        .await?;
    let output = completed(&mut client, id).await?;
    assert_eq!(
        littlehorse::from_value::<serde_json::Value>(&output)?["approved"],
        true
    );
    Ok(())
}
#[derive(Clone, Debug, PartialEq, LittleHorseValue)]
#[lh(name = "rust-native-test-record")]
struct Record {
    name: String,
    scores: Vec<i64>,
    attributes: HashMap<String, i64>,
    optional: Option<String>,
}
#[tokio::test]
#[ignore = "requires live LittleHorse"]
async fn native_values_and_inline_structs_survive_server_storage() -> Result<(), Error> {
    let mut client = ready().await?;
    client.put_struct_def(Record::registration()).await?;
    let record = Record {
        name: "Ada".into(),
        scores: vec![1, 2, 3],
        attributes: HashMap::from([("rating".into(), 5)]),
        optional: None,
    };
    for inline in [false, true] {
        let value = if inline {
            Inline(record.clone()).to_lh()?
        } else {
            record.to_lh()?
        };
        let definition = if inline {
            Inline::<Record>::type_definition()
        } else {
            Record::type_definition()
        };
        let workflow = Workflow::define(unique("native"), |thread| {
            let variable = thread.declare(Variable::new("record", definition).default_value(value));
            thread.return_value(variable.expression());
            Ok(())
        })?;
        let id = run(&mut client, workflow).await?;
        assert_eq!(Record::from_lh(&completed(&mut client, id).await?)?, record);
    }
    Ok(())
}
#[tokio::test]
#[ignore = "requires live LittleHorse"]
async fn durable_checkpoints_replay_after_task_retry_in_cluster_worker() -> Result<(), Error> {
    let mut client = ready().await?;
    let task_name = unique("checkpoint");
    let calls = Arc::new(AtomicUsize::new(0));
    let worker = Worker::new(&task_name, {
        let calls = calls.clone();
        move |mut context| {
            let calls = calls.clone();
            async move {
                let value = context
                    .checkpoint_typed(|| async {
                        calls.fetch_add(1, Ordering::SeqCst);
                        Ok("checkpoint-value".to_string())
                    })
                    .await?;
                if context.task.attempt_number == 0 {
                    return Err(TaskFailure::Error("retry after persistence".into()));
                }
                value
                    .to_lh()
                    .map_err(|error| TaskFailure::Output(error.to_string()))
            }
        }
    });
    worker
        .register(&mut client, vec![], Some(String::type_definition()))
        .await?;
    let workflow = Workflow::define(unique("checkpoint-wf"), |thread| {
        let output = thread.execute_with(TaskOptions::new(&task_name, []).retries(1).timeout(10));
        thread.return_value(output.expression());
        Ok(())
    })?;
    let (shutdown, receiver) = tokio::sync::watch::channel(false);
    let config = ClientConfig::from_env()?;
    let running = tokio::spawn(async move { worker.run_cluster(config, receiver).await });
    let id = run(&mut client, workflow).await?;
    let result = completed(&mut client, id).await;
    shutdown.send(true)?;
    running.await??;
    assert_eq!(String::from_lh(&result?)?, "checkpoint-value");
    assert_eq!(calls.load(Ordering::SeqCst), 1);
    Ok(())
}
