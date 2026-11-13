use littlehorse::Variable;
use littlehorse::{proto::*, workflow::primitive_type, *};
use std::collections::{BTreeMap, HashMap};
fn record(cases: &mut BTreeMap<String, PutWfSpecRequest>, case: &str, workflow: Workflow) {
    assert!(
        cases
            .insert(
                case.into(),
                workflow.build().expect("valid conformance recipe")
            )
            .is_none(),
        "duplicate recipe {case}"
    );
}
pub fn feature_workflows() -> BTreeMap<String, PutWfSpecRequest> {
    let mut cases = BTreeMap::new();
    java_primitive_and_collection_declarations(&mut cases);
    java_variable_modifiers(&mut cases);
    java_task_and_event_options(&mut cases);
    java_arithmetic_and_mutations(&mut cases);
    java_comparison_and_logical_expressions(&mut cases);
    java_control_flow(&mut cases);
    java_cast_format_and_nested_paths(&mut cases);
    java_collection_expressions_and_paths(&mut cases);
    java_conditional_chains_and_waits(&mut cases);
    java_parallel_threads_and_children(&mut cases);
    java_interrupts_and_failure_handlers(&mut cases);
    java_user_task_actions(&mut cases);
    java_workflow_defaults_and_retention(&mut cases);
    cases
}
fn java_primitive_and_collection_declarations(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    let declarations = [
        ("declare-str", VariableType::Str),
        ("declare-int", VariableType::Int),
        ("declare-double", VariableType::Double),
        ("declare-bool", VariableType::Bool),
        ("declare-bytes", VariableType::Bytes),
        ("declare-timestamp", VariableType::Timestamp),
        ("declare-json-obj", VariableType::JsonObj),
        ("declare-json-arr", VariableType::JsonArr),
        ("add-variable", VariableType::Str),
    ];
    for (case, kind) in declarations {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                thread.declare(Variable::primitive("v", kind));
                Ok(())
            })
            .unwrap(),
        );
    }
    for (case, definition) in [
        ("declare-array", Vec::<String>::type_definition()),
        ("declare-map", HashMap::<String, String>::type_definition()),
        (
            "declare-struct",
            TypeDefinition {
                defined_type: Some(type_definition::DefinedType::StructDefId(StructDefId {
                    name: "customer".into(),
                    version: -1,
                })),
                masked: false,
            },
        ),
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                thread.declare(Variable::new("v", definition));
                Ok(())
            })
            .unwrap(),
        );
    }
}
fn java_variable_modifiers(cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>) {
    for case in [
        "var-required",
        "var-searchable",
        "var-masked",
        "var-as-public",
        "var-with-access-level",
        "var-as-inherited",
        "var-with-default",
        "var-searchable-on",
    ] {
        let variable = match case {
            "var-required" => Variable::typed::<String>("v").required(),
            "var-searchable" => Variable::typed::<String>("v").searchable(),
            "var-masked" => Variable::typed::<String>("v").masked(),
            "var-as-public" | "var-with-access-level" => {
                Variable::typed::<String>("v").access(WfRunVariableAccessLevel::PublicVar)
            }
            "var-as-inherited" => {
                Variable::typed::<String>("v").access(WfRunVariableAccessLevel::InheritedVar)
            }
            "var-with-default" => {
                Variable::typed::<i64>("v").default_value(42_i64.to_lh().unwrap())
            }
            _ => Variable::primitive("v", VariableType::JsonObj)
                .index("$.customerId", VariableType::Str),
        };
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                thread.declare(variable);
                Ok(())
            })
            .unwrap(),
        );
    }
}
fn java_task_and_event_options(cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>) {
    for case in [
        "workflow-minimal",
        "execute-args",
        "task-with-retries",
        "task-timeout",
        "task-exponential-backoff",
        "sleep-seconds",
        "wait-for-event",
        "external-event-timeout",
        "external-event-correlation",
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                match case {
                    "workflow-minimal" => {}
                    "execute-args" => {
                        let name = thread.declare_str("name");
                        thread.execute("greet", [name.expression()]);
                    }
                    "task-with-retries" => {
                        thread.execute_with(TaskOptions::new("noop", []).retries(3));
                    }
                    "task-timeout" => {
                        thread.execute_with(TaskOptions::new("noop", []).timeout(30));
                    }
                    "task-exponential-backoff" => {
                        thread.execute_with(TaskOptions::new("noop", []).backoff(
                            ExponentialBackoffRetryPolicy {
                                base_interval_ms: 1000,
                                multiplier: 2.0,
                                max_delay_ms: 60000,
                            },
                        ));
                    }
                    "sleep-seconds" => {
                        thread.sleep(30.into());
                    }
                    "wait-for-event" => {
                        thread.wait_for_event(EventOptions::new("payment-received"));
                    }
                    "external-event-timeout" => {
                        thread.wait_for_event(
                            EventOptions::new("payment-received").timeout(3600.into()),
                        );
                    }
                    _ => {
                        let id = thread.declare_str("order-id");
                        thread.wait_for_event(
                            EventOptions::new("payment-received")
                                .correlation(id.expression(), false),
                        );
                    }
                }
                Ok(())
            })
            .unwrap(),
        );
    }
}
fn java_arithmetic_and_mutations(cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>) {
    for prefix in ["expr", "wt"] {
        for (operation, rhs) in [
            ("add", 1),
            ("subtract", 1),
            ("multiply", 2),
            ("divide", 2),
            ("pow", 2),
        ] {
            let case = format!("{prefix}-{operation}");
            record(
                cases,
                &case,
                Workflow::define(format!("probe-{case}"), |thread| {
                    let count = thread.declare_int("count");
                    thread.execute("noop", []);
                    let value = match operation {
                        "add" => count.expression().plus(rhs),
                        "subtract" => count.expression().minus(rhs),
                        "multiply" => count.expression().times(rhs),
                        "divide" => count.expression().divided_by(rhs),
                        _ => count.expression().pow(rhs),
                    };
                    thread.assign(&count, value);
                    Ok(())
                })
                .unwrap(),
            );
        }
    }
    record(
        cases,
        "var-assign",
        Workflow::define("probe-var-assign", |thread| {
            let count = thread.declare_int("count");
            thread.execute("noop", []);
            thread.assign(&count, 5.into());
            Ok(())
        })
        .unwrap(),
    );
    record(
        cases,
        "mutate",
        Workflow::define("probe-mutate", |thread| {
            let count = thread.declare_int("count");
            thread.execute("noop", []);
            thread.mutate(&count, VariableMutationType::Add, 5.into());
            Ok(())
        })
        .unwrap(),
    );
}
fn java_comparison_and_logical_expressions(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    for case in [
        "expr-is-equal-to",
        "expr-is-not-equal-to",
        "expr-is-greater-than",
        "expr-is-less-than",
        "var-is-greater-than-eq",
        "var-is-less-than-eq",
        "expr-and",
        "expr-or",
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let variable = thread.declare_int("a");
                thread.execute("noop", []);
                let value = variable.expression();
                let condition = match case {
                    "expr-is-equal-to" => value.equals(5),
                    "expr-is-not-equal-to" => value.not_equals(5),
                    "expr-is-greater-than" => value.greater_than(5),
                    "expr-is-less-than" => value.less_than(5),
                    "var-is-greater-than-eq" => value.greater_or_equal(5),
                    "var-is-less-than-eq" => value.less_or_equal(5),
                    "expr-and" => value.clone().greater_than(0).and(value.less_than(10)),
                    _ => value.clone().less_than(0).or(value.greater_than(10)),
                };
                thread.wait_for_condition(condition);
                Ok(())
            })
            .unwrap(),
        );
    }
}
fn java_control_flow(cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>) {
    record(
        cases,
        "do-if-else",
        Workflow::define("probe-do-if-else", |thread| {
            let value = thread.declare_int("a");
            thread.if_else(
                value.expression().greater_than(5),
                |thread| {
                    thread.execute("yes", []);
                    Ok(())
                },
                |thread| {
                    thread.execute("no", []);
                    Ok(())
                },
            )
        })
        .unwrap(),
    );
    record(
        cases,
        "do-while",
        Workflow::define("probe-do-while", |thread| {
            let value = thread.declare_int("remaining");
            thread.while_loop(value.expression().greater_than(0), |thread| {
                thread.execute("process-one", []);
                Ok(())
            })
        })
        .unwrap(),
    );
    record(
        cases,
        "fail",
        Workflow::define("probe-fail", |thread| {
            thread.execute("noop", []);
            thread.fail("business-problem", "Something went wrong", None);
            Ok(())
        })
        .unwrap(),
    );
    record(
        cases,
        "wt-complete",
        Workflow::define("probe-wt-complete", |thread| {
            thread.execute("noop", []);
            thread.return_value("done".into());
            Ok(())
        })
        .unwrap(),
    );
}
fn java_cast_format_and_nested_paths(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    record(
        cases,
        "format",
        Workflow::define("probe-format", |thread| {
            let name = thread.declare_str("name");
            let label = thread.declare_str("label");
            thread.execute("noop", []);
            thread.assign(
                &label,
                Expression::format("Hello {0}".into(), [name.expression()]),
            );
            Ok(())
        })
        .unwrap(),
    );
    for (case, kind, source, target, source_kind, target_kind) in [
        (
            "expr-cast-to",
            VariableType::Str,
            "v",
            "s",
            VariableType::Int,
            VariableType::Str,
        ),
        (
            "expr-cast-to-int",
            VariableType::Int,
            "s",
            "v",
            VariableType::Str,
            VariableType::Int,
        ),
        (
            "expr-cast-to-double",
            VariableType::Double,
            "s",
            "v",
            VariableType::Str,
            VariableType::Double,
        ),
        (
            "expr-cast-to-str",
            VariableType::Str,
            "n",
            "s",
            VariableType::Int,
            VariableType::Str,
        ),
        (
            "expr-cast-to-bool",
            VariableType::Bool,
            "s",
            "b",
            VariableType::Str,
            VariableType::Bool,
        ),
        (
            "expr-cast-to-bytes",
            VariableType::Bytes,
            "s",
            "b",
            VariableType::Str,
            VariableType::Bytes,
        ),
        (
            "expr-cast-to-wf-run-id",
            VariableType::WfRunId,
            "s",
            "w",
            VariableType::Str,
            VariableType::Str,
        ),
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let source = thread.declare(Variable::primitive(source, source_kind));
                let target = thread.declare(Variable::primitive(target, target_kind));
                thread.execute("noop", []);
                thread.assign(&target, source.expression().cast(primitive_type(kind)));
                Ok(())
            })
            .unwrap(),
        );
    }
}

fn java_collection_expressions_and_paths(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    for prefix in ["expr", "wt"] {
        for operation in ["extend", "remove-if-present", "remove-index", "remove-key"] {
            let case = format!("{prefix}-{operation}");
            record(
                cases,
                &case,
                Workflow::define(format!("probe-{case}"), |thread| {
                    let (name, kind) = if operation == "remove-key" {
                        ("obj", VariableType::JsonObj)
                    } else {
                        ("items", VariableType::JsonArr)
                    };
                    let variable = thread.declare(Variable::primitive(name, kind));
                    thread.execute("noop", []);
                    let value = match operation {
                        "extend" => variable.expression().extend("x"),
                        "remove-if-present" => variable.expression().remove_if_present("x"),
                        "remove-index" => variable.expression().remove_index(0),
                        _ => variable.expression().remove_key("k"),
                    };
                    thread.assign(&variable, value);
                    Ok(())
                })
                .unwrap(),
            );
        }
    }
    record(
        cases,
        "expr-size",
        Workflow::define("probe-expr-size", |thread| {
            let items = thread.declare(Variable::primitive("items", VariableType::JsonArr));
            let count = thread.declare_int("count");
            thread.execute("noop", []);
            thread.assign(&count, items.expression().size());
            Ok(())
        })
        .unwrap(),
    );
    for case in [
        "expr-is-in",
        "expr-is-not-in",
        "expr-does-contain",
        "expr-does-not-contain",
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let condition = if case.contains("does") {
                    let items = thread.declare(Variable::primitive("items", VariableType::JsonArr));
                    if case == "expr-does-contain" {
                        Expression::from("x").is_in(items.expression())
                    } else {
                        Expression::from("x").not_in(items.expression())
                    }
                } else {
                    let a = thread.declare_str("a");
                    let items = thread.declare(Variable::primitive(
                        if case == "expr-is-in" {
                            "allowed"
                        } else {
                            "blocked"
                        },
                        VariableType::JsonArr,
                    ));
                    if case == "expr-is-in" {
                        a.expression().is_in(items.expression())
                    } else {
                        a.expression().not_in(items.expression())
                    }
                };
                thread.execute("noop", []);
                thread.wait_for_condition(condition);
                Ok(())
            })
            .unwrap(),
        );
    }
    for case in [
        "var-get-field",
        "var-json-path",
        "node-output-get",
        "node-output-json-path",
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                if case.starts_with("node") {
                    let variable = thread.declare(Variable::primitive("v", VariableType::JsonObj));
                    let output = thread.execute("noop", []);
                    thread.assign(
                        &variable,
                        if case.ends_with("get") {
                            output.expression().field("total")
                        } else {
                            output.expression().json_path("$.total")
                        },
                    );
                } else {
                    let variable = thread.declare(Variable::primitive("v", VariableType::JsonObj));
                    thread.execute(
                        "noop",
                        [if case == "var-get-field" {
                            variable.expression().field("field")
                        } else {
                            variable.expression().json_path("$.field")
                        }],
                    );
                }
                Ok(())
            })
            .unwrap(),
        );
    }
    for case in ["build-struct", "build-inline-struct"] {
        record(cases, case, Workflow::define(format!("probe-{case}"), |thread| {
            let variable = thread.declare(Variable::new("v", TypeDefinition { defined_type: Some(type_definition::DefinedType::StructDefId(StructDefId { name: "customer".into(), version: -1 })), masked: false }));
            thread.execute("noop", []);
            let fields = if case == "build-struct" { HashMap::from([("name".into(), InlineStructFieldValue { struct_value: Some(inline_struct_field_value::StructValue::SimpleValue(Expression::from("n").0)) })]) }
            else { HashMap::from([("nested".into(), InlineStructFieldValue { struct_value: Some(inline_struct_field_value::StructValue::SubStructure(InlineStructBuilder { fields: HashMap::from([("k".into(), InlineStructFieldValue { struct_value: Some(inline_struct_field_value::StructValue::SimpleValue(Expression::from("x").0)) })]) })) })]) };
            thread.assign(&variable, Expression::struct_value("customer", -1, fields)); Ok(())
        }).unwrap());
    }
}
fn java_conditional_chains_and_waits(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    record(
        cases,
        "do-if",
        Workflow::define("probe-do-if", |thread| {
            let value = thread.declare_int("a");
            thread.execute("noop", []);
            thread.do_if(value.expression().greater_than(5), |thread| {
                thread.execute("branch", []);
                Ok(())
            })
        })
        .unwrap(),
    );
    for case in ["do-else", "do-else-if"] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let value = thread.declare_int("a");
                let branch = thread.if_branch(value.expression().greater_than(10), |thread| {
                    thread.execute("big", []);
                    Ok(())
                })?;
                if case == "do-else" {
                    thread.otherwise(&branch, |thread| {
                        thread.execute("small", []);
                        Ok(())
                    })?;
                } else {
                    thread.else_if(&branch, value.expression().greater_than(5), |thread| {
                        thread.execute("medium", []);
                        Ok(())
                    })?;
                }
                Ok(())
            })
            .unwrap(),
        );
    }
    record(
        cases,
        "sleep-until",
        Workflow::define("probe-sleep-until", |thread| {
            let variable = thread.declare(Variable::primitive("wake-at", VariableType::Timestamp));
            thread.sleep_until(variable.expression());
            Ok(())
        })
        .unwrap(),
    );
    record(
        cases,
        "wait-for-condition",
        Workflow::define("probe-wait-for-condition", |thread| {
            let ready = thread.declare_bool("ready");
            thread.wait_for_condition(ready.expression().equals(true));
            Ok(())
        })
        .unwrap(),
    );
    record(
        cases,
        "throw-event",
        Workflow::define("probe-throw-event", |thread| {
            let payload = thread.declare_str("payload");
            thread.throw_event("milestone", payload.expression());
            Ok(())
        })
        .unwrap(),
    );
}
fn java_parallel_threads_and_children(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    for case in [
        "spawn-thread-input-vars",
        "wait-for-threads",
        "wait-for-any-of",
        "wait-for-first-of",
        "spawned-thread-number",
        "spawned-threads-of",
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let amount = if case == "spawn-thread-input-vars" {
                    Some(thread.declare_int("amount"))
                } else {
                    None
                };
                let number = if case == "spawned-thread-number" {
                    Some(thread.declare_int("num"))
                } else {
                    None
                };
                if case == "spawned-threads-of" {
                    let first = thread.spawn("child-a", HashMap::new(), |thread| {
                        thread.execute("child-a-task", []);
                        Ok(())
                    })?;
                    let second = thread.spawn("child-b", HashMap::new(), |thread| {
                        thread.execute("child-b-task", []);
                        Ok(())
                    })?;
                    thread.wait_for_threads(&[first, second], WaitForThreadsStrategy::WaitForAll);
                } else {
                    let child = thread.spawn(
                        "child",
                        amount
                            .map(|amount| HashMap::from([("budget".into(), amount.expression())]))
                            .unwrap_or_default(),
                        |thread| {
                            thread.execute("child-task", []);
                            Ok(())
                        },
                    )?;
                    match case {
                        "spawn-thread-input-vars" => {}
                        "spawned-thread-number" => {
                            thread.assign(&number.unwrap(), child.expression())
                        }
                        _ => {
                            thread.wait_for_threads(
                                &[child],
                                match case {
                                    "wait-for-any-of" => WaitForThreadsStrategy::WaitForAny,
                                    "wait-for-first-of" => WaitForThreadsStrategy::WaitForFirst,
                                    _ => WaitForThreadsStrategy::WaitForAll,
                                },
                            );
                        }
                    }
                }
                Ok(())
            })
            .unwrap(),
        );
    }
    record(
        cases,
        "spawn-thread-for-each",
        Workflow::define("probe-spawn-thread-for-each", |thread| {
            let items = thread.declare(Variable::primitive("items", VariableType::JsonArr));
            thread.for_each("processor", items.expression(), HashMap::new(), |thread| {
                thread.execute("process-one", []);
                Ok(())
            })?;
            Ok(())
        })
        .unwrap(),
    );
    for case in ["run-wf-inputs", "wait-for-child-wf"] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let inputs = if case == "run-wf-inputs" {
                    let id = thread.declare_str("order-id");
                    HashMap::from([("order-id".into(), id.expression())])
                } else {
                    HashMap::new()
                };
                let child = thread.run_workflow("child-wf", -1, inputs, None);
                if case == "wait-for-child-wf" {
                    thread.wait_for_child(&child);
                }
                Ok(())
            })
            .unwrap(),
        );
    }
}
fn java_interrupts_and_failure_handlers(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    use littlehorse::dsl::FailureFilter;
    record(
        cases,
        "interrupt-handler",
        Workflow::define("probe-interrupt-handler", |thread| {
            thread.execute("main-step", []);
            thread.interrupt("cancel-requested", "interrupt-cancel-requested", |thread| {
                thread.execute("cancel", []);
                Ok(())
            })
        })
        .unwrap(),
    );
    for (case, task, cleanup, filter) in [
        (
            "handle-error-any",
            "risky-task",
            "cleanup",
            FailureFilter::Error(None),
        ),
        (
            "handle-exception-named",
            "flaky-task",
            "reorder",
            FailureFilter::Exception(Some("out-of-stock".into())),
        ),
        (
            "handle-any-failure",
            "fragile-task",
            "cleanup-any",
            FailureFilter::Any,
        ),
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let output = thread.execute(task, []);
                thread.handle(&output, filter, |thread| {
                    thread.execute(cleanup, []);
                    Ok(())
                })
            })
            .unwrap(),
        );
    }
    for (case, cleanup, filter) in [
        (
            "wait-threads-handle-error-on-child",
            "cleanup",
            FailureFilter::Error(Some(LhErrorType::ChildFailure)),
        ),
        (
            "wait-threads-handle-exception-on-child",
            "reorder",
            FailureFilter::Exception(Some("out-of-stock".into())),
        ),
        (
            "wait-threads-handle-any-failure-on-child",
            "cleanup-any",
            FailureFilter::Any,
        ),
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let child = thread.spawn("child", HashMap::new(), |thread| {
                    thread.execute("child-task", []);
                    Ok(())
                })?;
                let wait = thread.wait_for_threads(&[child], WaitForThreadsStrategy::WaitForAll);
                thread.handle_child_failure(&wait, filter, |thread| {
                    thread.execute(cleanup, []);
                    Ok(())
                })
            })
            .unwrap(),
        );
    }
}
fn java_user_task_actions(cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>) {
    use littlehorse::schema::*;
    for case in [
        "assign-user-task",
        "user-task-notes",
        "user-task-on-cancellation-exception",
        "release-to-group-on-deadline",
        "reassign-user-task",
        "schedule-reminder-task",
        "schedule-reminder-on-assignment",
        "cancel-user-task-run-after",
        "cancel-user-task-after-assignment",
    ] {
        record(
            cases,
            case,
            Workflow::define(format!("probe-{case}"), |thread| {
                let output = thread.user_task(
                    "approve-request",
                    Some("alice".into()),
                    Some("approvers".into()),
                    if case == "user-task-notes" {
                        Some("Please review".into())
                    } else {
                        None
                    },
                );
                let arrival = ut_action_trigger::UtHook::OnArrival;
                let assigned = ut_action_trigger::UtHook::OnTaskAssigned;
                let action = match case {
                    "release-to-group-on-deadline" => Some(reassign_user_task(
                        300.into(),
                        assigned,
                        None,
                        Some("approvers".into()),
                    )),
                    "reassign-user-task" => Some(reassign_user_task(
                        300.into(),
                        assigned,
                        Some("bob".into()),
                        Some("approvers".into()),
                    )),
                    "schedule-reminder-task" => Some(remind_user_task(
                        60.into(),
                        arrival,
                        TaskOptions::new("send-reminder", []),
                    )),
                    "schedule-reminder-on-assignment" => Some(remind_user_task(
                        60.into(),
                        assigned,
                        TaskOptions::new("send-reminder", []),
                    )),
                    "cancel-user-task-run-after" => Some(cancel_user_task(86400.into(), arrival)),
                    "cancel-user-task-after-assignment" => {
                        Some(cancel_user_task(86400.into(), assigned))
                    }
                    _ => None,
                };
                if let Some(action) = action {
                    thread.user_task_action(&output, action)?;
                }
                if case == "user-task-on-cancellation-exception" {
                    thread.user_task_cancellation_exception(&output, "cancelled".into())?;
                }
                Ok(())
            })
            .unwrap(),
        );
    }
}
fn java_workflow_defaults_and_retention(
    cases: &mut std::collections::BTreeMap<String, PutWfSpecRequest>,
) {
    for case in [
        "wf-update-type",
        "wf-set-parent",
        "wf-retention-policy",
        "wf-default-thread-retention",
        "wf-default-task-timeout",
        "wf-default-task-retries",
        "wf-default-task-backoff",
        "wt-retention-policy",
    ] {
        let workflow = Workflow::define(format!("probe-{case}"), |thread| {
            thread.execute("noop", []);
            if case == "wt-retention-policy" {
                thread.retention(ThreadRetentionPolicy {
                    thread_gc_policy: Some(
                        thread_retention_policy::ThreadGcPolicy::SecondsAfterThreadTermination(600),
                    ),
                });
            }
            Ok(())
        })
        .unwrap();
        let workflow = match case {
            "wf-update-type" => workflow.updates(AllowedUpdateType::NoUpdates),
            "wf-set-parent" => workflow.parent("parent-wf", 0),
            "wf-retention-policy" => workflow.retention(WorkflowRetentionPolicy {
                wf_gc_policy: Some(
                    workflow_retention_policy::WfGcPolicy::SecondsAfterWfTermination(3600),
                ),
            }),
            "wf-default-thread-retention" => {
                workflow.default_thread_retention(ThreadRetentionPolicy {
                    thread_gc_policy: Some(
                        thread_retention_policy::ThreadGcPolicy::SecondsAfterThreadTermination(600),
                    ),
                })
            }
            "wf-default-task-timeout" => workflow.default_task_options(45, 0, None),
            "wf-default-task-retries" => workflow.default_task_options(0, 2, None),
            "wf-default-task-backoff" => workflow.default_task_options(
                0,
                0,
                Some(ExponentialBackoffRetryPolicy {
                    base_interval_ms: 1000,
                    multiplier: 2.0,
                    max_delay_ms: 60000,
                }),
            ),
            _ => workflow,
        };
        record(cases, case, workflow);
    }
}
