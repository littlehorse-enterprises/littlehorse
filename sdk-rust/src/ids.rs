//! Stable IDs compatible with the Java SDK's string representations.
use crate::proto::WfRunId;
pub fn wf_run_id_string(id: &WfRunId) -> String {
    match &id.parent_wf_run_id {
        Some(parent) => format!("{}_{}", wf_run_id_string(parent), id.id),
        None => id.id.clone(),
    }
}
pub fn parse_wf_run_id(value: &str) -> Result<WfRunId, &'static str> {
    let mut parent = None;
    for segment in value.split('_') {
        if segment.is_empty() {
            return Err("workflow ID segments cannot be empty");
        }
        parent = Some(WfRunId {
            id: segment.into(),
            parent_wf_run_id: parent.map(Box::new),
        });
    }
    parent.ok_or("workflow ID cannot be empty")
}
