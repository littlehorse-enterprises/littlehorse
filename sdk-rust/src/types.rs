//! Typed native values. Implement `LittleHorseValue` for custom adapters or derive it for structs.
use crate::proto::*;
use std::collections::HashMap;

#[derive(Debug, thiserror::Error)]
pub enum TypeError {
    #[error("expected {0}")]
    Expected(&'static str),
    #[error("missing field {0}")]
    MissingField(String),
    #[error("numeric value is outside the target type's range")]
    OutOfRange,
}
pub trait LittleHorseValue: Sized {
    fn type_definition() -> TypeDefinition;
    fn to_lh(&self) -> Result<VariableValue, TypeError>;
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError>;
    fn registrations() -> Vec<PutStructDefRequest> {
        vec![]
    }
    fn nullable() -> bool {
        false
    }
}
macro_rules! primitive {
    ($rust:ty, $variant:ident, $kind:ident, $label:literal) => {
        impl LittleHorseValue for $rust {
            fn type_definition() -> TypeDefinition {
                crate::workflow::primitive_type(VariableType::$kind)
            }
            fn to_lh(&self) -> Result<VariableValue, TypeError> {
                Ok(VariableValue {
                    value: Some(variable_value::Value::$variant(self.clone())),
                })
            }
            fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
                match &value.value {
                    Some(variable_value::Value::$variant(value)) => Ok(value.clone()),
                    _ => Err(TypeError::Expected($label)),
                }
            }
        }
    };
}
primitive!(String, Str, Str, "STR");
primitive!(i64, Int, Int, "INT");
primitive!(bool, Bool, Bool, "BOOL");
primitive!(WfRunId, WfRunId, WfRunId, "WF_RUN_ID");
primitive!(prost_types::Timestamp, UtcTimestamp, Timestamp, "TIMESTAMP");
impl LittleHorseValue for f64 {
    fn type_definition() -> TypeDefinition {
        crate::workflow::primitive_type(VariableType::Double)
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        if !self.is_finite() {
            return Err(TypeError::OutOfRange);
        }
        Ok(VariableValue {
            value: Some(variable_value::Value::Double(*self)),
        })
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        match value.value {
            Some(variable_value::Value::Double(value)) if value.is_finite() => Ok(value),
            _ => Err(TypeError::Expected("finite DOUBLE")),
        }
    }
}
impl LittleHorseValue for i32 {
    fn type_definition() -> TypeDefinition {
        i64::type_definition()
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        i64::from(*self).to_lh()
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        i64::from_lh(value)?
            .try_into()
            .map_err(|_| TypeError::OutOfRange)
    }
}
impl LittleHorseValue for () {
    fn type_definition() -> TypeDefinition {
        TypeDefinition::default()
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        Ok(VariableValue::default())
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        if value.value.is_none() {
            Ok(())
        } else {
            Err(TypeError::Expected("NULL"))
        }
    }
}
impl<T: LittleHorseValue> LittleHorseValue for Option<T> {
    fn registrations() -> Vec<PutStructDefRequest> {
        T::registrations()
    }
    fn type_definition() -> TypeDefinition {
        T::type_definition()
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        match self {
            Some(value) => value.to_lh(),
            None => Ok(VariableValue::default()),
        }
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        if value.value.is_none() {
            Ok(None)
        } else {
            T::from_lh(value).map(Some)
        }
    }
    fn nullable() -> bool {
        true
    }
}
#[derive(Clone, Debug, PartialEq, Eq)]
pub struct Bytes(pub Vec<u8>);
impl LittleHorseValue for Bytes {
    fn type_definition() -> TypeDefinition {
        crate::workflow::primitive_type(VariableType::Bytes)
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        Ok(VariableValue {
            value: Some(variable_value::Value::Bytes(self.0.clone())),
        })
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        match &value.value {
            Some(variable_value::Value::Bytes(bytes)) => Ok(Self(bytes.clone())),
            _ => Err(TypeError::Expected("BYTES")),
        }
    }
}
impl<T: LittleHorseValue> LittleHorseValue for Vec<T> {
    fn registrations() -> Vec<PutStructDefRequest> {
        T::registrations()
    }
    fn type_definition() -> TypeDefinition {
        TypeDefinition {
            defined_type: Some(type_definition::DefinedType::InlineArrayDef(Box::new(
                InlineArrayDef {
                    array_type: Some(Box::new(T::type_definition())),
                },
            ))),
            masked: false,
        }
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        Ok(VariableValue {
            value: Some(variable_value::Value::Array(Array {
                items: self.iter().map(T::to_lh).collect::<Result<_, _>>()?,
                element_type: Some(T::type_definition()),
            })),
        })
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        match &value.value {
            Some(variable_value::Value::Array(array)) => {
                array.items.iter().map(T::from_lh).collect()
            }
            _ => Err(TypeError::Expected("ARRAY")),
        }
    }
}
/// Only primitive values can be map keys. Implementations for structs and collections are intentionally absent.
pub trait MapKey: LittleHorseValue + Eq + std::hash::Hash {}
impl MapKey for String {}
impl MapKey for i64 {}
impl MapKey for i32 {}
impl MapKey for bool {}
impl<K: MapKey, V: LittleHorseValue> LittleHorseValue for HashMap<K, V> {
    fn registrations() -> Vec<PutStructDefRequest> {
        V::registrations()
    }
    fn type_definition() -> TypeDefinition {
        TypeDefinition {
            defined_type: Some(type_definition::DefinedType::InlineMapDef(Box::new(
                map_type::<K, V>(),
            ))),
            masked: false,
        }
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        let entries = self
            .iter()
            .map(|(key, value)| {
                Ok(map::Entry {
                    key: Some(key.to_lh()?),
                    value: Some(value.to_lh()?),
                })
            })
            .collect::<Result<_, TypeError>>()?;
        Ok(VariableValue {
            value: Some(variable_value::Value::Map(Map {
                entries,
                map_type: Some(map_type::<K, V>()),
            })),
        })
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        let Some(variable_value::Value::Map(map)) = &value.value else {
            return Err(TypeError::Expected("MAP"));
        };
        map.entries
            .iter()
            .map(|entry| {
                Ok((
                    K::from_lh(entry.key.as_ref().ok_or(TypeError::Expected("map key"))?)?,
                    V::from_lh(
                        entry
                            .value
                            .as_ref()
                            .ok_or(TypeError::Expected("map value"))?,
                    )?,
                ))
            })
            .collect()
    }
}
fn map_type<K: LittleHorseValue, V: LittleHorseValue>() -> InlineMapDef {
    InlineMapDef {
        key_type: Some(Box::new(K::type_definition())),
        value_type: Some(Box::new(V::type_definition())),
    }
}
/// Struct definitions associated with a Rust type, including a stable registered name.
pub trait StructSchema: LittleHorseValue {
    const NAME: &'static str;
    fn inline_definition() -> InlineStructDef;
    fn registration() -> PutStructDefRequest {
        PutStructDefRequest {
            name: Self::NAME.into(),
            struct_def: Some(Self::inline_definition()),
            ..Default::default()
        }
    }
}
/// Use a struct type's schema inline, without requiring registration of its named definition.
#[derive(Clone, Debug, PartialEq)]
pub struct Inline<T>(pub T);
impl<T: StructSchema> LittleHorseValue for Inline<T> {
    fn registrations() -> Vec<PutStructDefRequest> {
        T::registrations()
            .into_iter()
            .filter(|definition| definition.name != T::NAME)
            .collect()
    }
    fn type_definition() -> TypeDefinition {
        TypeDefinition {
            defined_type: Some(type_definition::DefinedType::InlineStructDef(
                T::inline_definition(),
            )),
            masked: false,
        }
    }
    fn to_lh(&self) -> Result<VariableValue, TypeError> {
        let mut value = self.0.to_lh()?;
        if let Some(variable_value::Value::Struct(value)) = &mut value.value {
            value.struct_def_id = None;
        }
        Ok(value)
    }
    fn from_lh(value: &VariableValue) -> Result<Self, TypeError> {
        T::from_lh(value).map(Self)
    }
}
