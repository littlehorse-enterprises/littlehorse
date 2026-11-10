use base64::Engine;
use littlehorse::{proto::*, types::Bytes, *};
pub fn convert(kind: &str, value: Option<&str>) -> Result<VariableValue, client::ClientError> {
    let text = value.unwrap_or_default();
    Ok(match kind {
        "str" => text.to_owned().to_lh()?,
        "int" => text.parse::<i64>()?.to_lh()?,
        "double" => text.parse::<f64>()?.to_lh()?,
        "bool" => text.parse::<bool>()?.to_lh()?,
        "bytes" => Bytes(base64::engine::general_purpose::STANDARD.decode(text)?).to_lh()?,
        "timestamp" => {
            let millis = text.parse::<i64>()?;
            prost_types::Timestamp {
                seconds: millis.div_euclid(1000),
                nanos: (millis.rem_euclid(1000) * 1_000_000) as i32,
            }
            .to_lh()?
        }
        "json-obj" | "json-arr" => {
            let value: serde_json::Value = serde_json::from_str(text)?;
            if (kind == "json-obj" && !value.is_object())
                || (kind == "json-arr" && !value.is_array())
            {
                return Err("JSON input has the wrong type".into());
            }
            to_value(&value)?
        }
        "null" => ().to_lh()?,
        _ => return Err(format!("unknown conversion type {kind}").into()),
    })
}
