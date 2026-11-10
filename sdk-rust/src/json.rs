//! Canonical protobuf JSON for workflow review and portable saved definitions.
use crate::proto::PutWfSpecRequest;
use prost::Message;
use prost_reflect::{DescriptorPool, DynamicMessage};
pub fn workflow_to_json(
    request: &PutWfSpecRequest,
) -> Result<String, Box<dyn std::error::Error + Send + Sync>> {
    let pool = DescriptorPool::decode(
        include_bytes!(concat!(env!("OUT_DIR"), "/littlehorse-descriptor.bin")).as_ref(),
    )?;
    let descriptor = pool
        .get_message_by_name("littlehorse.PutWfSpecRequest")
        .ok_or("missing workflow descriptor")?;
    let dynamic = DynamicMessage::decode(descriptor, request.encode_to_vec().as_slice())?;
    Ok(serde_json::to_string_pretty(&dynamic)?)
}
pub fn workflow_from_json(
    json: &str,
) -> Result<PutWfSpecRequest, Box<dyn std::error::Error + Send + Sync>> {
    let pool = DescriptorPool::decode(
        include_bytes!(concat!(env!("OUT_DIR"), "/littlehorse-descriptor.bin")).as_ref(),
    )?;
    let descriptor = pool
        .get_message_by_name("littlehorse.PutWfSpecRequest")
        .ok_or("missing workflow descriptor")?;
    let mut deserializer = serde_json::Deserializer::from_str(json);
    let dynamic = DynamicMessage::deserialize(descriptor, &mut deserializer)?;
    deserializer.end()?;
    Ok(PutWfSpecRequest::decode(
        dynamic.encode_to_vec().as_slice(),
    )?)
}

/// Encode any generated message using the shared descriptor set and protobuf JSON defaults.
pub fn message_to_json_value<M: Message>(
    name: &str,
    message: &M,
) -> Result<serde_json::Value, crate::client::ClientError> {
    let pool = DescriptorPool::decode(
        include_bytes!(concat!(env!("OUT_DIR"), "/littlehorse-descriptor.bin")).as_ref(),
    )?;
    let descriptor = pool
        .get_message_by_name(name)
        .ok_or("unknown protobuf message")?;
    let dynamic = DynamicMessage::decode(descriptor, message.encode_to_vec().as_slice())?;
    Ok(dynamic.serialize_with_options(
        serde_json::value::Serializer,
        &prost_reflect::SerializeOptions::new().skip_default_fields(false),
    )?)
}
