mod support;
// Exercises APIs introduced on the pinned upstream master, beyond v1.3.1.
use littlehorse::{
    ClientConfig, LittleHorseValue, StructSchema, Workflow, proto::*, schema::UserTaskSchema,
};
use std::time::Duration;
#[derive(Debug, PartialEq, LittleHorseValue)]
#[lh(name = "rust-master-approval-result")]
struct Approval {
    approved: bool,
    reason: Option<String>,
}
#[tokio::test]
#[ignore = "requires the pinned master server"]
async fn struct_backed_user_tasks_accept_native_completion_output()
-> Result<(), littlehorse::client::ClientError> {
    let mut client = tokio::time::timeout(Duration::from_secs(120), async {
        loop {
            if let Ok(mut client) = ClientConfig::from_env()?.connect().await
                && client.whoami(()).await.is_ok()
            {
                return Ok::<_, littlehorse::client::ClientError>(client);
            }
            tokio::time::sleep(Duration::from_millis(200)).await;
        }
    })
    .await??;
    let definition = client
        .put_struct_def(Approval::registration())
        .await?
        .into_inner()
        .id
        .unwrap();
    let form = format!("rust-master-form-{}", uuid::Uuid::new_v4());
    client
        .put_user_task_def(UserTaskSchema::struct_backed(&form, definition.clone()).compile()?)
        .await?;
    let name = format!("rust-master-workflow-{}", uuid::Uuid::new_v4());
    let workflow = Workflow::define(&name, |thread| {
        let output = thread.user_task(&form, Some("alice".into()), None, None);
        thread.return_value(output.expression());
        Ok(())
    })?;
    let registered = workflow.register(&mut client).await?;
    let id = support::start_registered(&mut client, registered)
        .await?
        .id
        .unwrap();
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
    let answer = Approval {
        approved: true,
        reason: Some("reviewed".into()),
    };
    let mut output = answer.to_lh()?;
    if let Some(variable_value::Value::Struct(value)) = &mut output.value {
        value.struct_def_id = Some(definition);
    }
    client
        .complete_user_task_run(CompleteUserTaskRunRequest {
            user_task_run_id: task.id,
            user_id: "alice".into(),
            output: Some(output),
            ..Default::default()
        })
        .await?;
    let completed = tokio::time::timeout(Duration::from_secs(10), async {
        loop {
            let run = client.get_wf_run(id.clone()).await?.into_inner();
            assert_ne!(
                run.status,
                LhStatus::Error as i32,
                "workflow failed: {run:?}"
            );
            if run.status == LhStatus::Completed as i32 {
                return Ok::<_, tonic::Status>(run.thread_runs[0].output.clone().unwrap());
            }
            tokio::time::sleep(Duration::from_millis(100)).await;
        }
    })
    .await??;
    assert_eq!(Approval::from_lh(&completed)?, answer);
    Ok(())
}
