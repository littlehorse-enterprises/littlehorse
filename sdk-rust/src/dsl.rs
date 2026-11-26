//! Sequential, conditional, parallel, and event-driven workflow construction.
use crate::{
    proto::*,
    types::{IntoValue, LittleHorseValue, StructSchema},
    workflow::{Expression, Workflow, WorkflowError, primitive_type},
};
use std::{
    collections::HashMap,
    sync::{Arc, Mutex},
};

/// The part of a thread's build state that variables also write to: the current node
/// and the mutations waiting to be attached to its next outgoing edge.
#[derive(Default)]
struct ThreadState {
    cursor: Option<String>,
    pending_mutations: Vec<VariableMutation>,
    errors: Vec<WorkflowError>,
}
impl ThreadState {
    fn mutate(&mut self, variable: &str, operation: VariableMutationType, value: Expression) {
        if self.cursor.is_none() {
            self.errors.push(WorkflowError::UnreachableNode(
                "mutation after return/fail".into(),
            ));
        }
        self.pending_mutations.push(VariableMutation {
            lhs_name: variable.into(),
            operation: operation as i32,
            rhs_value: Some(variable_mutation::RhsValue::RhsAssignment(value.0)),
            ..Default::default()
        });
    }
}
/// The stack of threads currently being built; the innermost one receives mutations
/// made through `Variable::assign` and `Variable::mutate`.
type ActiveThreads = Arc<Mutex<Vec<Arc<Mutex<ThreadState>>>>>;

/// A workflow variable. Clones share one definition, so options can be set before or
/// after declaring it, like the other SDKs:
/// `thread.declare_str("name").with_default("world").searchable()`.
/// The declaring thread reads the definition when the workflow is compiled.
#[derive(Clone, Debug)]
pub struct Variable {
    pub name: String,
    state: Arc<Mutex<VariableState>>,
}
struct VariableState {
    definition: ThreadVarDef,
    error: Option<WorkflowError>,
    active: Option<ActiveThreads>,
}
impl std::fmt::Debug for VariableState {
    fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
        formatter
            .debug_struct("VariableState")
            .field("definition", &self.definition)
            .field("error", &self.error)
            .finish_non_exhaustive()
    }
}
impl Variable {
    pub fn new(name: impl Into<String>, variable_type: TypeDefinition) -> Self {
        let name = name.into();
        let definition = ThreadVarDef {
            var_def: Some(VariableDef {
                name: name.clone(),
                type_def: Some(variable_type),
                ..Default::default()
            }),
            access_level: WfRunVariableAccessLevel::PrivateVar as i32,
            ..Default::default()
        };
        Self {
            name,
            state: Arc::new(Mutex::new(VariableState {
                definition,
                error: None,
                active: None,
            })),
        }
    }
    pub fn typed<T: LittleHorseValue>(name: impl Into<String>) -> Self {
        Self::new(name, T::type_definition())
    }
    pub fn primitive(name: impl Into<String>, variable_type: VariableType) -> Self {
        Self::new(name, primitive_type(variable_type))
    }
    fn update(&self, change: impl FnOnce(&mut VariableState)) -> Self {
        change(&mut self.state.lock().unwrap());
        self.clone()
    }
    fn var_def(state: &mut VariableState) -> &mut VariableDef {
        state.definition.var_def.as_mut().unwrap()
    }
    pub fn required(&self) -> Self {
        self.update(|state| state.definition.required = true)
    }
    pub fn searchable(&self) -> Self {
        self.update(|state| state.definition.searchable = true)
    }
    pub fn masked(&self) -> Self {
        self.update(|state| Self::var_def(state).type_def.as_mut().unwrap().masked = true)
    }
    pub fn access(&self, access: WfRunVariableAccessLevel) -> Self {
        self.update(|state| state.definition.access_level = access as i32)
    }
    /// Set the default from a native value, such as `"world"`, `3`, or a derived struct.
    /// A value that cannot be converted is reported by `Workflow::define`.
    pub fn with_default(&self, value: impl IntoValue) -> Self {
        let name = self.name.clone();
        self.update(|state| match value.into_value() {
            Ok(value) => Self::var_def(state).default_value = Some(value),
            Err(error) => state.error = Some(WorkflowError::InvalidDefault(name, error)),
        })
    }
    /// Set the default from an already-converted protobuf value.
    pub fn default_value(&self, value: VariableValue) -> Self {
        self.update(|state| Self::var_def(state).default_value = Some(value))
    }
    pub fn index(&self, path: impl Into<String>, kind: VariableType) -> Self {
        let index = JsonIndex {
            field_path: path.into(),
            field_type: kind as i32,
        };
        self.update(|state| state.definition.json_indexes.push(index))
    }
    /// A snapshot of the current definition.
    pub fn definition(&self) -> ThreadVarDef {
        self.state.lock().unwrap().definition.clone()
    }
    pub fn expression(&self) -> Expression {
        Expression::variable(&self.name)
    }
    /// Assign a value at this point in the thread currently being built, like
    /// `variable.assign(...)` in the other SDKs.
    pub fn assign(&self, value: impl Into<Expression>) {
        self.mutate(VariableMutationType::Assign, value);
    }
    /// Mutate this variable at this point in the thread currently being built.
    pub fn mutate(&self, operation: VariableMutationType, value: impl Into<Expression>) {
        let active = self.state.lock().unwrap().active.clone();
        let Some(thread) = active.and_then(|active| active.lock().unwrap().last().cloned()) else {
            self.state.lock().unwrap().error =
                Some(WorkflowError::UndeclaredVariable(self.name.clone()));
            return;
        };
        thread
            .lock()
            .unwrap()
            .mutate(&self.name, operation, value.into());
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
/// Expression methods available directly on variables and node outputs, so
/// `donuts.less_than(10)` works without calling `.expression()` first.
macro_rules! expression_methods {
    ($($target:ty),*) => {$(
        impl $target {
            pub fn json_path(&self, path: impl Into<String>) -> Expression {
                self.expression().json_path(path)
            }
            pub fn field(&self, field: impl Into<String>) -> Expression {
                self.expression().field(field)
            }
            pub fn cast(&self, target_type: TypeDefinition) -> Expression {
                self.expression().cast(target_type)
            }
            pub fn size(&self) -> Expression {
                self.expression().size()
            }
            pub fn negate(&self) -> Expression {
                self.expression().negate()
            }
            expression_methods!(@binary plus, minus, times, divided_by, pow, extend, and, or,
                remove_if_present, remove_index, remove_key, equals, not_equals, less_than,
                greater_than, less_or_equal, greater_or_equal, is_in, not_in);
        }
    )*};
    (@binary $($method:ident),*) => {$(
        pub fn $method(&self, rhs: impl Into<Expression>) -> Expression {
            self.expression().$method(rhs)
        }
    )*};
}
expression_methods!(Variable, NodeOutput);

/// Task inputs: `()` for none, a single value such as `&name`, a tuple of values such
/// as `(&full_name, &email)`, or an array or `Vec` of `Expression`s.
pub trait TaskInputs {
    fn into_expressions(self) -> Vec<Expression>;
}
impl<const N: usize> TaskInputs for [Expression; N] {
    fn into_expressions(self) -> Vec<Expression> {
        self.into()
    }
}
impl TaskInputs for Vec<Expression> {
    fn into_expressions(self) -> Vec<Expression> {
        self
    }
}
impl TaskInputs for () {
    fn into_expressions(self) -> Vec<Expression> {
        Vec::new()
    }
}
macro_rules! single_input {
    ($($input:ty),*) => {$(
        impl TaskInputs for $input {
            fn into_expressions(self) -> Vec<Expression> {
                vec![self.into()]
            }
        }
    )*};
}
single_input!(
    Expression,
    &Variable,
    &NodeOutput,
    &TaskOutput,
    &EventOutput,
    &UserTaskOutput,
    &str,
    String,
    i64,
    i32,
    bool
);
macro_rules! tuple_inputs {
    ($(($($input:ident),+)),*) => {$(
        impl<$($input: Into<Expression>),+> TaskInputs for ($($input,)+) {
            #[allow(non_snake_case)]
            fn into_expressions(self) -> Vec<Expression> {
                let ($($input,)+) = self;
                vec![$($input.into()),+]
            }
        }
    )*};
}
tuple_inputs!(
    (A),
    (A, B),
    (A, B, C),
    (A, B, C, D),
    (A, B, C, D, E),
    (A, B, C, D, E, F)
);

/// What a thread body closure may return: `()` or `Result<(), WorkflowError>`.
/// Builder errors are collected and returned from `Workflow::define` either way.
pub trait BodyResult {
    fn into_result(self) -> Result<(), WorkflowError>;
}
impl BodyResult for () {
    fn into_result(self) -> Result<(), WorkflowError> {
        Ok(())
    }
}
impl BodyResult for Result<(), WorkflowError> {
    fn into_result(self) -> Result<(), WorkflowError> {
        self
    }
}

#[derive(Clone, Debug)]
pub struct TaskOptions {
    pub task: TaskNode,
}
impl TaskOptions {
    pub fn new(name: impl Into<String>, inputs: impl TaskInputs) -> Self {
        let node::Node::Task(task) = crate::workflow::task(name, inputs.into_expressions()) else {
            unreachable!()
        };
        Self { task }
    }
    pub fn dynamic(name: Expression, inputs: impl TaskInputs) -> Self {
        Self {
            task: TaskNode {
                task_to_execute: Some(task_node::TaskToExecute::DynamicTask(name.0)),
                timeout_seconds: 0,
                variables: inputs
                    .into_expressions()
                    .into_iter()
                    .map(|input| input.0)
                    .collect(),
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
    pub fn timeout(mut self, seconds: impl Into<Expression>) -> Self {
        self.event.timeout_seconds = Some(seconds.into().0);
        self
    }
    pub fn correlation(mut self, key: impl Into<Expression>, masked: bool) -> Self {
        self.event.correlation_key = Some(key.into().0);
        self.event.mask_correlation_key = masked;
        self
    }
}
/// A thread builder's closures execute once at definition time, never on workers.
/// Builder methods never fail directly: problems are collected and returned from
/// `Workflow::define`.
pub struct ThreadBuilder {
    name: String,
    spec: ThreadSpec,
    children: HashMap<String, ThreadSpec>,
    next_node: usize,
    state: Arc<Mutex<ThreadState>>,
    active: ActiveThreads,
    variables: Vec<Variable>,
    edits: PendingEdits,
    pub default_task_timeout: i32,
    pub default_task_retries: i32,
    pub default_task_backoff: Option<ExponentialBackoffRetryPolicy>,
}
impl ThreadBuilder {
    pub fn new(name: impl Into<String>) -> Self {
        Self::within(name, Arc::default())
    }
    /// A builder that is the innermost active thread of `active` until it finishes.
    fn within(name: impl Into<String>, active: ActiveThreads) -> Self {
        let state = Arc::<Mutex<ThreadState>>::default();
        active.lock().unwrap().push(state.clone());
        let mut builder = Self {
            name: name.into(),
            spec: ThreadSpec::default(),
            children: HashMap::new(),
            next_node: 0,
            state,
            active,
            variables: Vec::new(),
            edits: PendingEdits::default(),
            default_task_timeout: 0,
            default_task_retries: 0,
            default_task_backoff: None,
        };
        builder.append(node::Node::Entrypoint(EntrypointNode {}));
        builder
    }
    fn cursor(&self) -> Option<String> {
        self.state.lock().unwrap().cursor.clone()
    }
    fn set_cursor(&self, cursor: Option<String>) {
        self.state.lock().unwrap().cursor = cursor;
    }
    fn take_cursor(&self) -> Option<String> {
        self.state.lock().unwrap().cursor.take()
    }
    fn take_mutations(&self) -> Vec<VariableMutation> {
        std::mem::take(&mut self.state.lock().unwrap().pending_mutations)
    }
    fn error(&self, error: WorkflowError) {
        self.state.lock().unwrap().errors.push(error);
    }
    /// Run a body closure on this builder, recording any error it returns.
    fn run<F, R>(&mut self, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        if let Err(error) = body(self).into_result() {
            self.error(error);
        }
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
    /// Attach the pending mutations to the last outgoing edge of `node`.
    fn attach_mutations(&mut self, node: &str) {
        let mutations = self.take_mutations();
        self.spec
            .nodes
            .get_mut(node)
            .unwrap()
            .outgoing_edges
            .last_mut()
            .unwrap()
            .variable_mutations = mutations;
    }
    pub fn append(&mut self, operation: node::Node) -> NodeOutput {
        if self.cursor().is_none() && !self.spec.nodes.is_empty() {
            self.error(WorkflowError::UnreachableNode(
                "code after return/fail".into(),
            ));
        }
        let terminal = matches!(operation, node::Node::Exit(_));
        let name = self.insert(operation);
        if let Some(previous) = self.take_cursor() {
            self.edge(&previous, &name, None);
            self.attach_mutations(&previous);
        }
        if !terminal {
            self.set_cursor(Some(name.clone()));
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
    /// Declare a variable in this thread. The returned handle shares its definition,
    /// so options set on it later still apply.
    pub fn declare(&mut self, variable: Variable) -> Variable {
        if self
            .variables
            .iter()
            .any(|existing| existing.name == variable.name)
        {
            self.error(WorkflowError::InvalidName(variable.name.clone()));
        } else {
            variable.state.lock().unwrap().active = Some(self.active.clone());
            self.variables.push(variable.clone());
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
    pub fn declare_double(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::typed::<f64>(name))
    }
    pub fn declare_json_obj(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::primitive(name, VariableType::JsonObj))
    }
    pub fn declare_json_arr(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::primitive(name, VariableType::JsonArr))
    }
    pub fn declare_struct<T: StructSchema>(&mut self, name: impl Into<String>) -> Variable {
        self.declare(Variable::typed::<T>(name))
    }
    pub fn execute(&mut self, name: impl Into<String>, inputs: impl TaskInputs) -> TaskOutput {
        let mut options = TaskOptions::new(name, inputs)
            .timeout(self.default_task_timeout)
            .retries(self.default_task_retries);
        options.task.exponential_backoff = self.default_task_backoff;
        self.execute_with(options)
    }
    pub fn execute_with(&mut self, options: TaskOptions) -> TaskOutput {
        let output = self.append(node::Node::Task(options.task));
        self.typed_output(output)
    }
    /// Wait for an external event. Options can be passed here or chained on the result:
    /// `wf.wait_for_event("paid").timeout(60)`.
    pub fn wait_for_event(&mut self, options: impl Into<EventOptions>) -> EventOutput {
        let output = self.append(node::Node::ExternalEvent(options.into().event));
        self.typed_output(output)
    }
    pub fn throw_event(
        &mut self,
        name: impl Into<String>,
        content: impl Into<Expression>,
    ) -> NodeOutput {
        self.append(node::Node::ThrowEvent(ThrowEventNode {
            event_def_id: Some(WorkflowEventDefId { name: name.into() }),
            content: Some(content.into().0),
        }))
    }
    pub fn wait_for_condition(&mut self, condition: impl Into<Expression>) -> NodeOutput {
        self.append(node::Node::WaitForCondition(WaitForConditionNode {
            node_condition: Some(wait_for_condition_node::NodeCondition::Condition(
                condition.into().0,
            )),
        }))
    }
    pub fn sleep(&mut self, seconds: impl Into<Expression>) -> NodeOutput {
        self.append(node::Node::Sleep(SleepNode {
            sleep_length: Some(sleep_node::SleepLength::RawSeconds(seconds.into().0)),
        }))
    }
    pub fn sleep_until(&mut self, timestamp: impl Into<Expression>) -> NodeOutput {
        self.append(node::Node::Sleep(SleepNode {
            sleep_length: Some(sleep_node::SleepLength::Timestamp(timestamp.into().0)),
        }))
    }
    pub fn sleep_until_iso(&mut self, timestamp: impl Into<Expression>) -> NodeOutput {
        self.append(node::Node::Sleep(SleepNode {
            sleep_length: Some(sleep_node::SleepLength::IsoDate(timestamp.into().0)),
        }))
    }
    pub fn mutate(
        &mut self,
        variable: &Variable,
        operation: VariableMutationType,
        value: impl Into<Expression>,
    ) {
        self.state
            .lock()
            .unwrap()
            .mutate(&variable.name, operation, value.into());
    }
    pub fn assign(&mut self, variable: &Variable, value: impl Into<Expression>) {
        self.mutate(variable, VariableMutationType::Assign, value);
    }
    pub fn return_value(&mut self, value: impl Into<Expression>) {
        self.append(node::Node::Exit(ExitNode {
            result: Some(exit_node::Result::ReturnContent(value.into().0)),
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
    fn if_else<F, G, R, S>(&mut self, condition: Expression, then_body: F, else_body: G)
    where
        F: FnOnce(&mut Self) -> R,
        G: FnOnce(&mut Self) -> S,
        R: BodyResult,
        S: BodyResult,
    {
        let branch = self.append(node::Node::Nop(NopNode {})).name;
        self.run(then_body);
        let then_edge_count = self.spec.nodes[&branch].outgoing_edges.len();
        let then_end = self.take_cursor();
        let merge = self.insert(node::Node::Nop(NopNode {}));
        if let Some(end) = then_end.as_ref().filter(|end| **end != branch) {
            self.edge(end, &merge, None);
            self.attach_mutations(end);
        }
        if then_edge_count == 0 {
            self.edge(&branch, &merge, Some(condition));
            self.attach_mutations(&branch);
        } else {
            self.spec.nodes.get_mut(&branch).unwrap().outgoing_edges[0].edge_condition =
                Some(edge::EdgeCondition::Condition(condition.0));
        }
        self.set_cursor(Some(branch.clone()));
        self.run(else_body);
        let else_end = self.take_cursor();
        if let Some(end) = &else_end {
            self.edge(end, &merge, None);
            self.attach_mutations(end);
        }
        if then_end.is_none() && else_end.is_none() {
            self.spec.nodes.remove(&merge);
            self.set_cursor(None);
        } else {
            self.set_cursor(Some(merge));
        }
    }
    /// Start an if statement, like `doIf` in the other SDKs. Chain `do_else_if` and
    /// `do_else` on the result:
    /// `wf.do_if(a, |_| ...).do_else_if(b, |_| ...).do_else(|_| ...)`.
    pub fn do_if<F, R>(&mut self, condition: impl Into<Expression>, body: F) -> IfStatement<'_>
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        let index = self.next_node;
        self.if_else(condition.into(), body, |_| ());
        // An empty else branch always reaches the merge node, so the cursor is set.
        let merge = self.cursor().unwrap_or_default();
        IfStatement {
            thread: self,
            branch: format!("{index}-nop-NOP"),
            merge,
        }
    }
    pub fn while_loop<F, R>(&mut self, condition: impl Into<Expression>, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        let condition: Expression = condition.into();
        let check = self.append(node::Node::Nop(NopNode {})).name;
        self.run(body);
        let body_end = self.cursor();
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
            self.state.lock().unwrap().errors.pop();
        }
        self.set_cursor(Some(merge));
    }
    fn child<F, R>(&mut self, name: &str, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        if name == self.name || self.children.contains_key(name) {
            self.error(WorkflowError::InvalidName(name.into()));
            return;
        }
        let mut child = Self::within(name, self.active.clone());
        child.default_task_timeout = self.default_task_timeout;
        child.default_task_retries = self.default_task_retries;
        child.default_task_backoff = self.default_task_backoff;
        let outcome = body(&mut child).into_result();
        self.active.lock().unwrap().pop();
        match outcome.and_then(|()| child.finish()) {
            Ok((spec, children)) => {
                self.children.insert(name.into(), spec);
                for (name, spec) in children {
                    if name == self.name || self.children.insert(name.clone(), spec).is_some() {
                        self.error(WorkflowError::InvalidName(name));
                    }
                }
            }
            Err(error) => self.error(error),
        }
    }
    pub fn spawn<F, R>(
        &mut self,
        name: &str,
        inputs: HashMap<String, Expression>,
        body: F,
    ) -> NodeOutput
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.child(name, body);
        self.append(node::Node::StartThread(StartThreadNode {
            thread_spec_name: name.into(),
            variables: assignments(inputs),
        }))
    }
    pub fn for_each<F, R>(
        &mut self,
        name: &str,
        iterable: impl Into<Expression>,
        inputs: HashMap<String, Expression>,
        body: F,
    ) -> NodeOutput
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.child(name, body);
        self.append(node::Node::StartMultipleThreads(StartMultipleThreadsNode {
            thread_spec_name: name.into(),
            variables: assignments(inputs),
            iterable: Some(iterable.into().0),
        }))
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
        threads: impl Into<Expression>,
        strategy: WaitForThreadsStrategy,
    ) -> NodeOutput {
        self.append(node::Node::WaitForThreads(WaitForThreadsNode {
            strategy: strategy as i32,
            threads_to_wait_for: Some(wait_for_threads_node::ThreadsToWaitForOneOf::ThreadList(
                threads.into().0,
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
    pub fn interrupt<F, R>(&mut self, event: &str, handler_name: &str, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.child(handler_name, body);
        self.spec.interrupt_defs.push(InterruptDef {
            external_event_def_id: Some(ExternalEventDefId { name: event.into() }),
            handler_spec_name: handler_name.into(),
        });
    }
    pub fn handle_failure<F, R>(
        &mut self,
        output: &NodeOutput,
        handler_name: &str,
        failure: Option<failure_handler_def::FailureToCatch>,
        body: F,
    ) where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.child(handler_name, body);
        let handler = FailureHandlerDef {
            handler_spec_name: handler_name.into(),
            failure_to_catch: failure,
        };
        match self.spec.nodes.get_mut(&output.name) {
            Some(node) => node.failure_handlers.push(handler),
            None => self.error(WorkflowError::UnknownNode(output.name.clone())),
        }
    }
    pub fn user_task(
        &mut self,
        name: impl Into<String>,
        user: Option<Expression>,
        group: Option<Expression>,
        notes: Option<Expression>,
    ) -> UserTaskOutput {
        let output = self.append(node::Node::UserTask(UserTaskNode {
            user_task_def_name: name.into(),
            user_id: user.map(|value| value.0),
            user_group: group.map(|value| value.0),
            notes: notes.map(|value| value.0),
            ..Default::default()
        }));
        self.typed_output(output)
    }
    pub fn user_task_action(&mut self, output: &NodeOutput, action: UtActionTrigger) {
        match self
            .spec
            .nodes
            .get_mut(&output.name)
            .map(|node| &mut node.node)
        {
            Some(Some(node::Node::UserTask(task))) => task.actions.push(action),
            _ => self.error(WorkflowError::UnknownNode(output.name.clone())),
        }
    }
    pub fn retention(&mut self, policy: ThreadRetentionPolicy) {
        self.spec.retention_policy = Some(policy);
    }
    pub fn node_mut(&mut self, output: &NodeOutput) -> Result<&mut Node, WorkflowError> {
        self.apply_edits();
        self.spec
            .nodes
            .get_mut(&output.name)
            .ok_or_else(|| WorkflowError::UnknownNode(output.name.clone()))
    }
    pub fn finish(mut self) -> Result<(ThreadSpec, HashMap<String, ThreadSpec>), WorkflowError> {
        self.apply_edits();
        for variable in &self.variables {
            let mut state = variable.state.lock().unwrap();
            if let Some(error) = state.error.take() {
                return Err(error);
            }
            self.spec.variable_defs.push(state.definition.clone());
        }
        let errors = std::mem::take(&mut self.state.lock().unwrap().errors);
        if let Some(error) = errors.into_iter().next() {
            return Err(error);
        }
        if self.cursor().is_some() {
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
    /// Define a workflow. The body may return `()` or `Result<(), WorkflowError>`;
    /// every problem found while building is returned here.
    pub fn define<F, R>(name: impl Into<String>, body: F) -> Result<Self, WorkflowError>
    where
        F: FnOnce(&mut ThreadBuilder) -> R,
        R: BodyResult,
    {
        let mut thread = ThreadBuilder::new("entrypoint");
        body(&mut thread).into_result()?;
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
    pub fn handle<F, R>(&mut self, output: &NodeOutput, filter: FailureFilter, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        let name = handler_name(&output.name, &filter, false);
        self.handle_failure(output, &name, filter.wire(), body)
    }
    pub fn handle_child_failure<F, R>(
        &mut self,
        output: &NodeOutput,
        filter: FailureFilter,
        body: F,
    ) where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        let name = handler_name(&output.name, &filter, true);
        self.child(&name, body);
        let handler = FailureHandlerDef {
            handler_spec_name: name,
            failure_to_catch: filter.wire(),
        };
        match self
            .spec
            .nodes
            .get_mut(&output.name)
            .map(|node| &mut node.node)
        {
            Some(Some(node::Node::WaitForThreads(wait))) => {
                wait.per_thread_failure_handlers.push(handler)
            }
            _ => self.error(WorkflowError::UnknownNode(output.name.clone())),
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
    pub fn user_task_cancellation_exception(&mut self, output: &NodeOutput, name: Expression) {
        match self
            .spec
            .nodes
            .get_mut(&output.name)
            .map(|node| &mut node.node)
        {
            Some(Some(node::Node::UserTask(task))) => {
                task.on_cancellation_exception_name = Some(name.0)
            }
            _ => self.error(WorkflowError::UnknownNode(output.name.clone())),
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
/// An if statement being built, returned by `ThreadBuilder::do_if`. It borrows the
/// thread until the chain ends.
pub struct IfStatement<'a> {
    thread: &'a mut ThreadBuilder,
    branch: String,
    merge: String,
}
impl IfStatement<'_> {
    /// Add an `else if` branch, like `doElseIf` in the other SDKs.
    pub fn do_else_if<F, R>(self, condition: impl Into<Expression>, body: F) -> Self
    where
        F: FnOnce(&mut ThreadBuilder) -> R,
        R: BodyResult,
    {
        let condition: Expression = condition.into();
        let Self {
            thread,
            branch,
            merge,
        } = self;
        let Some(node) = thread.spec.nodes.get_mut(&branch) else {
            thread.error(WorkflowError::UnknownNode(branch.clone()));
            return Self {
                thread,
                branch,
                merge,
            };
        };
        // Replace the default edge to the merge node with this branch, then restore it.
        node.outgoing_edges.pop();
        let edge_count = node.outgoing_edges.len();
        thread.set_cursor(Some(branch.clone()));
        thread.run(body);
        if thread.spec.nodes[&branch].outgoing_edges.len() == edge_count {
            thread.edge(&branch, &merge, Some(condition));
            thread.attach_mutations(&branch);
        } else {
            thread.spec.nodes.get_mut(&branch).unwrap().outgoing_edges[edge_count].edge_condition =
                Some(edge::EdgeCondition::Condition(condition.0));
            if let Some(end) = thread.take_cursor() {
                thread.edge(&end, &merge, None);
                thread.attach_mutations(&end);
            }
        }
        thread.edge(&branch, &merge, None);
        thread.set_cursor(Some(merge.clone()));
        Self {
            thread,
            branch,
            merge,
        }
    }
    /// Add the final `else` branch, like `doElse` in the other SDKs.
    pub fn do_else<F, R>(self, body: F)
    where
        F: FnOnce(&mut ThreadBuilder) -> R,
        R: BodyResult,
    {
        let Self {
            thread,
            branch,
            merge,
        } = self;
        let Some(node) = thread.spec.nodes.get_mut(&branch) else {
            thread.error(WorkflowError::UnknownNode(branch));
            return;
        };
        node.outgoing_edges.pop();
        thread.set_cursor(Some(branch.clone()));
        thread.run(body);
        if let Some(end) = thread.take_cursor() {
            thread.edge(&end, &merge, None);
            thread.attach_mutations(&end);
        }
        let merge_is_reachable = thread.spec.nodes.values().any(|node| {
            node.outgoing_edges
                .iter()
                .any(|edge| edge.sink_node_name == merge)
        });
        if merge_is_reachable {
            thread.set_cursor(Some(merge));
        } else {
            thread.spec.nodes.remove(&merge);
            thread.set_cursor(None);
        }
    }
}
/// Named failure handlers, matching `handleError`, `handleException`, and
/// `handleAnyFailure` in the other SDKs.
impl ThreadBuilder {
    /// Handle one specific technical error, such as `LhErrorType::Timeout`.
    pub fn handle_error<F, R>(&mut self, output: &NodeOutput, error: LhErrorType, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.handle(output, FailureFilter::Error(Some(error)), body)
    }
    /// Handle any technical error.
    pub fn handle_any_error<F, R>(&mut self, output: &NodeOutput, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.handle(output, FailureFilter::Error(None), body)
    }
    /// Handle one named business exception thrown by a task.
    pub fn handle_exception<F, R>(&mut self, output: &NodeOutput, name: &str, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.handle(output, FailureFilter::Exception(Some(name.into())), body)
    }
    /// Handle any business exception.
    pub fn handle_any_exception<F, R>(&mut self, output: &NodeOutput, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.handle(output, FailureFilter::Exception(None), body)
    }
    /// Handle any failure, error or exception.
    pub fn handle_any_failure<F, R>(&mut self, output: &NodeOutput, body: F)
    where
        F: FnOnce(&mut Self) -> R,
        R: BodyResult,
    {
        self.handle(output, FailureFilter::Any, body)
    }
}
/// User task assignment, matching `assignUserTask` in the other SDKs.
impl ThreadBuilder {
    /// Assign a user task to one user.
    pub fn assign_user_task_to_user(
        &mut self,
        name: impl Into<String>,
        user: impl Into<Expression>,
    ) -> UserTaskOutput {
        self.user_task(name, Some(user.into()), None, None)
    }
    /// Assign a user task to a group; any member can claim it.
    pub fn assign_user_task_to_group(
        &mut self,
        name: impl Into<String>,
        group: impl Into<Expression>,
    ) -> UserTaskOutput {
        self.user_task(name, None, Some(group.into()), None)
    }
    /// Assign a user task to a user within a group.
    pub fn assign_user_task(
        &mut self,
        name: impl Into<String>,
        user: impl Into<Expression>,
        group: impl Into<Expression>,
    ) -> UserTaskOutput {
        self.user_task(name, Some(user.into()), Some(group.into()), None)
    }
}

/// Edits to a node that is already in the thread, made through a typed output and
/// applied when the thread is compiled (or before `node_mut`).
#[derive(Debug)]
enum NodeEdit {
    Retries(i32),
    Timeout(i32),
    Backoff(ExponentialBackoffRetryPolicy),
    EventTimeout(VariableAssignment),
    Correlation(VariableAssignment, bool),
    Notes(VariableAssignment),
    CancellationException(VariableAssignment),
}
type PendingEdits = Arc<Mutex<Vec<(String, NodeEdit)>>>;
impl ThreadBuilder {
    fn typed_output<T: From<(NodeOutput, PendingEdits)>>(&self, output: NodeOutput) -> T {
        T::from((output, self.edits.clone()))
    }
    fn apply_edits(&mut self) {
        let edits = std::mem::take(&mut *self.edits.lock().unwrap());
        for (name, edit) in edits {
            let node = self
                .spec
                .nodes
                .get_mut(&name)
                .and_then(|node| node.node.as_mut());
            let applied = match (node, edit) {
                (Some(node::Node::Task(task)), NodeEdit::Retries(retries)) => {
                    task.retries = retries;
                    true
                }
                (Some(node::Node::Task(task)), NodeEdit::Timeout(seconds)) => {
                    task.timeout_seconds = seconds;
                    true
                }
                (Some(node::Node::Task(task)), NodeEdit::Backoff(policy)) => {
                    task.exponential_backoff = Some(policy);
                    true
                }
                (Some(node::Node::ExternalEvent(event)), NodeEdit::EventTimeout(seconds)) => {
                    event.timeout_seconds = Some(seconds);
                    true
                }
                (Some(node::Node::ExternalEvent(event)), NodeEdit::Correlation(key, masked)) => {
                    event.correlation_key = Some(key);
                    event.mask_correlation_key = masked;
                    true
                }
                (Some(node::Node::UserTask(task)), NodeEdit::Notes(notes)) => {
                    task.notes = Some(notes);
                    true
                }
                (Some(node::Node::UserTask(task)), NodeEdit::CancellationException(name)) => {
                    task.on_cancellation_exception_name = Some(name);
                    true
                }
                _ => false,
            };
            if !applied {
                self.error(WorkflowError::UnknownNode(name));
            }
        }
    }
}
/// Typed node outputs whose options can be chained after the node is added, like the
/// other SDKs. Each one can be used wherever a `NodeOutput` is expected.
macro_rules! typed_output {
    ($($(#[$doc:meta])* $output:ident),*) => {$(
        $(#[$doc])*
        #[derive(Clone)]
        pub struct $output {
            output: NodeOutput,
            edits: PendingEdits,
        }
        impl $output {
            fn edit(&self, edit: NodeEdit) -> Self {
                self.edits
                    .lock()
                    .unwrap()
                    .push((self.output.name.clone(), edit));
                self.clone()
            }
        }
        impl From<(NodeOutput, PendingEdits)> for $output {
            fn from((output, edits): (NodeOutput, PendingEdits)) -> Self {
                Self { output, edits }
            }
        }
        impl std::ops::Deref for $output {
            type Target = NodeOutput;
            fn deref(&self) -> &NodeOutput {
                &self.output
            }
        }
        impl std::fmt::Debug for $output {
            fn fmt(&self, formatter: &mut std::fmt::Formatter<'_>) -> std::fmt::Result {
                self.output.fmt(formatter)
            }
        }
        impl From<$output> for NodeOutput {
            fn from(value: $output) -> Self {
                value.output
            }
        }
        impl From<&$output> for Expression {
            fn from(value: &$output) -> Self {
                value.expression()
            }
        }
    )*};
}
typed_output!(
    /// Returned by `execute`; chain `with_retries`, `timeout`, or `with_exponential_backoff`.
    TaskOutput,
    /// Returned by `wait_for_event`; chain `timeout` or `with_correlation_id`.
    EventOutput,
    /// Returned by user task assignment; chain `with_notes` or `with_on_cancellation_exception`.
    UserTaskOutput
);
impl TaskOutput {
    pub fn with_retries(&self, retries: i32) -> Self {
        self.edit(NodeEdit::Retries(retries))
    }
    pub fn timeout(&self, seconds: i32) -> Self {
        self.edit(NodeEdit::Timeout(seconds))
    }
    pub fn with_exponential_backoff(&self, policy: ExponentialBackoffRetryPolicy) -> Self {
        self.edit(NodeEdit::Backoff(policy))
    }
}
impl EventOutput {
    pub fn timeout(&self, seconds: impl Into<Expression>) -> Self {
        self.edit(NodeEdit::EventTimeout(seconds.into().0))
    }
    /// Correlate the event by a business key instead of the workflow run ID.
    pub fn with_correlation_id(&self, key: impl Into<Expression>) -> Self {
        self.edit(NodeEdit::Correlation(key.into().0, false))
    }
    /// Like `with_correlation_id`, with the key masked in the dashboard and API.
    pub fn with_masked_correlation_id(&self, key: impl Into<Expression>) -> Self {
        self.edit(NodeEdit::Correlation(key.into().0, true))
    }
}
impl UserTaskOutput {
    pub fn with_notes(&self, notes: impl Into<Expression>) -> Self {
        self.edit(NodeEdit::Notes(notes.into().0))
    }
    pub fn with_on_cancellation_exception(&self, name: impl Into<Expression>) -> Self {
        self.edit(NodeEdit::CancellationException(name.into().0))
    }
}
impl From<&str> for EventOptions {
    fn from(name: &str) -> Self {
        Self::new(name)
    }
}
impl From<String> for EventOptions {
    fn from(name: String) -> Self {
        Self::new(name)
    }
}
