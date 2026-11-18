use littlehorse::{proto::*, schema::Registrations, *};
use std::collections::HashMap;
pub const CASES: &[&str] = &[
    "reg-external-event",
    "reg-correlated-event-config",
    "reg-workflow-event",
    "reg-interrupt-event-type",
    "req-task-def-names",
    "req-external-event-def-names",
    "req-child-wf-spec-names",
    "req-workflow-event-def-names",
];
pub fn answer(case: &str, feature: bool) -> Result<serde_json::Value, client::ClientError> {
    let mut registrations = Registrations::default();
    let workflow = Workflow::define(format!("probe-{case}"), |thread| {
        match case {
            "reg-external-event" | "reg-correlated-event-config" => {
                thread.wait_for_event(EventOptions::new("payment-received"));
                if feature || case == "reg-correlated-event-config" {
                    let config = if feature && case == "reg-correlated-event-config" {
                        Some(CorrelatedEventConfig {
                            ttl_seconds: Some(3600),
                            delete_after_first_correlation: true,
                        })
                    } else {
                        None
                    };
                    registrations
                        .external_events
                        .push(PutExternalEventDefRequest {
                            name: "payment-received".into(),
                            content_type: Some(ReturnType {
                                return_type: Some(String::type_definition()),
                            }),
                            correlated_event_config: config,
                            ..Default::default()
                        });
                }
            }
            "reg-workflow-event" => {
                let payload = thread.declare_str("payload");
                thread.throw_event("milestone", payload.expression());
                if feature {
                    registrations
                        .workflow_events
                        .push(PutWorkflowEventDefRequest {
                            name: "milestone".into(),
                            content_type: Some(ReturnType {
                                return_type: Some(String::type_definition()),
                            }),
                        });
                }
            }
            "reg-interrupt-event-type" => {
                thread.execute("main-step", []);
                thread.interrupt(
                    "cancel-requested",
                    "interrupt-cancel-requested",
                    |handler| {
                        handler.execute("cancel", []);
                        Ok(())
                    },
                )?;
                if feature {
                    registrations
                        .external_events
                        .push(PutExternalEventDefRequest {
                            name: "cancel-requested".into(),
                            content_type: Some(ReturnType {
                                return_type: Some(String::type_definition()),
                            }),
                            ..Default::default()
                        });
                }
            }
            "req-task-def-names" => {
                thread.declare_str("v");
                if feature {
                    thread.execute("noop", []);
                }
            }
            "req-external-event-def-names" => {
                thread.execute("noop", []);
                if feature {
                    thread.wait_for_event(EventOptions::new("payment-received"));
                }
            }
            "req-child-wf-spec-names" => {
                thread.execute("noop", []);
                if feature {
                    thread.run_workflow("child-wf", -1, HashMap::new(), None);
                }
            }
            "req-workflow-event-def-names" => {
                let payload = thread.declare_str("payload");
                thread.execute("noop", []);
                if feature {
                    thread.throw_event("milestone", payload.expression());
                }
            }
            _ => {
                return Err(WorkflowError::InvalidName(format!(
                    "unknown registration case {case}"
                )));
            }
        }
        Ok(())
    })?;
    workflow.compile()?;
    let dependencies = workflow.dependencies();
    let external: Vec<_> = registrations
        .external_events
        .iter()
        .map(|request| {
            json::message_to_json_value("littlehorse.PutExternalEventDefRequest", request)
        })
        .collect::<Result<_, _>>()?;
    let events: Vec<_> = registrations
        .workflow_events
        .iter()
        .map(|request| {
            json::message_to_json_value("littlehorse.PutWorkflowEventDefRequest", request)
        })
        .collect::<Result<_, _>>()?;
    Ok(
        serde_json::json!({ "externalEventDefs": external, "workflowEventDefs": events, "requiredTaskDefNames": dependencies.tasks, "requiredExternalEventDefNames": dependencies.external_events, "requiredChildWfSpecNames": dependencies.child_workflows, "requiredWorkflowEventDefNames": dependencies.workflow_events }),
    )
}
