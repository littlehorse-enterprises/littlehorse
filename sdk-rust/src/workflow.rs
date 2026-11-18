//! Workflow graphs with explicit node names, checked edges, and composable expressions.
use crate::{
    proto::*,
    value::{ValueError, to_value},
};
use std::collections::{HashMap, HashSet};

#[derive(Debug, thiserror::Error)]
pub enum WorkflowError {
    #[error("duplicate or empty name: {0}")]
    InvalidName(String),
    #[error("unknown node: {0}")]
    UnknownNode(String),
    #[error("thread {0} must have exactly one entrypoint and at least one exit")]
    InvalidThread(String),
    #[error("unreachable node: {0}")]
    UnreachableNode(String),
    #[error("non-exit node has no outgoing edges: {0}")]
    DeadEnd(String),
    #[error("exit node has outgoing edges: {0}")]
    ExitEdges(String),
    #[error("node has no operation: {0}")]
    MissingOperation(String),
}
#[derive(Clone, Debug)]
pub struct Expression(pub VariableAssignment);
impl Expression {
    pub fn literal(value: &impl serde::Serialize) -> Result<Self, ValueError> {
        Ok(Self(VariableAssignment {
            source: Some(variable_assignment::Source::LiteralValue(to_value(value)?)),
            ..Default::default()
        }))
    }
    pub fn variable(name: impl Into<String>) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::VariableName(name.into())),
            ..Default::default()
        })
    }
    pub fn output(node_name: impl Into<String>) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::NodeOutput(
                variable_assignment::NodeOutputReference {
                    node_name: node_name.into(),
                },
            )),
            ..Default::default()
        })
    }
    pub fn compare(self, comparator: Comparator, rhs: Self) -> Self {
        self.binary(
            variable_assignment::expression::Operation::Comparator(comparator as i32),
            rhs,
        )
    }
    pub fn operation(self, operation: VariableMutationType, rhs: Self) -> Self {
        self.binary(
            variable_assignment::expression::Operation::MutationType(operation as i32),
            rhs,
        )
    }
    fn binary(self, operation: variable_assignment::expression::Operation, rhs: Self) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::Expression(Box::new(
                variable_assignment::Expression {
                    lhs: Some(Box::new(self.0)),
                    rhs: Some(Box::new(rhs.0)),
                    operation: Some(operation),
                },
            ))),
            ..Default::default()
        })
    }
    pub fn cast(mut self, target_type: TypeDefinition) -> Self {
        self.0.target_type = Some(target_type);
        self
    }
    pub fn path(mut self, path: LhPath) -> Self {
        self.0.path = Some(variable_assignment::Path::LhPath(path));
        self
    }
}
/// A graph builder. All protobuf node variants, handlers, and mutations are supported
/// through `add_node`, `node_mut`, and `connect` without losing wire-level options.
pub struct Workflow {
    request: PutWfSpecRequest,
}
impl Workflow {
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            request: PutWfSpecRequest {
                name: name.into(),
                entrypoint_thread_name: "entrypoint".into(),
                ..Default::default()
            },
        }
    }
    pub fn add_thread(
        &mut self,
        name: impl Into<String>,
        thread: ThreadSpec,
    ) -> Result<&mut Self, WorkflowError> {
        let name = name.into();
        if name.is_empty() || self.request.thread_specs.contains_key(&name) {
            return Err(WorkflowError::InvalidName(name));
        }
        self.request.thread_specs.insert(name, thread);
        Ok(self)
    }
    pub fn add_node(
        &mut self,
        thread: &str,
        name: impl Into<String>,
        operation: node::Node,
    ) -> Result<&mut Self, WorkflowError> {
        let name = name.into();
        let thread = self.request.thread_specs.entry(thread.into()).or_default();
        if name.is_empty() || thread.nodes.contains_key(&name) {
            return Err(WorkflowError::InvalidName(name));
        }
        thread.nodes.insert(
            name,
            Node {
                node: Some(operation),
                ..Default::default()
            },
        );
        Ok(self)
    }
    pub fn node_mut(&mut self, thread: &str, name: &str) -> Result<&mut Node, WorkflowError> {
        self.request
            .thread_specs
            .get_mut(thread)
            .and_then(|thread| thread.nodes.get_mut(name))
            .ok_or_else(|| WorkflowError::UnknownNode(name.into()))
    }
    pub fn connect(
        &mut self,
        thread: &str,
        source: &str,
        sink: &str,
        condition: Option<Expression>,
        mutations: Vec<VariableMutation>,
    ) -> Result<&mut Self, WorkflowError> {
        if !self
            .request
            .thread_specs
            .get(thread)
            .is_some_and(|thread| thread.nodes.contains_key(sink))
        {
            return Err(WorkflowError::UnknownNode(sink.into()));
        }
        self.node_mut(thread, source)?.outgoing_edges.push(Edge {
            sink_node_name: sink.into(),
            variable_mutations: mutations,
            edge_condition: condition.map(|condition| edge::EdgeCondition::Condition(condition.0)),
        });
        Ok(self)
    }
    /// Access retention, parent workflow, variable definitions, interrupts, and other schema options.
    pub fn request_mut(&mut self) -> &mut PutWfSpecRequest {
        &mut self.request
    }
    pub fn build(self) -> Result<PutWfSpecRequest, WorkflowError> {
        if self.request.name.is_empty() {
            return Err(WorkflowError::InvalidName(self.request.name));
        }
        if !self
            .request
            .thread_specs
            .contains_key(&self.request.entrypoint_thread_name)
        {
            return Err(WorkflowError::InvalidThread(
                self.request.entrypoint_thread_name,
            ));
        }
        for (thread_name, thread) in &self.request.thread_specs {
            validate_thread(thread_name, thread)?;
        }
        Ok(self.request)
    }
}
fn validate_thread(name: &str, thread: &ThreadSpec) -> Result<(), WorkflowError> {
    let entrypoints: Vec<_> = thread
        .nodes
        .iter()
        .filter(|(_, node)| matches!(node.node, Some(node::Node::Entrypoint(_))))
        .map(|(name, _)| name)
        .collect();
    if entrypoints.len() != 1
        || !thread
            .nodes
            .values()
            .any(|node| matches!(node.node, Some(node::Node::Exit(_))))
    {
        return Err(WorkflowError::InvalidThread(name.into()));
    }
    let mut variable_names = HashSet::new();
    for variable in &thread.variable_defs {
        let name = variable
            .var_def
            .as_ref()
            .map(|definition| definition.name.as_str())
            .unwrap_or("");
        if name.is_empty() || !variable_names.insert(name) {
            return Err(WorkflowError::InvalidName(name.into()));
        }
    }
    for (name, node) in &thread.nodes {
        if node.node.is_none() {
            return Err(WorkflowError::MissingOperation(name.clone()));
        }
        if matches!(node.node, Some(node::Node::Exit(_))) {
            if !node.outgoing_edges.is_empty() {
                return Err(WorkflowError::ExitEdges(name.clone()));
            }
        } else if node.outgoing_edges.is_empty() {
            return Err(WorkflowError::DeadEnd(name.clone()));
        }
        for edge in &node.outgoing_edges {
            if !thread.nodes.contains_key(&edge.sink_node_name) {
                return Err(WorkflowError::UnknownNode(edge.sink_node_name.clone()));
            }
        }
    }
    let mut visited = HashSet::new();
    let mut pending = entrypoints;
    while let Some(name) = pending.pop() {
        if visited.insert(name) {
            pending.extend(
                thread.nodes[name]
                    .outgoing_edges
                    .iter()
                    .map(|edge| &edge.sink_node_name),
            );
        }
    }
    for name in thread.nodes.keys() {
        if !visited.contains(name) {
            return Err(WorkflowError::UnreachableNode(name.clone()));
        }
    }
    Ok(())
}
/// Build the common primitive type definition without deprecated fields.
pub fn primitive_type(variable_type: VariableType) -> TypeDefinition {
    TypeDefinition {
        defined_type: Some(type_definition::DefinedType::PrimitiveType(
            variable_type as i32,
        )),
        masked: false,
    }
}
/// Build task inputs in declaration order.
pub fn task(name: impl Into<String>, inputs: impl IntoIterator<Item = Expression>) -> node::Node {
    node::Node::Task(TaskNode {
        task_to_execute: Some(task_node::TaskToExecute::TaskDefId(TaskDefId {
            name: name.into(),
        })),
        timeout_seconds: 0,
        variables: inputs.into_iter().map(|input| input.0).collect(),
        ..Default::default()
    })
}
/// Construct a thread from explicitly named nodes. Edges can be added afterwards.
pub fn thread(nodes: impl IntoIterator<Item = (String, Node)>) -> ThreadSpec {
    ThreadSpec {
        nodes: nodes.into_iter().collect::<HashMap<_, _>>(),
        ..Default::default()
    }
}

impl Expression {
    pub fn native<T: crate::types::LittleHorseValue>(
        value: &T,
    ) -> Result<Self, crate::types::TypeError> {
        Ok(Self(VariableAssignment {
            source: Some(variable_assignment::Source::LiteralValue(value.to_lh()?)),
            ..Default::default()
        }))
    }
    pub fn negate(mut self) -> Self {
        if let Some(variable_assignment::Source::Expression(expression)) = &mut self.0.source
            && let Some(variable_assignment::expression::Operation::Comparator(comparator)) =
                &mut expression.operation
        {
            *comparator = match Comparator::try_from(*comparator).expect("known comparator") {
                Comparator::Equals => Comparator::NotEquals,
                Comparator::NotEquals => Comparator::Equals,
                Comparator::LessThan => Comparator::GreaterThanEq,
                Comparator::GreaterThanEq => Comparator::LessThan,
                Comparator::GreaterThan => Comparator::LessThanEq,
                Comparator::LessThanEq => Comparator::GreaterThan,
                Comparator::In => Comparator::NotIn,
                Comparator::NotIn => Comparator::In,
            } as i32;
            return self;
        }
        self.compare(Comparator::Equals, Self::from(false))
    }
    pub fn size(self) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::SizeOf(Box::new(
                variable_assignment::SizeOf {
                    operand: Some(Box::new(self.0)),
                },
            ))),
            ..Default::default()
        })
    }
    pub fn format(template: Expression, args: impl IntoIterator<Item = Expression>) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::FormatString(Box::new(
                variable_assignment::FormatString {
                    format: Some(Box::new(template.0)),
                    args: args.into_iter().map(|value| value.0).collect(),
                },
            ))),
            ..Default::default()
        })
    }
    pub fn field(mut self, field: impl Into<String>) -> Self {
        let selector = lh_path::Selector {
            selector_type: Some(lh_path::selector::SelectorType::Key(field.into())),
        };
        match &mut self.0.path {
            Some(variable_assignment::Path::LhPath(path)) => path.path.push(selector),
            _ => {
                self.0.path = Some(variable_assignment::Path::LhPath(LhPath {
                    path: vec![selector],
                }))
            }
        }
        self
    }
    pub fn index(mut self, index: i32) -> Self {
        let selector = lh_path::Selector {
            selector_type: Some(lh_path::selector::SelectorType::Index(index)),
        };
        match &mut self.0.path {
            Some(variable_assignment::Path::LhPath(path)) => path.path.push(selector),
            _ => {
                self.0.path = Some(variable_assignment::Path::LhPath(LhPath {
                    path: vec![selector],
                }))
            }
        }
        self
    }
    pub fn json_path(mut self, path: impl Into<String>) -> Self {
        self.0.path = Some(variable_assignment::Path::JsonPath(path.into()));
        self
    }
    pub fn struct_value(
        name: impl Into<String>,
        version: i32,
        fields: HashMap<String, InlineStructFieldValue>,
    ) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::StructBuilder(StructBuilder {
                struct_def_id: Some(StructDefId {
                    name: name.into(),
                    version,
                }),
                value: Some(InlineStructBuilder { fields }),
            })),
            ..Default::default()
        })
    }
    pub fn map(
        entries: impl IntoIterator<Item = (Expression, Expression)>,
        map_type: InlineMapDef,
    ) -> Self {
        Self(VariableAssignment {
            source: Some(variable_assignment::Source::MapBuilder(MapBuilder {
                entries: entries
                    .into_iter()
                    .map(|(key, value)| map_builder::Entry {
                        key: Some(key.0),
                        value: Some(value.0),
                    })
                    .collect(),
                map_type: Some(map_type),
            })),
            ..Default::default()
        })
    }
}
impl From<i64> for Expression {
    fn from(value: i64) -> Self {
        Self::native(&value).expect("primitive value is always representable")
    }
}
impl From<i32> for Expression {
    fn from(value: i32) -> Self {
        Self::native(&value).expect("primitive value is always representable")
    }
}
impl From<bool> for Expression {
    fn from(value: bool) -> Self {
        Self::native(&value).expect("primitive value is always representable")
    }
}
impl From<String> for Expression {
    fn from(value: String) -> Self {
        Self::native(&value).expect("primitive value is always representable")
    }
}
impl From<&str> for Expression {
    fn from(value: &str) -> Self {
        Self::from(value.to_owned())
    }
}
impl Expression {
    pub fn plus(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Add, rhs.into())
    }
}
impl Expression {
    pub fn minus(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Subtract, rhs.into())
    }
}
impl Expression {
    pub fn times(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Multiply, rhs.into())
    }
}
impl Expression {
    pub fn divided_by(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Divide, rhs.into())
    }
}
impl Expression {
    pub fn pow(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Pow, rhs.into())
    }
}
impl Expression {
    pub fn extend(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Extend, rhs.into())
    }
}
impl Expression {
    pub fn and(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::And, rhs.into())
    }
}
impl Expression {
    pub fn or(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::Or, rhs.into())
    }
}
impl Expression {
    pub fn remove_if_present(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::RemoveIfPresent, rhs.into())
    }
}
impl Expression {
    pub fn remove_index(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::RemoveIndex, rhs.into())
    }
}
impl Expression {
    pub fn remove_key(self, rhs: impl Into<Expression>) -> Self {
        self.operation(VariableMutationType::RemoveKey, rhs.into())
    }
}
impl Expression {
    pub fn equals(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::Equals, rhs.into())
    }
}
impl Expression {
    pub fn not_equals(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::NotEquals, rhs.into())
    }
}
impl Expression {
    pub fn less_than(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::LessThan, rhs.into())
    }
}
impl Expression {
    pub fn greater_than(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::GreaterThan, rhs.into())
    }
}
impl Expression {
    pub fn less_or_equal(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::LessThanEq, rhs.into())
    }
}
impl Expression {
    pub fn greater_or_equal(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::GreaterThanEq, rhs.into())
    }
}
impl Expression {
    pub fn is_in(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::In, rhs.into())
    }
}
impl Expression {
    pub fn not_in(self, rhs: impl Into<Expression>) -> Self {
        self.compare(Comparator::NotIn, rhs.into())
    }
}

/// Metadata names referenced by a workflow. Dynamic references are resolved by the server at runtime.
#[derive(Debug, Default, PartialEq, Eq)]
pub struct Dependencies {
    pub tasks: std::collections::BTreeSet<String>,
    pub external_events: std::collections::BTreeSet<String>,
    pub workflow_events: std::collections::BTreeSet<String>,
    pub child_workflows: std::collections::BTreeSet<String>,
}
impl Workflow {
    pub fn name(&self) -> &str {
        &self.request.name
    }
    pub fn compile(&self) -> Result<PutWfSpecRequest, WorkflowError> {
        Self {
            request: self.request.clone(),
        }
        .build()
    }
    pub fn to_json(&self) -> Result<String, crate::client::ClientError> {
        crate::json::workflow_to_json(&self.compile()?)
    }
    pub fn save(
        &self,
        path: impl AsRef<std::path::Path>,
    ) -> Result<(), crate::client::ClientError> {
        std::fs::write(path, self.to_json()?)?;
        Ok(())
    }
    pub async fn exists(
        &self,
        client: &mut crate::Client,
        major_version: Option<i32>,
    ) -> Result<bool, tonic::Status> {
        match client
            .get_latest_wf_spec(GetLatestWfSpecRequest {
                name: self.name().into(),
                major_version,
            })
            .await
        {
            Ok(_) => Ok(true),
            Err(error) if error.code() == tonic::Code::NotFound => Ok(false),
            Err(error) => Err(error),
        }
    }
    pub async fn register(
        &self,
        client: &mut crate::Client,
    ) -> Result<WfSpec, crate::client::ClientError> {
        let mut request = self.compile()?;
        crate::schema::pin_user_task_versions(client, &mut request).await?;
        Ok(client.put_wf_spec(request).await?.into_inner())
    }
    pub fn dependencies(&self) -> Dependencies {
        let mut dependencies = Dependencies::default();
        for thread in self.request.thread_specs.values() {
            for interrupt in &thread.interrupt_defs {
                if let Some(id) = &interrupt.external_event_def_id {
                    dependencies.external_events.insert(id.name.clone());
                }
            }
            for node in thread.nodes.values() {
                match &node.node {
                    Some(node::Node::Task(task)) => {
                        if let Some(task_node::TaskToExecute::TaskDefId(id)) = &task.task_to_execute
                        {
                            dependencies.tasks.insert(id.name.clone());
                        }
                    }
                    Some(node::Node::ExternalEvent(event)) => {
                        if let Some(id) = &event.external_event_def_id {
                            dependencies.external_events.insert(id.name.clone());
                        }
                    }
                    Some(node::Node::ThrowEvent(event)) => {
                        if let Some(id) = &event.event_def_id {
                            dependencies.workflow_events.insert(id.name.clone());
                        }
                    }
                    Some(node::Node::RunChildWf(child)) => {
                        if let Some(run_child_wf_node::WfSpec::WfSpecName(name)) = &child.wf_spec {
                            dependencies.child_workflows.insert(name.clone());
                        }
                    }
                    Some(node::Node::UserTask(user_task)) => {
                        for action in &user_task.actions {
                            if let Some(ut_action_trigger::Action::Task(task)) = &action.action
                                && let Some(task_node::TaskToExecute::TaskDefId(id)) = task
                                    .task
                                    .as_ref()
                                    .and_then(|task| task.task_to_execute.as_ref())
                            {
                                dependencies.tasks.insert(id.name.clone());
                            }
                        }
                    }
                    _ => {}
                }
            }
        }
        dependencies
    }
    pub fn default_thread_retention(mut self, policy: ThreadRetentionPolicy) -> Self {
        for thread in self.request.thread_specs.values_mut() {
            if thread.retention_policy.is_none() {
                thread.retention_policy = Some(policy);
            }
        }
        self
    }
    pub fn default_task_options(
        mut self,
        timeout: i32,
        retries: i32,
        backoff: Option<ExponentialBackoffRetryPolicy>,
    ) -> Self {
        for thread in self.request.thread_specs.values_mut() {
            for node in thread.nodes.values_mut() {
                if let Some(node::Node::Task(task)) = &mut node.node {
                    if task.timeout_seconds == 0 {
                        task.timeout_seconds = timeout;
                    }
                    if task.retries == 0 {
                        task.retries = retries;
                    }
                    if task.exponential_backoff.is_none() {
                        task.exponential_backoff = backoff;
                    }
                }
            }
        }
        self
    }
}
