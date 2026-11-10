//! Sequential, conditional, parallel, and event-driven workflow construction.
use crate::{
    proto::*,
    types::LittleHorseValue,
    workflow::{Expression, Workflow, WorkflowError, primitive_type},
};
use std::collections::HashMap;

#[derive(Clone, Debug)]
pub struct Variable {
    pub name: String,
    pub definition: ThreadVarDef,
}
impl Variable {
    pub fn new(name: impl Into<String>, variable_type: TypeDefinition) -> Self {
        let name = name.into();
        Self {
            definition: ThreadVarDef {
                var_def: Some(VariableDef {
                    name: name.clone(),
                    type_def: Some(variable_type),
                    ..Default::default()
                }),
                access_level: WfRunVariableAccessLevel::PrivateVar as i32,
                ..Default::default()
            },
            name,
        }
    }
    pub fn typed<T: LittleHorseValue>(name: impl Into<String>) -> Self {
        Self::new(name, T::type_definition())
    }
    pub fn primitive(name: impl Into<String>, variable_type: VariableType) -> Self {
        Self::new(name, primitive_type(variable_type))
    }
    pub fn required(mut self) -> Self {
        self.definition.required = true;
        self
    }
    pub fn searchable(mut self) -> Self {
        self.definition.searchable = true;
        self
    }
    pub fn masked(mut self) -> Self {
        self.definition
            .var_def
            .as_mut()
            .unwrap()
            .type_def
            .as_mut()
            .unwrap()
            .masked = true;
        self
    }
    pub fn access(mut self, access: WfRunVariableAccessLevel) -> Self {
        self.definition.access_level = access as i32;
        self
    }
    pub fn default_value(mut self, value: VariableValue) -> Self {
        self.definition.var_def.as_mut().unwrap().default_value = Some(value);
        self
    }
    pub fn index(mut self, path: impl Into<String>, kind: VariableType) -> Self {
        self.definition.json_indexes.push(JsonIndex {
            field_path: path.into(),
            field_type: kind as i32,
        });
        self
    }
    pub fn expression(&self) -> Expression {
        Expression::variable(&self.name)
    }
}
#[derive(Clone, Debug)]
pub struct NodeOutput {
    pub name: String,
}
impl NodeOutput {
    pub fn expression(&self) -> Expression {
        if self.name.ends_with("-START_THREAD") || self.name.ends_with("-START_MULTIPLE_THREADS") {
            Expression::variable(&self.name)
        } else {
            Expression::output(&self.name)
        }
    }
}
#[derive(Clone, Debug)]
pub struct TaskOptions {
    pub task: TaskNode,
}
impl TaskOptions {
    pub fn new(name: impl Into<String>, inputs: impl IntoIterator<Item = Expression>) -> Self {
        let node::Node::Task(task) = crate::workflow::task(name, inputs) else {
            unreachable!()
        };
        Self { task }
    }
    pub fn dynamic(name: Expression, inputs: impl IntoIterator<Item = Expression>) -> Self {
        Self {
            task: TaskNode {
                task_to_execute: Some(task_node::TaskToExecute::DynamicTask(name.0)),
                timeout_seconds: 0,
                variables: inputs.into_iter().map(|input| input.0).collect(),
                ..Default::default()
            },
        }
    }
    pub fn timeout(mut self, seconds: i32) -> Self {
        self.task.timeout_seconds = seconds;
        self
    }
    pub fn retries(mut self, retries: i32) -> Self {
        self.task.retries = retries;
        self
    }
    pub fn backoff(mut self, policy: ExponentialBackoffRetryPolicy) -> Self {
        self.task.exponential_backoff = Some(policy);
        self
    }
}
#[derive(Clone, Debug)]
pub struct EventOptions {
    pub event: ExternalEventNode,
}
impl EventOptions {
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            event: ExternalEventNode {
                external_event_def_id: Some(ExternalEventDefId { name: name.into() }),
                ..Default::default()
            },
        }
    }
    pub fn timeout(mut self, seconds: Expression) -> Self {
        self.event.timeout_seconds = Some(seconds.0);
        self
    }
    pub fn correlation(mut self, key: Expression, masked: bool) -> Self {
        self.event.correlation_key = Some(key.0);
        self.event.mask_correlation_key = masked;
        self
    }
}
/// A thread builder's closures execute once at definition time, never on workers.
pub struct ThreadBuilder {
    name: String,
    spec: ThreadSpec,
    children: HashMap<String, ThreadSpec>,
    cursor: Option<String>,
    next_node: usize,
    pending_mutations: Vec<VariableMutation>,
    errors: Vec<WorkflowError>,
    pub default_task_timeout: i32,
    pub default_task_retries: i32,
    pub default_task_backoff: Option<ExponentialBackoffRetryPolicy>,
}
impl ThreadBuilder {
    pub fn new(name: impl Into<String>) -> Self {
        let mut builder = Self {
            name: name.into(),
            spec: ThreadSpec::default(),
            children: HashMap::new(),
            cursor: None,
            next_node: 0,
            pending_mutations: Vec::new(),
            errors: Vec::new(),
            default_task_timeout: 0,
            default_task_retries: 0,
            default_task_backoff: None,
        };
        builder.append(node::Node::Entrypoint(EntrypointNode {}));
        builder
    }
    fn unique_name(&mut self, prefix: &str) -> String {
        let name = format!("{}-{prefix}", self.next_node);
        self.next_node += 1;
        name
    }
    fn insert(&mut self, operation: node::Node) -> String {
        let (human, kind) = node_label(&operation);
        let name = self.unique_name(&format!("{human}-{kind}"));
        self.spec.nodes.insert(
            name.clone(),
            Node {
                node: Some(operation),
                ..Default::default()
            },
        );
        name
    }
    fn edge(&mut self, source: &str, sink: &str, condition: Option<Expression>) {
        self.spec
            .nodes
            .get_mut(source)
            .unwrap()
            .outgoing_edges
            .push(Edge {
                sink_node_name: sink.into(),
                edge_condition: condition
                    .map(|condition| edge::EdgeCondition::Condition(condition.0)),
                ..Default::default()
            });
    }
    pub fn append(&mut self, operation: node::Node) -> NodeOutput {
        if self.cursor.is_none() && !self.spec.nodes.is_empty() {
            self.errors.push(WorkflowError::UnreachableNode(
                "code after return/fail".into(),
            ));
        }
        let terminal = matches!(operation, node::Node::Exit(_));
        let name = self.insert(operation);
        if let Some(previous) = self.cursor.take() {
            self.edge(&previous, &name, None);
            let mutations = std::mem::take(&mut self.pending_mutations);
            self.spec
                .nodes
                .get_mut(&previous)
                .unwrap()
                .outgoing_edges
                .last_mut()
                .unwrap()
                .variable_mutations = mutations;
        }
        if !terminal {
            self.cursor = Some(name.clone());
        }
        if name.ends_with("-START_THREAD") || name.ends_with("-START_MULTIPLE_THREADS") {
            let kind = if name.ends_with("-START_THREAD") {
                VariableType::Int
            } else {
                VariableType::JsonArr
            };
            let variable = self.declare(Variable::primitive(&name, kind));
            self.assign(&variable, Expression::output(&name));
        }
        NodeOutput { name }
    }
    pub fn declare(&mut self, variable: Variable) -> Variable {
        if self.spec.variable_defs.iter().any(|existing| {
            existing
                .var_def
                .as_ref()
                .is_some_and(|definition| definition.name == variable.name)
        }) {
            self.errors
                .push(WorkflowError::InvalidName(variable.name.clone()));
        } else {
            self.spec.variable_defs.push(variable.definition.clone());
        }
        variable
    }
    pub fn declare_int(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::typed::<i64>(name))
    }
    pub fn declare_str(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::typed::<String>(name))
    }
    pub fn declare_bool(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::typed::<bool>(name))
    }
    pub fn execute(
        &mut self,
        name: impl Into<String>,
        inputs: impl IntoIterator<Item = Expression>,
    ) -> NodeOutput {
        let mut options = TaskOptions::new(name, inputs)
            .timeout(self.default_task_timeout)
            .retries(self.default_task_retries);
        options.task.exponential_backoff = self.default_task_backoff;
        self.execute_with(options)
    }
    pub fn execute_with(&mut self, options: TaskOptions) -> NodeOutput {
        self.append(node::Node::Task(options.task))
    }
    pub fn wait_for_event(&mut self, options: EventOptions) -> NodeOutput {
        self.append(node::Node::ExternalEvent(options.event))
    }
    pub fn throw_event(&mut self, name: impl Into<String>, content: Expression) -> NodeOutput {
        self.append(node::Node::ThrowEvent(ThrowEventNode {
            event_def_id: Some(WorkflowEventDefId { name: name.into() }),
            content: Some(content.0),
        }))
    }
    pub fn wait_for_condition(&mut self, condition: Expression) -> NodeOutput {
        self.append(node::Node::WaitForCondition(WaitForConditionNode {
            node_condition: Some(wait_for_condition_node::NodeCondition::Condition(
                condition.0,
            )),
        }))
    }
    pub fn sleep(&mut self, seconds: Expression) -> NodeOutput {
        self.append(node::Node::Sleep(SleepNode {
            sleep_length: Some(sleep_node::SleepLength::RawSeconds(seconds.0)),
        }))
    }
    pub fn sleep_until(&mut self, timestamp: Expression) -> NodeOutput {
        self.append(node::Node::Sleep(SleepNode {
            sleep_length: Some(sleep_node::SleepLength::Timestamp(timestamp.0)),
        }))
    }
    pub fn sleep_until_iso(&mut self, timestamp: Expression) -> NodeOutput {
        self.append(node::Node::Sleep(SleepNode {
            sleep_length: Some(sleep_node::SleepLength::IsoDate(timestamp.0)),
        }))
    }
    pub fn mutate(
        &mut self,
        variable: &Variable,
        operation: VariableMutationType,
        value: Expression,
    ) {
        if self.cursor.is_none() {
            self.errors.push(WorkflowError::UnreachableNode(
                "mutation after return/fail".into(),
            ));
        }
        self.pending_mutations.push(VariableMutation {
            lhs_name: variable.name.clone(),
            operation: operation as i32,
            rhs_value: Some(variable_mutation::RhsValue::RhsAssignment(value.0)),
            ..Default::default()
        });
    }
    pub fn assign(&mut self, variable: &Variable, value: Expression) {
        self.mutate(variable, VariableMutationType::Assign, value);
    }
    pub fn return_value(&mut self, value: Expression) {
        self.append(node::Node::Exit(ExitNode {
            result: Some(exit_node::Result::ReturnContent(value.0)),
        }));
    }
    pub fn complete(&mut self) {
        self.append(node::Node::Exit(ExitNode::default()));
    }
    pub fn fail(
        &mut self,
        name: impl Into<String>,
        message: impl Into<String>,
        content: Option<Expression>,
    ) {
        self.append(node::Node::Exit(ExitNode {
            result: Some(exit_node::Result::FailureDef(FailureDef {
                failure_name: name.into(),
                message: message.into(),
                content: content.map(|content| content.0),
            })),
        }));
    }
    pub fn if_else<F, G>(
        &mut self,
        condition: Expression,
        then_body: F,
        else_body: G,
    ) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
        G: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        let branch = self.append(node::Node::Nop(NopNode {})).name;
        then_body(self)?;
        let then_edge_count = self.spec.nodes[&branch].outgoing_edges.len();
        let then_end = self.cursor.take();
        let merge = self.insert(node::Node::Nop(NopNode {}));
        if let Some(end) = then_end.as_ref().filter(|end| **end != branch) {
            self.edge(end, &merge, None);
            self.spec
                .nodes
                .get_mut(end)
                .unwrap()
                .outgoing_edges
                .last_mut()
                .unwrap()
                .variable_mutations = std::mem::take(&mut self.pending_mutations);
        }
        if then_edge_count == 0 {
            self.edge(&branch, &merge, Some(condition));
            self.spec
                .nodes
                .get_mut(&branch)
                .unwrap()
                .outgoing_edges
                .last_mut()
                .unwrap()
                .variable_mutations = std::mem::take(&mut self.pending_mutations);
        } else {
            self.spec.nodes.get_mut(&branch).unwrap().outgoing_edges[0].edge_condition =
                Some(edge::EdgeCondition::Condition(condition.0));
        }
        self.cursor = Some(branch.clone());
        else_body(self)?;
        let else_end = self.cursor.take();
        if let Some(end) = &else_end {
            self.edge(end, &merge, None);
            self.spec
                .nodes
                .get_mut(end)
                .unwrap()
                .outgoing_edges
                .last_mut()
                .unwrap()
                .variable_mutations = std::mem::take(&mut self.pending_mutations);
        }
        if then_end.is_none() && else_end.is_none() {
            self.spec.nodes.remove(&merge);
            self.cursor = None;
        } else {
            self.cursor = Some(merge);
        }
        Ok(())
    }
    pub fn do_if<F>(&mut self, condition: Expression, body: F) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.if_else(condition, body, |_| Ok(()))
    }
    pub fn while_loop<F>(&mut self, condition: Expression, body: F) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        let check = self.append(node::Node::Nop(NopNode {})).name;
        body(self)?;
        let body_end = self.cursor.clone();
        let merge = self.append(node::Node::Nop(NopNode {})).name;
        if let Some(first) = self
            .spec
            .nodes
            .get_mut(&check)
            .unwrap()
            .outgoing_edges
            .first_mut()
        {
            first.edge_condition = Some(edge::EdgeCondition::Condition(condition.clone().0));
        }
        self.edge(&check, &merge, Some(condition.clone().negate()));
        self.edge(&merge, &check, Some(condition));
        if body_end.is_none() {
            self.errors.pop();
        }
        self.cursor = Some(merge);
        Ok(())
    }
    fn child<F>(&mut self, name: &str, body: F) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        if name == self.name || self.children.contains_key(name) {
            return Err(WorkflowError::InvalidName(name.into()));
        }
        let mut child = Self::new(name);
        child.default_task_timeout = self.default_task_timeout;
        child.default_task_retries = self.default_task_retries;
        child.default_task_backoff = self.default_task_backoff;
        body(&mut child)?;
        let (spec, children) = child.finish()?;
        self.children.insert(name.into(), spec);
        for (name, spec) in children {
            if name == self.name || self.children.insert(name.clone(), spec).is_some() {
                return Err(WorkflowError::InvalidName(name));
            }
        }
        Ok(())
    }
    pub fn spawn<F>(
        &mut self,
        name: &str,
        inputs: HashMap<String, Expression>,
        body: F,
    ) -> Result<NodeOutput, WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.child(name, body)?;
        Ok(self.append(node::Node::StartThread(StartThreadNode {
            thread_spec_name: name.into(),
            variables: assignments(inputs),
        })))
    }
    pub fn for_each<F>(
        &mut self,
        name: &str,
        iterable: Expression,
        inputs: HashMap<String, Expression>,
        body: F,
    ) -> Result<NodeOutput, WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.child(name, body)?;
        Ok(
            self.append(node::Node::StartMultipleThreads(StartMultipleThreadsNode {
                thread_spec_name: name.into(),
                variables: assignments(inputs),
                iterable: Some(iterable.0),
            })),
        )
    }
    pub fn wait_for_threads(
        &mut self,
        threads: &[NodeOutput],
        strategy: WaitForThreadsStrategy,
    ) -> NodeOutput {
        self.append(node::Node::WaitForThreads(WaitForThreadsNode {
            strategy: strategy as i32,
            threads_to_wait_for: Some(wait_for_threads_node::ThreadsToWaitForOneOf::Threads(
                wait_for_threads_node::ThreadsToWaitFor {
                    threads: threads
                        .iter()
                        .map(|thread| wait_for_threads_node::ThreadToWaitFor {
                            thread_run_number: Some(thread.expression().0),
                        })
                        .collect(),
                },
            )),
            ..Default::default()
        }))
    }
    pub fn wait_for_thread_list(
        &mut self,
        threads: Expression,
        strategy: WaitForThreadsStrategy,
    ) -> NodeOutput {
        self.append(node::Node::WaitForThreads(WaitForThreadsNode {
            strategy: strategy as i32,
            threads_to_wait_for: Some(wait_for_threads_node::ThreadsToWaitForOneOf::ThreadList(
                threads.0,
            )),
            ..Default::default()
        }))
    }
    pub fn run_child(
        &mut self,
        name: Expression,
        major_version: i32,
        inputs: HashMap<String, Expression>,
        id: Option<Expression>,
    ) -> NodeOutput {
        self.append(node::Node::RunChildWf(RunChildWfNode {
            wf_spec: Some(run_child_wf_node::WfSpec::WfSpecVar(name.0)),
            major_version,
            inputs: assignments(inputs),
            child_id: id.map(|id| id.0),
        }))
    }
    pub fn wait_for_child(&mut self, child: &NodeOutput) -> NodeOutput {
        self.append(node::Node::WaitForChildWf(WaitForChildWfNode {
            child_wf_run_id: Some(child.expression().0),
            child_wf_run_source_node: child.name.clone(),
        }))
    }
    pub fn interrupt<F>(
        &mut self,
        event: &str,
        handler_name: &str,
        body: F,
    ) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.child(handler_name, body)?;
        self.spec.interrupt_defs.push(InterruptDef {
            external_event_def_id: Some(ExternalEventDefId { name: event.into() }),
            handler_spec_name: handler_name.into(),
        });
        Ok(())
    }
    pub fn handle_failure<F>(
        &mut self,
        output: &NodeOutput,
        handler_name: &str,
        failure: Option<failure_handler_def::FailureToCatch>,
        body: F,
    ) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.child(handler_name, body)?;
        self.spec
            .nodes
            .get_mut(&output.name)
            .ok_or_else(|| WorkflowError::UnknownNode(output.name.clone()))?
            .failure_handlers
            .push(FailureHandlerDef {
                handler_spec_name: handler_name.into(),
                failure_to_catch: failure,
            });
        Ok(())
    }
    pub fn user_task(
        &mut self,
        name: impl Into<String>,
        user: Option<Expression>,
        group: Option<Expression>,
        notes: Option<Expression>,
    ) -> NodeOutput {
        self.append(node::Node::UserTask(UserTaskNode {
            user_task_def_name: name.into(),
            user_id: user.map(|value| value.0),
            user_group: group.map(|value| value.0),
            notes: notes.map(|value| value.0),
            ..Default::default()
        }))
    }
    pub fn user_task_action(
        &mut self,
        output: &NodeOutput,
        action: UtActionTrigger,
    ) -> Result<(), WorkflowError> {
        match &mut self
            .spec
            .nodes
            .get_mut(&output.name)
            .ok_or_else(|| WorkflowError::UnknownNode(output.name.clone()))?
            .node
        {
            Some(node::Node::UserTask(task)) => {
                task.actions.push(action);
                Ok(())
            }
            _ => Err(WorkflowError::UnknownNode(output.name.clone())),
        }
    }
    pub fn retention(&mut self, policy: ThreadRetentionPolicy) {
        self.spec.retention_policy = Some(policy);
    }
    pub fn node_mut(&mut self, output: &NodeOutput) -> Result<&mut Node, WorkflowError> {
        self.spec
            .nodes
            .get_mut(&output.name)
            .ok_or_else(|| WorkflowError::UnknownNode(output.name.clone()))
    }
    pub fn finish(mut self) -> Result<(ThreadSpec, HashMap<String, ThreadSpec>), WorkflowError> {
        if let Some(error) = self.errors.pop() {
            return Err(error);
        }
        if self.cursor.is_some() {
            self.complete();
        }
        Ok((self.spec, self.children))
    }
}
fn assignments(inputs: HashMap<String, Expression>) -> HashMap<String, VariableAssignment> {
    inputs
        .into_iter()
        .map(|(name, value)| (name, value.0))
        .collect()
}
impl Workflow {
    pub fn define<F>(name: impl Into<String>, body: F) -> Result<Self, WorkflowError>
    where
        F: FnOnce(&mut ThreadBuilder) -> Result<(), WorkflowError>,
    {
        let mut thread = ThreadBuilder::new("entrypoint");
        body(&mut thread)?;
        let (spec, children) = thread.finish()?;
        let mut workflow = Self::new(name);
        workflow.add_thread("entrypoint", spec)?;
        for (name, spec) in children {
            workflow.add_thread(name, spec)?;
        }
        Ok(workflow)
    }
    pub fn retention(mut self, policy: WorkflowRetentionPolicy) -> Self {
        self.request_mut().retention_policy = Some(policy);
        self
    }
    pub fn parent(mut self, name: impl Into<String>, major_version: i32) -> Self {
        self.request_mut().parent_wf_spec = Some(wf_spec::ParentWfSpecReference {
            wf_spec_name: name.into(),
            wf_spec_major_version: major_version,
        });
        self
    }
    pub fn updates(mut self, policy: AllowedUpdateType) -> Self {
        self.request_mut().allowed_updates = policy as i32;
        self
    }
}

fn node_label(operation: &node::Node) -> (String, &'static str) {
    match operation {
        node::Node::Entrypoint(_) => ("entrypoint".into(), "ENTRYPOINT"),
        node::Node::Exit(exit) => (
            match &exit.result {
                Some(exit_node::Result::FailureDef(failure)) => failure.failure_name.clone(),
                Some(exit_node::Result::ReturnContent(_)) => "complete".into(),
                _ => "exit".into(),
            },
            "EXIT",
        ),
        node::Node::Nop(_) => ("nop".into(), "NOP"),
        node::Node::Sleep(_) => ("sleep".into(), "SLEEP"),
        node::Node::Task(task) => (
            match &task.task_to_execute {
                Some(task_node::TaskToExecute::TaskDefId(id)) => id.name.clone(),
                _ => "dynamic-task".into(),
            },
            "TASK",
        ),
        node::Node::ExternalEvent(event) => (
            event
                .external_event_def_id
                .as_ref()
                .map(|id| id.name.clone())
                .unwrap_or_default(),
            "EXTERNAL_EVENT",
        ),
        node::Node::ThrowEvent(event) => (
            format!(
                "throw-{}",
                event
                    .event_def_id
                    .as_ref()
                    .map(|id| id.name.as_str())
                    .unwrap_or_default()
            ),
            "THROW_EVENT",
        ),
        node::Node::StartThread(thread) => (thread.thread_spec_name.clone(), "START_THREAD"),
        node::Node::StartMultipleThreads(thread) => {
            (thread.thread_spec_name.clone(), "START_MULTIPLE_THREADS")
        }
        node::Node::WaitForThreads(_) => ("threads".into(), "WAIT_FOR_THREADS"),
        node::Node::WaitForCondition(_) => ("wait-for-condition".into(), "WAIT_FOR_CONDITION"),
        node::Node::UserTask(task) => (task.user_task_def_name.clone(), "USER_TASK"),
        node::Node::RunChildWf(child) => (
            match &child.wf_spec {
                Some(run_child_wf_node::WfSpec::WfSpecName(name)) => format!("run-{name}"),
                _ => "run-child-wf".into(),
            },
            "RUN_CHILD_WF",
        ),
        node::Node::WaitForChildWf(_) => ("wait".into(), "WAIT_FOR_CHILD_WF"),
    }
}

#[derive(Clone, Debug)]
pub enum FailureFilter {
    Any,
    Error(Option<LhErrorType>),
    Exception(Option<String>),
}
impl FailureFilter {
    fn wire(self) -> Option<failure_handler_def::FailureToCatch> {
        use failure_handler_def::{FailureToCatch, LhFailureType};
        match self {
            Self::Any => None,
            Self::Error(Some(error)) => {
                Some(FailureToCatch::SpecificFailure(error.as_str_name().into()))
            }
            Self::Error(None) => Some(FailureToCatch::AnyFailureOfType(
                LhFailureType::FailureTypeError as i32,
            )),
            Self::Exception(Some(name)) => Some(FailureToCatch::SpecificFailure(name)),
            Self::Exception(None) => Some(FailureToCatch::AnyFailureOfType(
                LhFailureType::FailureTypeException as i32,
            )),
        }
    }
}
impl ThreadBuilder {
    pub fn handle<F>(
        &mut self,
        output: &NodeOutput,
        filter: FailureFilter,
        body: F,
    ) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        let name = handler_name(&output.name, &filter, false);
        self.handle_failure(output, &name, filter.wire(), body)
    }
    pub fn handle_child_failure<F>(
        &mut self,
        output: &NodeOutput,
        filter: FailureFilter,
        body: F,
    ) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        let name = handler_name(&output.name, &filter, true);
        self.child(&name, body)?;
        match &mut self.node_mut(output)?.node {
            Some(node::Node::WaitForThreads(wait)) => {
                wait.per_thread_failure_handlers.push(FailureHandlerDef {
                    handler_spec_name: name,
                    failure_to_catch: filter.wire(),
                });
                Ok(())
            }
            _ => Err(WorkflowError::UnknownNode(output.name.clone())),
        }
    }
    pub fn run_workflow(
        &mut self,
        name: impl Into<String>,
        major_version: i32,
        inputs: HashMap<String, Expression>,
        id: Option<Expression>,
    ) -> NodeOutput {
        self.append(node::Node::RunChildWf(RunChildWfNode {
            wf_spec: Some(run_child_wf_node::WfSpec::WfSpecName(name.into())),
            major_version,
            inputs: assignments(inputs),
            child_id: id.map(|id| id.0),
        }))
    }
    pub fn user_task_cancellation_exception(
        &mut self,
        output: &NodeOutput,
        name: Expression,
    ) -> Result<(), WorkflowError> {
        match &mut self.node_mut(output)?.node {
            Some(node::Node::UserTask(task)) => {
                task.on_cancellation_exception_name = Some(name.0);
                Ok(())
            }
            _ => Err(WorkflowError::UnknownNode(output.name.clone())),
        }
    }
}
impl From<&Variable> for Expression {
    fn from(value: &Variable) -> Self {
        value.expression()
    }
}
impl From<&NodeOutput> for Expression {
    fn from(value: &NodeOutput) -> Self {
        value.expression()
    }
}

fn handler_name(node: &str, filter: &FailureFilter, child: bool) -> String {
    match filter {
        FailureFilter::Any => {
            if child {
                format!("failure-handler-{node}-ANY_FAILURE")
            } else {
                format!("exn-handler-{node}-any-failure")
            }
        }
        FailureFilter::Error(Some(error)) => format!(
            "{}-{node}-{}",
            if child {
                "error-handler"
            } else {
                "exn-handler"
            },
            error.as_str_name()
        ),
        FailureFilter::Error(None) => format!("exn-handler-{node}-FAILURE_TYPE_ERROR"),
        FailureFilter::Exception(Some(name)) => format!("exn-handler-{node}-{name}"),
        FailureFilter::Exception(None) => format!("exn-handler-{node}-FAILURE_TYPE_EXCEPTION"),
    }
}
#[derive(Clone, Debug)]
pub struct BranchHandle {
    branch: String,
    merge: String,
}
impl ThreadBuilder {
    pub fn if_branch<F>(
        &mut self,
        condition: Expression,
        body: F,
    ) -> Result<BranchHandle, WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        let index = self.next_node;
        self.do_if(condition, body)?;
        let merge = self
            .cursor
            .clone()
            .ok_or_else(|| WorkflowError::InvalidThread(self.name.clone()))?;
        Ok(BranchHandle {
            branch: format!("{index}-nop-NOP"),
            merge,
        })
    }
    pub fn else_if<F>(
        &mut self,
        branch: &BranchHandle,
        condition: Expression,
        body: F,
    ) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.spec
            .nodes
            .get_mut(&branch.branch)
            .ok_or_else(|| WorkflowError::UnknownNode(branch.branch.clone()))?
            .outgoing_edges
            .pop();
        let edge_count = self.spec.nodes[&branch.branch].outgoing_edges.len();
        self.cursor = Some(branch.branch.clone());
        body(self)?;
        if self.spec.nodes[&branch.branch].outgoing_edges.len() == edge_count {
            self.edge(&branch.branch, &branch.merge, Some(condition));
            self.spec
                .nodes
                .get_mut(&branch.branch)
                .unwrap()
                .outgoing_edges
                .last_mut()
                .unwrap()
                .variable_mutations = std::mem::take(&mut self.pending_mutations);
        } else {
            self.spec
                .nodes
                .get_mut(&branch.branch)
                .unwrap()
                .outgoing_edges[edge_count]
                .edge_condition = Some(edge::EdgeCondition::Condition(condition.0));
            if let Some(end) = self.cursor.take() {
                self.edge(&end, &branch.merge, None);
                self.spec
                    .nodes
                    .get_mut(&end)
                    .unwrap()
                    .outgoing_edges
                    .last_mut()
                    .unwrap()
                    .variable_mutations = std::mem::take(&mut self.pending_mutations);
            }
        }
        self.edge(&branch.branch, &branch.merge, None);
        self.cursor = Some(branch.merge.clone());
        Ok(())
    }
    pub fn otherwise<F>(&mut self, branch: &BranchHandle, body: F) -> Result<(), WorkflowError>
    where
        F: FnOnce(&mut Self) -> Result<(), WorkflowError>,
    {
        self.spec
            .nodes
            .get_mut(&branch.branch)
            .ok_or_else(|| WorkflowError::UnknownNode(branch.branch.clone()))?
            .outgoing_edges
            .pop();
        self.cursor = Some(branch.branch.clone());
        body(self)?;
        if let Some(end) = self.cursor.take() {
            self.edge(&end, &branch.merge, None);
            self.spec
                .nodes
                .get_mut(&end)
                .unwrap()
                .outgoing_edges
                .last_mut()
                .unwrap()
                .variable_mutations = std::mem::take(&mut self.pending_mutations);
        }
        let merge_is_reachable = self.spec.nodes.values().any(|node| {
            node.outgoing_edges
                .iter()
                .any(|edge| edge.sink_node_name == branch.merge)
        });
        if merge_is_reachable {
            self.cursor = Some(branch.merge.clone());
        } else {
            self.spec.nodes.remove(&branch.merge);
            self.cursor = None;
        }
        Ok(())
    }
}
