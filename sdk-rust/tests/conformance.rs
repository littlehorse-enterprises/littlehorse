//! Check independent Rust testee answers against the shared (or vendored) upstream corpus.
#![cfg(feature = "conformance")]
use std::{
    collections::BTreeSet,
    io::Write,
    path::PathBuf,
    process::{Command, Stdio},
};
fn corpus() -> PathBuf {
    if let Some(path) = std::env::var_os("LITTLEHORSE_CONFORMANCE_DIR") {
        return path.into();
    }
    let root = PathBuf::from(env!("CARGO_MANIFEST_DIR"));
    if root.join("../sdk-conformance/areas").is_dir() {
        root.join("../sdk-conformance/areas")
    } else {
        root.join("conformance/areas")
    }
}
fn answer(arguments: &[&str]) -> serde_json::Value {
    let output = Command::new(env!("CARGO_BIN_EXE_lh-conformance"))
        .args(arguments)
        .output()
        .unwrap();
    assert!(
        output.status.success(),
        "testee failed: {}",
        String::from_utf8_lossy(&output.stderr)
    );
    serde_json::from_slice(&output.stdout).unwrap()
}
fn normalize(value: &mut serde_json::Value) {
    match value {
        serde_json::Value::Object(fields) => {
            fields.retain(|_, value| !value.is_null());
            for value in fields.values_mut() {
                normalize(value);
            }
        }
        serde_json::Value::Array(values) => {
            for value in values {
                normalize(value);
            }
        }
        _ => {}
    }
}
fn compare(mut expected: serde_json::Value, mut actual: serde_json::Value, case: &str) {
    normalize(&mut expected);
    normalize(&mut actual);
    assert_eq!(actual, expected, "conformance case {case}");
}
#[test]
fn every_upstream_workflow_and_registration_variant_matches() {
    for (area, command) in [
        ("wfsdk", "compile-all"),
        ("registrations", "registrations-all"),
    ] {
        let answers = answer(&[command]);
        let root = corpus().join(area);
        let manifest: serde_json::Value =
            serde_json::from_slice(&std::fs::read(root.join("manifest.json")).unwrap()).unwrap();
        let mut expected_keys = BTreeSet::new();
        for case in manifest["cases"].as_array().unwrap() {
            let id = case["id"].as_str().unwrap();
            for variant in case["variants"].as_array().unwrap() {
                let variant = variant.as_str().unwrap();
                let key = format!("{id}/{variant}");
                let expected = serde_json::from_slice(
                    &std::fs::read(root.join("cases").join(id).join(format!("{variant}.json")))
                        .unwrap(),
                )
                .unwrap();
                compare(expected, answers[&key].clone(), &key);
                expected_keys.insert(key);
            }
        }
        assert_eq!(
            answers
                .as_object()
                .unwrap()
                .keys()
                .cloned()
                .collect::<BTreeSet<_>>(),
            expected_keys
        );
    }
}
#[test]
fn every_upstream_serialization_case_matches() {
    let root = corpus().join("serde");
    let manifest: serde_json::Value =
        serde_json::from_slice(&std::fs::read(root.join("manifest.json")).unwrap()).unwrap();
    let mut child = Command::new(env!("CARGO_BIN_EXE_lh-conformance"))
        .arg("convert-batch")
        .stdin(Stdio::piped())
        .stdout(Stdio::piped())
        .spawn()
        .unwrap();
    let mut input = child.stdin.take().unwrap();
    for case in manifest["cases"].as_array().unwrap() {
        writeln!(input, "{}", serde_json::json!({"id": case["id"], "type": case["input"]["type"], "value": case["input"]["value"]})).unwrap();
    }
    drop(input);
    let output = child.wait_with_output().unwrap();
    assert!(output.status.success());
    let answers: serde_json::Value = serde_json::from_slice(&output.stdout).unwrap();
    for case in manifest["cases"].as_array().unwrap() {
        let id = case["id"].as_str().unwrap();
        let expected = serde_json::from_slice(
            &std::fs::read(root.join("cases").join(format!("{id}.json"))).unwrap(),
        )
        .unwrap();
        compare(expected, answers[id].clone(), id);
    }
}
