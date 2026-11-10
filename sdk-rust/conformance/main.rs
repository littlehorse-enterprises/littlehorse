//! Upstream sdk-conformance testee. Answers are built from Rust APIs, never fixtures.
mod base;
mod fuzz;
mod registrations;
mod serde_cases;
mod workflows;
use littlehorse::{client::ClientError, json, proto::*};
use std::{collections::BTreeMap, io::BufRead};
fn flag<'a>(arguments: &'a [String], name: &str) -> Result<&'a str, ClientError> {
    arguments
        .windows(2)
        .find(|pair| pair[0] == name)
        .map(|pair| pair[1].as_str())
        .ok_or_else(|| format!("missing {name}").into())
}
fn variant(arguments: &[String]) -> Result<bool, ClientError> {
    match flag(arguments, "--variant")? {
        "feature" => Ok(true),
        "base" => Ok(false),
        _ => Err("variant must be base or feature".into()),
    }
}
fn serialize_workflow(request: &PutWfSpecRequest) -> Result<serde_json::Value, ClientError> {
    json::message_to_json_value("littlehorse.PutWfSpecRequest", request)
}
fn run(arguments: &[String]) -> Result<(), ClientError> {
    let command = arguments.first().map(String::as_str).unwrap_or("");
    let output = match command {
        "list" => {
            let mut names: Vec<_> = workflows::feature_workflows().into_keys().collect();
            names.extend(registrations::CASES.iter().map(|name| (*name).into()));
            // The convert verb accepts typed inputs; these IDs advertise the standard corpus cases.
            names.extend(["str-basic", "str-empty", "str-unicode", "int-answer", "int-zero", "int-negative", "int-max-safe", "int-beyond-double", "double-half", "double-negative", "double-whole", "bool-true", "bool-false", "bytes-basic", "timestamp-millis", "json-obj-single", "json-obj-empty", "json-arr-single", "json-arr-empty", "null-value"].map(String::from));
            names.sort(); println!("{}", names.join("\n")); return Ok(());
        }
        "compile" => {
            let case = flag(arguments, "--case")?;
            let request = if variant(arguments)? { workflows::feature_workflows().remove(case).ok_or("unknown workflow case")? } else { base::base_workflow(case)? };
            serialize_workflow(&request)?
        }
        "compile-all" => {
            let mut answers = BTreeMap::new();
            for (case, request) in workflows::feature_workflows() {
                if case != "workflow-minimal" { answers.insert(format!("{case}/base"), serialize_workflow(&base::base_workflow(&case)?)?); }
                answers.insert(format!("{case}/feature"), serialize_workflow(&request)?);
            } serde_json::to_value(answers)?
        }
        "registrations" => registrations::answer(flag(arguments, "--case")?, variant(arguments)?)?,
        "registrations-all" => {
            let mut answers = BTreeMap::new();
            for case in registrations::CASES { for (variant, feature) in [("base", false), ("feature", true)] { answers.insert(format!("{case}/{variant}"), registrations::answer(case, feature)?); } }
            serde_json::to_value(answers)?
        }
        "convert" => {
            let value = serde_cases::convert(flag(arguments, "--type")?, flag(arguments, "--value").ok())?;
            json::message_to_json_value("littlehorse.VariableValue", &value)?
        }
        "convert-batch" => {
            let mut answers = BTreeMap::new();
            for line in std::io::stdin().lock().lines() {
                let input: serde_json::Value = serde_json::from_str(&line?)?;
                let value = serde_cases::convert(input["type"].as_str().ok_or("missing type")?, input["value"].as_str())?;
                answers.insert(input["id"].as_str().ok_or("missing id")?.to_owned(), json::message_to_json_value("littlehorse.VariableValue", &value)?);
            } serde_json::to_value(answers)?
        }
        "fuzz" => {
            let workflow = fuzz::build(flag(arguments, "--seed")?.parse()?, flag(arguments, "--ops")?.parse()?)?;
            let request = workflow.compile()?;
            if arguments.iter().any(|argument| argument == "--register") {
                tokio::runtime::Runtime::new()?.block_on(async {
                    let mut client = littlehorse::ClientConfig::from_env()?.connect().await?;
                    for index in 0..5 {
                        for prefix in ["task", "branch"] { client.put_task_def(PutTaskDefRequest { name: format!("{prefix}-{index}"), return_type: Some(ReturnType { return_type: None }), ..Default::default() }).await?; }
                        client.put_external_event_def(PutExternalEventDefRequest { name: format!("evt-{index}"), ..Default::default() }).await?;
                    }
                    client.put_wf_spec(request.clone()).await?; Ok::<_, ClientError>(())
                })?;
            }
            serialize_workflow(&request)?
        }
        _ => return Err("expected list, compile, compile-all, registrations, registrations-all, convert, convert-batch, or fuzz".into()),
    };
    println!("{}", serde_json::to_string(&output)?);
    Ok(())
}
fn main() {
    if let Err(error) = run(&std::env::args().skip(1).collect::<Vec<_>>()) {
        eprintln!("{error}");
        std::process::exit(1);
    }
}
