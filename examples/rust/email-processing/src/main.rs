//! Scheduled email processing: every five minutes, `rust-check-inbox` starts one
//! `rust-process-email` run per new email, which reads the email, analyzes it with an
//! AI model, uploads the result to a third-party service, and marks the email processed.
//!
//! Each step has its own timeout, retries, and backoff. The integrations themselves
//! (IMAP, the AI call, the upload) are `todo!()` stubs at the bottom of this file.
use littlehorse::{
    Client, ClientConfig, LittleHorseValue, Workflow,
    proto::{ExponentialBackoffRetryPolicy, LhTaskException, RunWfRequest, ScheduleWfRequest},
    schema::UserTaskSchema,
    task,
    worker::TaskFailure,
};
use std::{collections::HashMap, path::PathBuf, sync::OnceLock};

type Error = Box<dyn std::error::Error + Send + Sync>;

const CHECK_INBOX: &str = "rust-check-inbox";
const PROCESS_EMAIL: &str = "rust-process-email";
const REVIEW_TASK: &str = "rust-review-email-analysis";

#[derive(LittleHorseValue)]
#[lh(name = "rust-email")]
struct Email {
    from: String,
    subject: String,
    body: String,
}

#[derive(LittleHorseValue)]
#[lh(name = "rust-email-analysis")]
struct EmailAnalysis {
    summary: String,
    category: String,
    /// How sure the model is, from 0 to 100.
    confidence: i64,
}

fn backoff(base_ms: i32, max_ms: i64, multiplier: f32) -> ExponentialBackoffRetryPolicy {
    ExponentialBackoffRetryPolicy {
        base_interval_ms: base_ms,
        max_delay_ms: max_ms,
        multiplier,
    }
}

/// Runs on a schedule. Starting the per-email runs is one task: it is safe to retry
/// because each run's ID is the email's Message-ID, so already-started emails are skipped.
fn check_inbox_workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    Workflow::define(CHECK_INBOX, |wf| {
        let started = wf
            .execute("rust-start-email-processing", ())
            .timeout(60)
            .with_retries(2)
            .with_exponential_backoff(backoff(1_000, 10_000, 2.0));
        wf.return_value(&started);
    })
}

/// One run per email. Steps without explicit options use the workflow defaults below.
fn process_email_workflow() -> Result<Workflow, littlehorse::WorkflowError> {
    let workflow = Workflow::define(PROCESS_EMAIL, |wf| {
        let message_id = wf.declare_str("message-id").required().searchable();

        // Uses the workflow defaults: 30s timeout, 3 retries, 1s to 30s backoff.
        let email = wf.execute("rust-fetch-email", &message_id);

        // AI calls are slow and rate-limited: a long timeout and patient backoff.
        let analysis = wf
            .execute("rust-analyze-email", &email)
            .timeout(120)
            .with_retries(5)
            .with_exponential_backoff(backoff(2_000, 60_000, 2.0));
        // An email the model can't process is a business exception: not retried.
        wf.handle_exception(&analysis, "unprocessable-email", |handler| {
            handler.execute("rust-dead-letter-email", (&message_id, "unprocessable"));
            handler.fail(
                "unprocessable-email",
                "The AI model could not process the email.",
                None,
            );
        });

        // Low-confidence results go to a person before anything is uploaded.
        wf.do_if(analysis.field("confidence").less_than(80), |wf| {
            let review = wf
                .assign_user_task_to_group(REVIEW_TASK, "email-reviewers")
                .with_notes(littlehorse::Expression::format(
                    "Low-confidence ({0}%) analysis: {1}",
                    (analysis.field("confidence"), analysis.field("summary")),
                ));
            wf.do_if(review.json_path("$.approved").equals(false), |wf| {
                wf.fail(
                    "rejected-by-reviewer",
                    "A reviewer rejected the analysis.",
                    None,
                );
            });
        });

        // Third-party outages can last minutes: more patience, and the Message-ID
        // doubles as the upload's idempotency key so a retry never uploads twice.
        let receipt = wf
            .execute("rust-upload-analysis", (&message_id, &analysis))
            .timeout(60)
            .with_retries(5)
            .with_exponential_backoff(backoff(5_000, 300_000, 3.0));
        // Retries are exhausted: record the email for follow-up and stop.
        wf.handle_any_error(&receipt, |handler| {
            handler.execute("rust-dead-letter-email", (&message_id, "upload-failed"));
            handler.fail(
                "upload-failed",
                "The upload kept failing after all retries.",
                None,
            );
        });

        // Acknowledge the email last, so a failure earlier never loses it.
        wf.execute("rust-mark-email-processed", &message_id)
            .with_retries(10)
            .with_exponential_backoff(backoff(1_000, 60_000, 2.0));
        wf.return_value(&receipt);
    })?;
    Ok(workflow.default_task_options(30, 3, Some(backoff(1_000, 30_000, 2.0))))
}

/// The tasks. Return `TaskFailure::Error` for problems a retry might fix (network,
/// rate limits, outages) and `TaskFailure::Exception` for ones it won't.
static CLIENT: OnceLock<Client> = OnceLock::new();

#[task("rust-start-email-processing")]
async fn start_email_processing() -> Result<i64, TaskFailure> {
    let mut client = CLIENT.get().expect("set in main").clone();
    let mut started = 0;
    for message_id in list_new_message_ids().await? {
        let request = RunWfRequest {
            wf_spec_name: PROCESS_EMAIL.into(),
            id: Some(message_id.clone()),
            variables: HashMap::from([("message-id".into(), message_id.to_lh().unwrap())]),
            ..Default::default()
        };
        match client.run_wf(request).await {
            Ok(_) => started += 1,
            // Seen by an earlier poll: this email already has a run.
            Err(status) if status.code() == tonic::Code::AlreadyExists => {}
            Err(status) => return Err(TaskFailure::Error(status.to_string())),
        }
    }
    Ok(started)
}

#[task("rust-fetch-email")]
async fn fetch_email(message_id: String) -> Result<Email, TaskFailure> {
    read_email(&message_id).await
}

#[task("rust-analyze-email")]
async fn analyze_email(email: Email) -> Result<EmailAnalysis, TaskFailure> {
    analyze_with_ai(&email).await
}

#[task("rust-upload-analysis")]
async fn upload_analysis(
    message_id: String,
    analysis: EmailAnalysis,
) -> Result<String, TaskFailure> {
    upload(&analysis, &message_id).await
}

#[task("rust-mark-email-processed")]
async fn mark_email_processed(message_id: String) -> Result<(), TaskFailure> {
    move_to_processed_folder(&message_id).await
}

#[task("rust-dead-letter-email")]
async fn dead_letter_email(message_id: String, reason: String) -> Result<(), TaskFailure> {
    record_dead_letter(&message_id, &reason).await
}

/// The integrations. Replace each `todo!()` with the real call.
async fn list_new_message_ids() -> Result<Vec<String>, TaskFailure> {
    todo!("IMAP: SEARCH UNSEEN in INBOX and return each email's Message-ID")
}

async fn read_email(message_id: &str) -> Result<Email, TaskFailure> {
    todo!("IMAP: FETCH {message_id} and parse its sender, subject, and body")
}

async fn analyze_with_ai(email: &Email) -> Result<EmailAnalysis, TaskFailure> {
    // A rate limit or 5xx: Err(TaskFailure::Error(..)), so LittleHorse retries.
    // Content the model refuses: Err(unprocessable()), which is not retried.
    todo!(
        "AI: summarize and categorize the email from {} about {:?} ({} bytes)",
        email.from,
        email.subject,
        email.body.len()
    )
}

async fn upload(analysis: &EmailAnalysis, idempotency_key: &str) -> Result<String, TaskFailure> {
    todo!(
        "HTTP: POST the {} analysis ({:?}) with Idempotency-Key {idempotency_key}; return the receipt ID",
        analysis.category,
        analysis.summary
    )
}

async fn move_to_processed_folder(message_id: &str) -> Result<(), TaskFailure> {
    todo!("IMAP: MOVE {message_id} to the Processed folder")
}

async fn record_dead_letter(message_id: &str, reason: &str) -> Result<(), TaskFailure> {
    todo!("Store {message_id} with reason {reason:?} for someone to follow up")
}

#[allow(
    dead_code,
    reason = "returned by analyze_with_ai once it is implemented"
)]
fn unprocessable() -> TaskFailure {
    TaskFailure::Exception(Box::new(LhTaskException {
        name: "unprocessable-email".into(),
        message: "The AI model could not process this email.".into(),
        ..Default::default()
    }))
}

fn config() -> Result<ClientConfig, Error> {
    let path = std::env::var_os("HOME")
        .or_else(|| std::env::var_os("USERPROFILE"))
        .map(|home| PathBuf::from(home).join(".config/littlehorse.config"));
    match path.filter(|path| path.is_file()) {
        Some(path) => ClientConfig::from_file(path),
        None => ClientConfig::from_env(),
    }
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let config = config()?;
    let mut client = config.clone().connect().await?;
    CLIENT.set(client.clone()).ok();

    // Registers each TaskDef, plus the Email and EmailAnalysis StructDefs it uses.
    start_email_processing::register(&mut client).await?;
    fetch_email::register(&mut client).await?;
    analyze_email::register(&mut client).await?;
    upload_analysis::register(&mut client).await?;
    mark_email_processed::register(&mut client).await?;
    dead_letter_email::register(&mut client).await?;
    client
        .put_user_task_def(
            UserTaskSchema::new(REVIEW_TASK)
                .field(UserTaskSchema::typed_field::<bool>("approved", true)?)
                .compile()?,
        )
        .await?;
    process_email_workflow()?.register(&mut client).await?;
    check_inbox_workflow()?.register(&mut client).await?;

    let schedule = client
        .schedule_wf(ScheduleWfRequest {
            id: Some("rust-check-inbox-every-5-minutes".into()),
            wf_spec_name: CHECK_INBOX.into(),
            cron_expression: "*/5 * * * *".into(),
            ..Default::default()
        })
        .await;
    match schedule {
        Ok(_) => {}
        Err(status) if status.code() == tonic::Code::AlreadyExists => {}
        Err(status) => return Err(status.into()),
    }
    println!("Registered {PROCESS_EMAIL}; {CHECK_INBOX} runs every five minutes.");

    // AI calls are rate-limited: run at most four at a time from this process.
    let mut analyze = analyze_email::worker();
    analyze.concurrency = 4;
    littlehorse::worker::start(
        config,
        [
            start_email_processing::worker(),
            fetch_email::worker(),
            analyze,
            upload_analysis::worker(),
            mark_email_processed::worker(),
            dead_letter_email::worker(),
        ],
    )
    .await?;
    Ok(())
}
