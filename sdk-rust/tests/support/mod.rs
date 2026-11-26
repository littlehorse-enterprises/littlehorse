//! Fresh metadata can be acknowledged before all core processors observe it.
use littlehorse::{Client, proto::*};
use std::time::Duration;
/// Start exactly the version returned by registration. Retry only the server's
/// pre-execution metadata-not-visible response, bounded to five seconds. A stable
/// run ID prevents duplicate creation; transport and other errors are never hidden.
pub async fn start_registered(
    client: &mut Client,
    specification: WfSpec,
) -> Result<WfRun, tonic::Status> {
    let id = specification
        .id
        .ok_or_else(|| tonic::Status::internal("registration returned no workflow ID"))?;
    let request = RunWfRequest {
        wf_spec_name: id.name,
        major_version: Some(id.major_version),
        revision: Some(id.revision),
        id: Some(uuid::Uuid::new_v4().to_string()),
        ..Default::default()
    };
    let deadline = tokio::time::Instant::now() + Duration::from_secs(5);
    loop {
        match client.run_wf(request.clone()).await {
            Ok(response) => return Ok(response.into_inner()),
            Err(error)
                if error.code() == tonic::Code::NotFound
                    && error.message() == "Couldn't find specified WfSpec"
                    && tokio::time::Instant::now() < deadline =>
            {
                tokio::time::sleep(Duration::from_millis(25)).await
            }
            Err(error) => return Err(error),
        }
    }
}
