use littlehorse::{task, worker::TaskFailure};

#[task("send-email")]
async fn send_email(email: String, subject: String, body: String) -> Result<String, TaskFailure> {
    Ok(format!("{email}: {subject}: {body}"))
}

#[tokio::test]
async fn task_macro_preserves_function_and_generates_worker() {
    assert_eq!(
        send_email("Ada".into(), "Hello".into(), "World".into())
            .await
            .unwrap(),
        "Ada: Hello: World"
    );
    assert_eq!(send_email::worker().task_name, "send-email");
    let _register = send_email::register;
}
