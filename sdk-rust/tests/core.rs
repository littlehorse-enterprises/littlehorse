use littlehorse::{Expression, Workflow, WorkflowError, from_value, proto::*, to_value};
use prost::Message;
use serde_json::json;

#[test]
fn values_round_trip_without_losing_integers() {
    let integer = i64::MAX;
    assert_eq!(
        from_value::<i64>(&to_value(&integer).unwrap()).unwrap(),
        integer
    );
    assert!(to_value(&u64::MAX).is_err());
    for value in [
        json!(null),
        json!(true),
        json!("hello"),
        json!(1.25),
        json!([1, "two"]),
        json!({"nested": [false]}),
    ] {
        assert_eq!(
            from_value::<serde_json::Value>(&to_value(&value).unwrap()).unwrap(),
            value
        );
    }
}
fn graph() -> Workflow {
    let mut workflow = Workflow::new("greet");
    workflow
        .add_node(
            "entrypoint",
            "start",
            node::Node::Entrypoint(EntrypointNode {}),
        )
        .unwrap();
    workflow
        .add_node(
            "entrypoint",
            "greet",
            littlehorse::workflow::task("greet", [Expression::literal(&"world").unwrap()]),
        )
        .unwrap();
    workflow
        .add_node(
            "entrypoint",
            "end",
            node::Node::Exit(ExitNode {
                result: Some(exit_node::Result::ReturnContent(
                    Expression::output("greet").0,
                )),
            }),
        )
        .unwrap();
    workflow
        .connect("entrypoint", "start", "greet", None, vec![])
        .unwrap();
    workflow
        .connect("entrypoint", "greet", "end", None, vec![])
        .unwrap();
    workflow
}
#[test]
fn workflow_wire_round_trip() {
    let request = graph().build().unwrap();
    assert_eq!(
        PutWfSpecRequest::decode(request.encode_to_vec().as_slice()).unwrap(),
        request
    );
}
#[test]
fn duplicate_nodes_do_not_replace_existing_nodes() {
    let mut workflow = graph();
    assert!(matches!(
        workflow.add_node("entrypoint", "greet", node::Node::Nop(NopNode {})),
        Err(WorkflowError::InvalidName(_))
    ));
    assert!(matches!(
        workflow.build().unwrap().thread_specs["entrypoint"].nodes["greet"].node,
        Some(node::Node::Task(_))
    ));
}
#[test]
fn rejects_dangling_edges_and_unreachable_nodes() {
    let mut workflow = graph();
    workflow
        .node_mut("entrypoint", "start")
        .unwrap()
        .outgoing_edges[0]
        .sink_node_name = "missing".into();
    assert!(matches!(
        workflow.build(),
        Err(WorkflowError::UnknownNode(_))
    ));
    let mut workflow = graph();
    workflow
        .add_node(
            "entrypoint",
            "unreachable",
            node::Node::Exit(ExitNode::default()),
        )
        .unwrap();
    assert!(matches!(
        workflow.build(),
        Err(WorkflowError::UnreachableNode(_))
    ));
}
#[test]
fn expressions_preserve_operators() {
    let expression = Expression::variable("count")
        .compare(Comparator::LessThan, Expression::literal(&10).unwrap());
    let Some(variable_assignment::Source::Expression(expression)) = expression.0.source else {
        panic!("missing expression")
    };
    assert_eq!(
        expression.operation,
        Some(variable_assignment::expression::Operation::Comparator(
            Comparator::LessThan as i32
        ))
    );
}
#[test]
fn credentials_are_redacted() {
    let config = littlehorse::ClientConfig {
        bearer_token: Some("secret-token".into()),
        ..Default::default()
    };
    assert!(!format!("{config:?}").contains("secret-token"));
}

#[test]
fn mutation_only_branches_keep_mutations_on_the_matching_edge() {
    let workflow = Workflow::define("conditional-mutation", |thread| {
        let count = thread.declare_int("count");
        thread.if_else(
            count.expression().equals(3),
            |thread| {
                thread.assign(&count, 103.into());
                Ok(())
            },
            |thread| {
                thread.fail("bad-count", "unexpected count", None);
                Ok(())
            },
        )?;
        thread.return_value(count.expression());
        Ok(())
    })
    .unwrap()
    .build()
    .unwrap();
    let branch = &workflow.thread_specs["entrypoint"].nodes["1-nop-NOP"];
    assert_eq!(branch.outgoing_edges[0].variable_mutations.len(), 1);
    assert!(branch.outgoing_edges[0].edge_condition.is_some());
    assert!(branch.outgoing_edges[1].variable_mutations.is_empty());
}

#[test]
fn exhaustive_conditional_chain_with_returns_compiles() {
    let request = Workflow::define("exhaustive-return", |thread| {
        let condition = thread.declare_bool("condition");
        let branch = thread.if_branch(condition.expression(), |thread| {
            thread.return_value("yes".into());
            Ok(())
        })?;
        thread.else_if(&branch, condition.expression().negate(), |thread| {
            thread.return_value("maybe".into());
            Ok(())
        })?;
        thread.otherwise(&branch, |thread| {
            thread.return_value("no".into());
            Ok(())
        })
    })
    .unwrap()
    .build()
    .unwrap();
    let thread = &request.thread_specs["entrypoint"];
    assert_eq!(
        thread
            .nodes
            .values()
            .filter(|node| matches!(node.node, Some(node::Node::Exit(_))))
            .count(),
        3
    );
}
