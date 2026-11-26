//! Explicit schemas for typed task parameters and human task forms.
use crate::{
    proto::*,
    types::{LittleHorseValue, TypeError},
};
use std::collections::HashSet;

pub fn parameter<T: LittleHorseValue>(name: impl Into<String>) -> VariableDef {
    VariableDef {
        name: name.into(),
        type_def: Some(T::type_definition()),
        ..Default::default()
    }
}
#[derive(Clone, Debug)]
pub struct UserTaskSchema {
    request: PutUserTaskDefRequest,
}
impl UserTaskSchema {
    pub fn new(name: impl Into<String>) -> Self {
        Self {
            request: PutUserTaskDefRequest {
                name: name.into(),
                ..Default::default()
            },
        }
    }
    /// Use master's struct-backed user-task contract, with an exact registered version.
    pub fn struct_backed(name: impl Into<String>, definition: StructDefId) -> Self {
        Self {
            request: PutUserTaskDefRequest {
                name: name.into(),
                result_struct_def_id: Some(definition),
                ..Default::default()
            },
        }
    }
    pub fn description(mut self, description: impl Into<String>) -> Self {
        self.request.description = Some(description.into());
        self
    }
    #[allow(deprecated)] // Legacy primitive forms remain compatible with v1.3.1.
    pub fn field(mut self, field: UserTaskField) -> Self {
        self.request.fields.push(field);
        self
    }
    #[allow(deprecated)] // Validate legacy fields while supporting master struct-backed forms.
    pub fn compile(self) -> Result<PutUserTaskDefRequest, TypeError> {
        if let Some(id) = &self.request.result_struct_def_id
            && (id.name.is_empty() || id.version < 0 || !self.request.fields.is_empty())
        {
            return Err(TypeError::Expected(
                "an exact nonnegative StructDef version without legacy fields",
            ));
        }
        let mut names = HashSet::new();
        for field in &self.request.fields {
            if field.name.is_empty() || !names.insert(&field.name) {
                return Err(TypeError::Expected(
                    "unique, nonempty user task field names",
                ));
            }
            if !matches!(
                VariableType::try_from(field.r#type),
                Ok(VariableType::Str
                    | VariableType::Int
                    | VariableType::Bool
                    | VariableType::Double)
            ) {
                return Err(TypeError::Expected(
                    "STR, INT, BOOL, or DOUBLE user task fields",
                ));
            }
        }
        Ok(self.request)
    }
    pub fn typed_field<T: LittleHorseValue>(
        name: impl Into<String>,
        required: bool,
    ) -> Result<UserTaskField, TypeError> {
        let name = name.into();
        let Some(type_definition::DefinedType::PrimitiveType(kind)) =
            T::type_definition().defined_type
        else {
            return Err(TypeError::Expected("primitive user task field"));
        };
        if !matches!(
            VariableType::try_from(kind),
            Ok(VariableType::Str | VariableType::Int | VariableType::Bool | VariableType::Double)
        ) {
            return Err(TypeError::Expected(
                "STR, INT, BOOL, or DOUBLE user task field",
            ));
        }
        Ok(UserTaskField {
            display_name: name.clone(),
            name,
            r#type: kind,
            required,
            description: None,
        })
    }
}
/// A set of metadata registrations, applied before the workflow itself.
#[derive(Default)]
pub struct Registrations {
    pub structs: Vec<PutStructDefRequest>,
    pub tasks: Vec<PutTaskDefRequest>,
    pub external_events: Vec<PutExternalEventDefRequest>,
    pub workflow_events: Vec<PutWorkflowEventDefRequest>,
    pub user_tasks: Vec<PutUserTaskDefRequest>,
}
impl Registrations {
    pub async fn register(&self, client: &mut crate::Client) -> Result<(), tonic::Status> {
        for definition in &self.structs {
            client.put_struct_def(definition.clone()).await?;
        }
        for definition in &self.tasks {
            client.put_task_def(definition.clone()).await?;
        }
        for definition in &self.external_events {
            client.put_external_event_def(definition.clone()).await?;
        }
        for definition in &self.workflow_events {
            client.put_workflow_event_def(definition.clone()).await?;
        }
        for definition in &self.user_tasks {
            client.put_user_task_def(definition.clone()).await?;
        }
        Ok(())
    }
    pub async fn register_workflow(
        &self,
        client: &mut crate::Client,
        mut workflow: PutWfSpecRequest,
    ) -> Result<WfSpec, tonic::Status> {
        self.register(client).await?;
        pin_user_task_versions(client, &mut workflow).await?;
        client
            .put_wf_spec(workflow)
            .await
            .map(|response| response.into_inner())
    }
}
/// Timer actions for user task assignment and reminder lifecycle hooks.
pub fn user_task_timer(
    delay: crate::Expression,
    hook: ut_action_trigger::UtHook,
    action: ut_action_trigger::Action,
) -> UtActionTrigger {
    UtActionTrigger {
        delay_seconds: Some(delay.0),
        hook: hook as i32,
        action: Some(action),
    }
}
pub fn cancel_user_task(
    delay: crate::Expression,
    hook: ut_action_trigger::UtHook,
) -> UtActionTrigger {
    user_task_timer(
        delay,
        hook,
        ut_action_trigger::Action::Cancel(ut_action_trigger::UtaCancel {}),
    )
}
pub fn reassign_user_task(
    delay: crate::Expression,
    hook: ut_action_trigger::UtHook,
    user: Option<crate::Expression>,
    group: Option<crate::Expression>,
) -> UtActionTrigger {
    user_task_timer(
        delay,
        hook,
        ut_action_trigger::Action::Reassign(ut_action_trigger::UtaReassign {
            user_id: user.map(|value| value.0),
            user_group: group.map(|value| value.0),
        }),
    )
}
pub fn remind_user_task(
    delay: crate::Expression,
    hook: ut_action_trigger::UtHook,
    task: crate::TaskOptions,
) -> UtActionTrigger {
    user_task_timer(
        delay,
        hook,
        ut_action_trigger::Action::Task(ut_action_trigger::UtaTask {
            task: Some(task.task),
            mutations: vec![],
        }),
    )
}

/// Resolve latest human-task definitions before server-side output-type inference.
/// Explicit versions are preserved. A just-created definition may take up to five
/// seconds to become visible; only NOT_FOUND lookups are retried with bounded backoff.
/// Compilation itself stays offline and deterministic.
pub async fn pin_user_task_versions(
    client: &mut crate::Client,
    workflow: &mut PutWfSpecRequest,
) -> Result<(), tonic::Status> {
    let mut resolved = std::collections::HashMap::new();
    for thread in workflow.thread_specs.values_mut() {
        for node in thread.nodes.values_mut() {
            if let Some(node::Node::UserTask(task)) = &mut node.node
                && task.user_task_def_version.is_none()
            {
                let version = if let Some(version) = resolved.get(&task.user_task_def_name) {
                    *version
                } else {
                    let deadline = tokio::time::Instant::now() + std::time::Duration::from_secs(5);
                    let mut delay = std::time::Duration::from_millis(25);
                    let definition = loop {
                        match client
                            .get_latest_user_task_def(GetLatestUserTaskDefRequest {
                                name: task.user_task_def_name.clone(),
                            })
                            .await
                        {
                            Ok(response) => break response.into_inner(),
                            Err(error)
                                if error.code() == tonic::Code::NotFound
                                    && tokio::time::Instant::now() < deadline =>
                            {
                                tokio::time::sleep(delay).await;
                                delay = (delay * 2).min(std::time::Duration::from_millis(200));
                            }
                            Err(error) => return Err(error),
                        }
                    };
                    if definition.name != task.user_task_def_name || definition.version < 0 {
                        return Err(tonic::Status::internal(
                            "invalid concrete user-task definition returned by metadata lookup",
                        ));
                    }
                    resolved.insert(task.user_task_def_name.clone(), definition.version);
                    definition.version
                };
                task.user_task_def_version = Some(version);
            }
        }
    }
    Ok(())
}
