use super::*;
use littlehorse::proto::{
    ExternalEventDefId, LhStatus, ListTaskRunsRequest, PutCorrelatedEventRequest, RunWfRequest,
};
use std::{collections::HashMap, time::Duration};
use tokio::time::{Instant, sleep, timeout};

async fn exercise_branches(client: &mut Client, specification: WfSpec) -> Result<(), Error> {
    let specification_id = specification.id.expect("registered workflow has an ID");
    for approved in [true, false] {
        let run_id = uuid::Uuid::new_v4().to_string();
        let email = format!("{run_id}@example.com");
        let request = RunWfRequest {
            wf_spec_name: specification_id.name.clone(),
            major_version: Some(specification_id.major_version),
            revision: Some(specification_id.revision),
            id: Some(run_id),
            variables: HashMap::from([
                ("full-name".into(), "Obi-Wan Kenobi".to_string().to_lh()?),
                ("email".into(), email.to_lh()?),
                ("ssn".into(), 123456789_i64.to_lh()?),
            ]),
            ..Default::default()
        };
        let deadline = Instant::now() + Duration::from_secs(5);
        let run = loop {
            match client.run_wf(request.clone()).await {
                Ok(response) => break response.into_inner(),
                Err(error)
                    if error.code() == tonic::Code::NotFound
                        && error.message() == "Couldn't find specified WfSpec"
                        && Instant::now() < deadline =>
                {
                    sleep(Duration::from_millis(25)).await
                }
                Err(error) => return Err(error.into()),
            }
        };
        let id = run.id.expect("started workflow has an ID");
        client
            .put_correlated_event(PutCorrelatedEventRequest {
                key: email,
                external_event_def_id: Some(ExternalEventDefId {
                    name: IDENTITY_VERIFIED.into(),
                }),
                content: Some(approved.to_lh()?),
            })
            .await?;
        timeout(Duration::from_secs(60), async {
            loop {
                let run = client.get_wf_run(id.clone()).await?.into_inner();
                if run.status == LhStatus::Completed as i32 {
                    return Ok::<_, Error>(());
                }
                if run.status == LhStatus::Error as i32 {
                    return Err(format!("quickstart failed: {run:?}").into());
                }
                sleep(Duration::from_millis(100)).await;
            }
        })
        .await??;
        let tasks = client
            .list_task_runs(ListTaskRunsRequest {
                wf_run_id: Some(id),
                ..Default::default()
            })
            .await?
            .into_inner();
        let mut names: Vec<_> = tasks
            .results
            .iter()
            .filter_map(|task| task.task_def_id.as_ref().map(|id| id.name.as_str()))
            .collect();
        names.sort_unstable();
        let expected_notification = if approved {
            NOTIFY_VERIFIED
        } else {
            NOTIFY_NOT_VERIFIED
        };
        let mut expected = vec![VERIFY_IDENTITY, expected_notification];
        expected.sort_unstable();
        assert_eq!(
            names, expected,
            "wrong notification for approved={approved}"
        );
    }
    Ok(())
}

#[tokio::test]
#[ignore = "requires a running LittleHorse server"]
async fn approval_and_rejection_complete_with_the_correct_notification() -> Result<(), Error> {
    let config = ClientConfig::from_env()?;
    let mut client = timeout(Duration::from_secs(120), async {
        loop {
            if let Ok(mut client) = config.clone().connect().await
                && client.whoami(()).await.is_ok()
            {
                return client;
            }
            sleep(Duration::from_millis(200)).await;
        }
    })
    .await?;
    let workers = workers();
    let specification = register(&mut client, &workers).await?;
    let (shutdown, receiver) = watch::channel(false);
    let mut tasks = start_workers(workers, config, receiver);
    let result = exercise_branches(&mut client, specification).await;
    shutdown.send_replace(true);
    while let Some(worker) = tasks.join_next().await {
        worker??;
    }
    result
}
