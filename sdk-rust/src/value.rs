//! Conversion between serde data and LittleHorse primitive/JSON values.
use crate::proto::{VariableValue, variable_value::Value};
use serde::{Serialize, de::DeserializeOwned};

#[derive(Debug, thiserror::Error)]
pub enum ValueError {
    #[error(transparent)]
    Json(#[from] serde_json::Error),
    #[error("integer exceeds LittleHorse's signed 64-bit range")]
    IntegerOverflow,
    #[error(
        "use protobuf values directly for native arrays, maps, structs, IDs, timestamps, or bytes"
    )]
    UnsupportedType,
    #[error("non-finite floating point values cannot be represented in JSON")]
    NonFinite,
}
/// Serialize a Rust value. Objects and lists use JSON_OBJ and JSON_ARR.
pub fn to_value(value: &impl Serialize) -> Result<VariableValue, ValueError> {
    let value = match serde_json::to_value(value)? {
        serde_json::Value::Null => None,
        serde_json::Value::Bool(value) => Some(Value::Bool(value)),
        serde_json::Value::String(value) => Some(Value::Str(value)),
        serde_json::Value::Number(value) if value.is_i64() => {
            Some(Value::Int(value.as_i64().unwrap()))
        }
        serde_json::Value::Number(value) if value.is_u64() => {
            return Err(ValueError::IntegerOverflow);
        }
        serde_json::Value::Number(value) => Some(Value::Double(value.as_f64().unwrap())),
        value @ serde_json::Value::Array(_) => Some(Value::JsonArr(value.to_string())),
        value @ serde_json::Value::Object(_) => Some(Value::JsonObj(value.to_string())),
    };
    Ok(VariableValue { value })
}
/// Deserialize a primitive or JSON value into Rust data.
pub fn from_value<T: DeserializeOwned>(value: &VariableValue) -> Result<T, ValueError> {
    let json = match &value.value {
        None => serde_json::Value::Null,
        Some(Value::Bool(value)) => (*value).into(),
        Some(Value::Str(value)) => value.clone().into(),
        Some(Value::Int(value)) => (*value).into(),
        Some(Value::Double(value)) => serde_json::Number::from_f64(*value)
            .ok_or(ValueError::NonFinite)?
            .into(),
        Some(Value::JsonObj(value) | Value::JsonArr(value)) => serde_json::from_str(value)?,
        _ => return Err(ValueError::UnsupportedType),
    };
    Ok(serde_json::from_value(json)?)
}
