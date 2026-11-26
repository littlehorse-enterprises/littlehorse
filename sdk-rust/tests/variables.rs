use littlehorse::{Workflow, WorkflowError, dsl::FailureFilter, proto::*};
use std::collections::HashMap;

fn variable_defs(workflow: &Workflow, thread: &str) -> Vec<ThreadVarDef> {
    workflow.compile().unwrap().thread_specs[thread]
        .variable_defs
        .clone()
}

#[test]
fn options_set_after_declaring_apply_to_the_compiled_spec() {
    let workflow = Workflow::define("late-options", |thread| {
        let name = thread.declare_str("name").with_default("world");
        thread.execute("greet", [name.expression()]);
        name.searchable(); // Go style: configure after declaring.
        let count = thread.declare_int("count").with_default(3);
        count.required();
        Ok(())
    })
    .unwrap();
    let defs = variable_defs(&workflow, "entrypoint");
    assert_eq!(defs.len(), 2);
    let name = &defs[0];
    assert!(name.searchable);
    assert_eq!(
        name.var_def.as_ref().unwrap().default_value,
        Some(VariableValue {
            value: Some(variable_value::Value::Str("world".into())),
        })
    );
    let count = &defs[1];
    assert!(count.required);
    assert_eq!(
        count.var_def.as_ref().unwrap().default_value,
        Some(VariableValue {
            value: Some(variable_value::Value::Int(3)),
        })
    );
}

#[test]
fn unconvertible_default_is_reported_by_define() {
    let error = Workflow::define("bad-default", |thread| {
        thread.declare_double("ratio").with_default(f64::NAN);
        Ok(())
    })
    .err()
    .unwrap();
    assert!(matches!(error, WorkflowError::InvalidDefault(name, _) if name == "ratio"));
}

#[test]
fn parent_variable_used_in_child_thread_is_declared_only_in_parent() {
    let workflow = Workflow::define("parent-child", |thread| {
        let result = thread.declare_str("result");
        let child = thread.spawn("child", HashMap::new(), |child| {
            child.assign(&result, "done");
            Ok(())
        });
        result.searchable();
        thread.wait_for_threads(&[child], WaitForThreadsStrategy::WaitForAll);
        Ok(())
    })
    .unwrap();
    let parent = variable_defs(&workflow, "entrypoint");
    let result = parent
        .iter()
        .find(|def| def.var_def.as_ref().unwrap().name == "result")
        .unwrap();
    assert!(result.searchable);
    assert!(
        variable_defs(&workflow, "child")
            .iter()
            .all(|def| def.var_def.as_ref().unwrap().name != "result")
    );
}

#[test]
fn duplicate_declarations_are_rejected() {
    let error = Workflow::define("duplicate", |thread| {
        thread.declare_str("name");
        thread.declare_int("name");
        Ok(())
    })
    .err()
    .unwrap();
    assert!(matches!(error, WorkflowError::InvalidName(name) if name == "name"));
}

/// Every mutation in a thread, as (variable, operation) pairs.
fn mutations(workflow: &Workflow, thread: &str) -> Vec<(String, i32)> {
    workflow.compile().unwrap().thread_specs[thread]
        .nodes
        .values()
        .flat_map(|node| &node.outgoing_edges)
        .flat_map(|edge| &edge.variable_mutations)
        .map(|mutation| (mutation.lhs_name.clone(), mutation.operation))
        .collect()
}

#[test]
fn variable_assign_targets_the_thread_being_built() {
    let workflow = Workflow::define("variable-assign", |wf| {
        let result = wf.declare_str("result");
        let count = wf.declare_int("count").with_default(0);
        let child = wf.spawn("child", HashMap::new(), |_| {
            result.assign("from child");
        });
        wf.wait_for_threads(&[child], WaitForThreadsStrategy::WaitForAll);
        wf.do_if(count.less_than(10), |_| {
            count.mutate(VariableMutationType::Add, 1);
        });
        wf.return_value(&result);
    })
    .unwrap();
    let assign = VariableMutationType::Assign as i32;
    let add = VariableMutationType::Add as i32;
    assert_eq!(
        mutations(&workflow, "child"),
        vec![("result".into(), assign)]
    );
    assert!(mutations(&workflow, "entrypoint").contains(&("count".into(), add)));
    assert!(!mutations(&workflow, "entrypoint").contains(&("result".into(), assign)));
}

#[test]
fn mutating_an_undeclared_variable_is_reported() {
    let stray = littlehorse::Variable::typed::<String>("stray");
    let error = Workflow::define("undeclared", |wf| {
        stray.assign("value");
        wf.declare(stray.clone());
    })
    .err()
    .unwrap();
    assert!(matches!(error, WorkflowError::UndeclaredVariable(name) if name == "stray"));
}

#[test]
fn task_inputs_accept_variables_tuples_and_nothing() {
    let workflow = Workflow::define("inputs", |wf| {
        let name = wf.declare_str("name");
        let greeting = wf.execute("greet", &name);
        wf.execute("notify", (&name, &greeting, "literal", 3));
        wf.execute("ping", ());
    })
    .unwrap();
    let request = workflow.compile().unwrap();
    let mut inputs: Vec<(String, usize)> = request.thread_specs["entrypoint"]
        .nodes
        .values()
        .filter_map(|node| match &node.node {
            Some(node::Node::Task(task)) => match &task.task_to_execute {
                Some(task_node::TaskToExecute::TaskDefId(id)) => {
                    Some((id.name.clone(), task.variables.len()))
                }
                _ => None,
            },
            _ => None,
        })
        .collect();
    inputs.sort();
    assert_eq!(
        inputs,
        vec![
            ("greet".into(), 1),
            ("notify".into(), 4),
            ("ping".into(), 0)
        ]
    );
}

#[test]
fn errors_returned_from_bodies_are_reported_by_define() {
    let error = Workflow::define("body-error", |wf| {
        wf.do_if(true, |_| Err(WorkflowError::InvalidName("boom".into())));
    })
    .err()
    .unwrap();
    assert!(matches!(error, WorkflowError::InvalidName(name) if name == "boom"));
}

#[test]
fn named_failure_handlers_match_the_filter_forms() {
    let named = Workflow::define("handlers", |wf| {
        let task = wf.execute("risky", ());
        wf.handle_error(&task, LhErrorType::Timeout, |_| ());
        wf.handle_exception(&task, "declined", |_| ());
        wf.handle_any_failure(&task, |_| ());
    })
    .unwrap();
    let filters = Workflow::define("handlers", |wf| {
        let task = wf.execute("risky", ());
        wf.handle(
            &task,
            FailureFilter::Error(Some(LhErrorType::Timeout)),
            |_| (),
        );
        wf.handle(
            &task,
            FailureFilter::Exception(Some("declined".into())),
            |_| (),
        );
        wf.handle(&task, FailureFilter::Any, |_| ());
    })
    .unwrap();
    assert_eq!(named.compile().unwrap(), filters.compile().unwrap());
}

#[test]
fn user_task_helpers_set_assignment_and_notes() {
    let workflow = Workflow::define("user-task", |wf| {
        let reviewer = wf.declare_str("reviewer");
        wf.assign_user_task_to_user("review", &reviewer)
            .with_notes("Approve this request?");
        wf.assign_user_task_to_group("triage", "support");
    })
    .unwrap();
    let request = workflow.compile().unwrap();
    let mut tasks: Vec<UserTaskNode> = request.thread_specs["entrypoint"]
        .nodes
        .values()
        .filter_map(|node| match &node.node {
            Some(node::Node::UserTask(task)) => Some(task.clone()),
            _ => None,
        })
        .collect();
    tasks.sort_by(|a, b| a.user_task_def_name.cmp(&b.user_task_def_name));
    let [review, triage] = &tasks[..] else {
        panic!("expected two user tasks")
    };
    assert!(review.user_id.is_some() && review.user_group.is_none());
    assert!(review.notes.is_some());
    assert!(triage.user_id.is_none() && triage.user_group.is_some());
}

#[test]
fn chained_node_options_match_the_options_structs() {
    use littlehorse::{EventOptions, TaskOptions};
    let chained = Workflow::define("options", |wf| {
        let key = wf.declare_str("key");
        let task = wf.execute("risky", &key).with_retries(3).timeout(10);
        wf.wait_for_event("paid")
            .timeout(60)
            .with_correlation_id(&key);
        // Options can also be set later on a stored output, as in the other SDKs.
        task.with_retries(4);
    })
    .unwrap();
    let structs = Workflow::define("options", |wf| {
        let key = wf.declare_str("key");
        wf.execute_with(TaskOptions::new("risky", &key).retries(4).timeout(10));
        wf.wait_for_event(
            EventOptions::new("paid")
                .timeout(60)
                .correlation(&key, false),
        );
    })
    .unwrap();
    assert_eq!(chained.compile().unwrap(), structs.compile().unwrap());
}
