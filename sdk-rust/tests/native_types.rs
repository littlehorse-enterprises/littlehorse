use littlehorse::{LittleHorseValue, StructSchema, Variable, Workflow, json, proto::*, types::*};
use std::collections::HashMap;
#[derive(Clone, Debug, PartialEq, LittleHorseValue)]
#[lh(name = "customer")]
struct Customer {
    #[lh(rename = "full-name", description = "Customer display name")]
    name: String,
    #[lh(masked)]
    balance: i64,
    tags: Vec<String>,
    metadata: HashMap<String, i64>,
    optional: Option<String>,
}
#[test]
fn native_struct_array_map_and_null_round_trip() {
    let customer = Customer {
        name: "Ada".into(),
        balance: i64::MAX,
        tags: vec!["priority".into()],
        metadata: HashMap::from([("score".into(), 42)]),
        optional: None,
    };
    assert_eq!(
        Customer::from_lh(&customer.to_lh().unwrap()).unwrap(),
        customer
    );
    let schema = Customer::inline_definition();
    assert!(schema.fields["balance"].field_type.as_ref().unwrap().masked);
    assert!(schema.fields["optional"].is_nullable);
    assert_eq!(Customer::registration().name, "customer");
    assert_eq!(
        schema.fields["full-name"].description.as_deref(),
        Some("Customer display name")
    );
    let inline = Inline(customer);
    assert_eq!(
        Inline::<Customer>::from_lh(&inline.to_lh().unwrap()).unwrap(),
        inline
    );
    assert_eq!(
        Bytes::from_lh(&Bytes(vec![0, 255]).to_lh().unwrap()).unwrap(),
        Bytes(vec![0, 255])
    );
    assert!(f64::NAN.to_lh().is_err());
    assert!(i32::from_lh(&i64::MAX.to_lh().unwrap()).is_err());
}
#[test]
fn protobuf_json_is_portable_and_rejects_extra_data() {
    let request = Workflow::define("json-round-trip", |thread| {
        thread.declare(Variable::typed::<Customer>("customer"));
        thread.return_value("done".into());
        Ok(())
    })
    .unwrap()
    .build()
    .unwrap();
    let json = json::workflow_to_json(&request).unwrap();
    assert_eq!(json::workflow_from_json(&json).unwrap(), request);
    assert!(json::workflow_from_json(&(json + " {} ")).is_err());
}
#[test]
fn configuration_validates_partial_credentials_and_redacts_secrets() {
    let config = littlehorse::ClientConfig::from_properties(HashMap::from([
        ("LHC_API_PORT".into(), "2024".into()),
        ("LHC_OAUTH_CLIENT_ID".into(), "id".into()),
        ("LHC_OAUTH_CLIENT_SECRET".into(), "hidden-secret".into()),
        (
            "LHC_OAUTH_ACCESS_TOKEN_URL".into(),
            "https://issuer.example/token".into(),
        ),
    ]))
    .unwrap();
    assert_eq!(config.endpoint, "http://localhost:2024");
    assert!(!format!("{config:?}").contains("hidden-secret"));
    assert!(
        littlehorse::ClientConfig::from_properties(HashMap::from([(
            "LHC_OAUTH_CLIENT_ID".into(),
            "id".into()
        )]))
        .is_err()
    );
}
#[tokio::test]
async fn pagination_retries_failed_pages_and_stops_at_end() {
    let mut pages = littlehorse::pagination::Paginator::new(|cursor: Option<Vec<u8>>| async move {
        Ok(littlehorse::pagination::Page {
            values: vec![cursor.is_some()],
            bookmark: if cursor.is_none() {
                Some(vec![1])
            } else {
                None
            },
        })
    });
    assert_eq!(pages.next_page().await.unwrap(), Some(vec![false]));
    assert_eq!(pages.next_page().await.unwrap(), Some(vec![true]));
    assert_eq!(pages.next_page().await.unwrap(), None);
}
#[test]
fn user_task_schema_checks_types_and_duplicates() {
    use littlehorse::schema::UserTaskSchema;
    let field = UserTaskSchema::typed_field::<String>("approval", true).unwrap();
    assert!(
        UserTaskSchema::new("approval")
            .field(field.clone())
            .compile()
            .is_ok()
    );
    assert!(
        UserTaskSchema::new("approval")
            .field(field.clone())
            .field(field)
            .compile()
            .is_err()
    );
    assert!(UserTaskSchema::typed_field::<Bytes>("bytes", true).is_err());
}
#[test]
fn workflow_ids_preserve_parentage() {
    let id = littlehorse::ids::parse_wf_run_id("parent_child_leaf").unwrap();
    assert_eq!(littlehorse::ids::wf_run_id_string(&id), "parent_child_leaf");
    assert!(littlehorse::ids::parse_wf_run_id("bad__id").is_err());
    let _: VariableValue = id.to_lh().unwrap();
}

#[derive(LittleHorseValue)]
struct Address {
    city: String,
}
#[derive(LittleHorseValue)]
struct CustomerWithAddress {
    address: Address,
}
#[test]
fn inline_schemas_preserve_nested_named_dependencies() {
    let definitions = Inline::<CustomerWithAddress>::registrations();
    assert_eq!(definitions.len(), 1);
    assert_eq!(definitions[0].name, "Address");
    assert_eq!(
        Vec::<Inline<CustomerWithAddress>>::registrations(),
        definitions
    );
}

#[test]
fn struct_backed_forms_accept_zero_based_versions_and_reject_latest_aliases() {
    use littlehorse::schema::UserTaskSchema;
    let zero = StructDefId {
        name: "approval".into(),
        version: 0,
    };
    assert_eq!(
        UserTaskSchema::struct_backed("approval-form", zero.clone())
            .compile()
            .unwrap()
            .result_struct_def_id,
        Some(zero.clone())
    );
    assert!(
        UserTaskSchema::struct_backed(
            "approval-form",
            StructDefId {
                version: -1,
                ..zero.clone()
            }
        )
        .compile()
        .is_err()
    );
    assert!(
        UserTaskSchema::struct_backed("approval-form", zero)
            .field(UserTaskSchema::typed_field::<bool>("approved", true).unwrap())
            .compile()
            .is_err()
    );
}
