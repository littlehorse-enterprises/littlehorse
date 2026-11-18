use littlehorse::Variable;
use littlehorse::{proto::*, *};
use std::collections::HashMap;

pub fn base_workflow(
    case: &str,
) -> Result<PutWfSpecRequest, Box<dyn std::error::Error + Send + Sync>> {
    let workflow = Workflow::define(format!("probe-{case}"), |thread| {
        match case {
            "workflow-minimal" => {
                return Err(WorkflowError::InvalidName(
                    "workflow-minimal has no base variant".into(),
                ));
            }
            case if case.starts_with("declare-")
                || case == "add-variable"
                || case == "sleep-seconds"
                || case == "wait-for-event"
                || case == "assign-user-task" => {}
            "var-with-default" => {
                thread.declare_int("v");
            }
            "var-searchable-on" => {
                thread.declare(Variable::primitive("v", VariableType::JsonObj));
            }
            "var-json-path" | "var-get-field" => {
                let variable = thread.declare(Variable::primitive("v", VariableType::JsonObj));
                thread.execute("noop", [variable.expression()]);
            }
            "var-assign" | "mutate" => {
                thread.declare_int("count");
                thread.execute("noop", []);
            }
            case if case.starts_with("var-") && !case.contains("than-eq") => {
                thread.declare_str("v");
            }
            "execute-args" => {
                thread.declare_str("name");
                thread.execute("greet", []);
            }
            case if case.starts_with("task-")
                || case.starts_with("wf-")
                || case == "wt-retention-policy"
                || case == "wt-complete"
                || case == "fail" =>
            {
                thread.execute("noop", []);
            }
            "node-output-json-path" | "node-output-get" => {
                thread.declare(Variable::primitive("v", VariableType::JsonObj));
                thread.execute("noop", []);
            }
            "do-if" => {
                thread.declare_int("a");
                thread.execute("noop", []);
            }
            "do-if-else" => {
                thread.declare_int("a");
            }
            "do-else" | "do-else-if" => {
                let variable = thread.declare_int("a");
                thread.do_if(variable.expression().greater_than(10), |thread| {
                    thread.execute("big", []);
                    Ok(())
                })?;
            }
            "do-while" => {
                thread.declare_int("remaining");
            }
            "sleep-until" => {
                thread.declare(Variable::primitive("wake-at", VariableType::Timestamp));
            }
            "wait-for-condition" => {
                thread.declare_bool("ready");
            }
            "throw-event" => {
                thread.declare_str("payload");
            }
            "external-event-timeout" => {
                thread.wait_for_event(EventOptions::new("payment-received"));
            }
            "external-event-correlation" => {
                thread.declare_str("order-id");
                thread.wait_for_event(EventOptions::new("payment-received"));
            }
            "format" => {
                thread.declare_str("name");
                thread.declare_str("label");
                thread.execute("noop", []);
            }
            "build-struct" | "build-inline-struct" => {
                thread.declare(Variable::new(
                    "v",
                    TypeDefinition {
                        defined_type: Some(type_definition::DefinedType::StructDefId(
                            StructDefId {
                                name: "customer".into(),
                                version: -1,
                            },
                        )),
                        masked: false,
                    },
                ));
                thread.execute("noop", []);
            }
            "spawn-thread-input-vars" => {
                thread.declare_int("amount");
                thread.spawn("child", HashMap::new(), |child| {
                    child.execute("child-task", []);
                    Ok(())
                })?;
            }
            "spawn-thread-for-each" => {
                thread.declare(Variable::primitive("items", VariableType::JsonArr));
            }
            "wait-for-threads" | "wait-for-any-of" | "wait-for-first-of" => {
                thread.spawn("child", HashMap::new(), |child| {
                    child.execute("child-task", []);
                    Ok(())
                })?;
            }
            "spawned-thread-number" => {
                thread.declare_int("num");
                thread.spawn("child", HashMap::new(), |child| {
                    child.execute("child-task", []);
                    Ok(())
                })?;
            }
            "spawned-threads-of" => {
                let first = thread.spawn("child-a", HashMap::new(), |child| {
                    child.execute("child-a-task", []);
                    Ok(())
                })?;
                thread.spawn("child-b", HashMap::new(), |child| {
                    child.execute("child-b-task", []);
                    Ok(())
                })?;
                thread.wait_for_threads(&[first], WaitForThreadsStrategy::WaitForAll);
            }
            case if case.starts_with("wait-threads-handle-") => {
                let child = thread.spawn("child", HashMap::new(), |child| {
                    child.execute("child-task", []);
                    Ok(())
                })?;
                thread.wait_for_threads(&[child], WaitForThreadsStrategy::WaitForAll);
            }
            "run-wf-inputs" => {
                thread.declare_str("order-id");
                thread.run_workflow("child-wf", -1, HashMap::new(), None);
            }
            "wait-for-child-wf" => {
                thread.run_workflow("child-wf", -1, HashMap::new(), None);
            }
            "interrupt-handler" => {
                thread.execute("main-step", []);
            }
            "handle-error-any" => {
                thread.execute("risky-task", []);
            }
            "handle-exception-named" => {
                thread.execute("flaky-task", []);
            }
            "handle-any-failure" => {
                thread.execute("fragile-task", []);
            }
            case if case.starts_with("user-task-")
                || case.starts_with("release-to-")
                || case.starts_with("reassign-")
                || case.starts_with("schedule-reminder")
                || case.starts_with("cancel-user-task") =>
            {
                thread.user_task(
                    "approve-request",
                    Some("alice".into()),
                    Some("approvers".into()),
                    None,
                );
            }
            case if case.starts_with("expr-")
                || case.starts_with("wt-")
                || case.contains("than-eq") =>
            {
                expression_declarations(thread, case);
            }
            _ => {
                return Err(WorkflowError::InvalidName(format!(
                    "unknown base recipe {case}"
                )));
            }
        }
        Ok(())
    })?;
    Ok(workflow.build()?)
}
fn expression_declarations(thread: &mut ThreadBuilder, case: &str) {
    if case == "expr-size" {
        thread.declare(Variable::primitive("items", VariableType::JsonArr));
        thread.declare_int("count");
    } else if case.contains("cast-to") {
        let (source, source_type, target, target_type) = match case {
            "expr-cast-to" => ("v", VariableType::Int, "s", VariableType::Str),
            "expr-cast-to-str" => ("n", VariableType::Int, "s", VariableType::Str),
            "expr-cast-to-int" => ("s", VariableType::Str, "v", VariableType::Int),
            "expr-cast-to-double" => ("s", VariableType::Str, "v", VariableType::Double),
            "expr-cast-to-bool" => ("s", VariableType::Str, "b", VariableType::Bool),
            "expr-cast-to-bytes" => ("s", VariableType::Str, "b", VariableType::Bytes),
            _ => ("s", VariableType::Str, "w", VariableType::Str),
        };
        thread.declare(Variable::primitive(source, source_type));
        thread.declare(Variable::primitive(target, target_type));
    } else if case.ends_with("remove-key") {
        thread.declare(Variable::primitive("obj", VariableType::JsonObj));
    } else if case.contains("extend") || case.contains("remove-") || case.contains("does-") {
        thread.declare(Variable::primitive("items", VariableType::JsonArr));
    } else if case == "expr-is-in" || case == "expr-is-not-in" {
        thread.declare_str("a");
        thread.declare(Variable::primitive(
            if case == "expr-is-in" {
                "allowed"
            } else {
                "blocked"
            },
            VariableType::JsonArr,
        ));
    } else if case.contains("is-")
        || case.ends_with("-and")
        || case.ends_with("-or")
        || case.contains("than-eq")
    {
        thread.declare_int("a");
    } else {
        thread.declare_int("count");
    }
    thread.execute("noop", []);
}
