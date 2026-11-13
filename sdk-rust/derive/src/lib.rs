use proc_macro::TokenStream;
use quote::quote;
use syn::{DeriveInput, parse_macro_input};

/// Derive a named native struct schema. `#[lh(name = "...")]` sets the schema name;
/// field attributes support `rename`, `masked`, and `description`.
#[proc_macro_derive(LittleHorseValue, attributes(lh))]
pub fn derive_value(input: TokenStream) -> TokenStream {
    let input = parse_macro_input!(input as DeriveInput);
    expand(input)
        .unwrap_or_else(syn::Error::into_compile_error)
        .into()
}
fn expand(input: DeriveInput) -> syn::Result<proc_macro2::TokenStream> {
    let name = &input.ident;
    let mut schema_name = name.to_string();
    for attribute in &input.attrs {
        if attribute.path().is_ident("lh") {
            attribute.parse_nested_meta(|meta| {
                if meta.path.is_ident("name") {
                    schema_name = meta.value()?.parse::<syn::LitStr>()?.value();
                    Ok(())
                } else {
                    Err(meta.error("supported struct attribute: name"))
                }
            })?;
        }
    }
    let syn::Data::Struct(data) = &input.data else {
        return Err(syn::Error::new_spanned(
            name,
            "LittleHorseValue requires a struct",
        ));
    };
    let syn::Fields::Named(fields) = &data.fields else {
        return Err(syn::Error::new_spanned(
            name,
            "LittleHorseValue requires named fields",
        ));
    };
    let mut wire_names = std::collections::HashSet::new();
    let mut dependencies = Vec::new();
    let mut definitions = Vec::new();
    let mut serialize = Vec::new();
    let mut deserialize = Vec::new();
    for field in &fields.named {
        let field_name = field.ident.as_ref().unwrap();
        let field_type = &field.ty;
        dependencies.push(quote! { definitions.extend(<#field_type as ::littlehorse::types::LittleHorseValue>::registrations()); });
        let mut wire_name = field_name.to_string();
        let mut masked = false;
        let mut description = None;
        for attribute in &field.attrs {
            if attribute.path().is_ident("lh") {
                attribute.parse_nested_meta(|meta| {
                    if meta.path.is_ident("rename") {
                        wire_name = meta.value()?.parse::<syn::LitStr>()?.value();
                    } else if meta.path.is_ident("masked") {
                        masked = true;
                    } else if meta.path.is_ident("description") {
                        description = Some(meta.value()?.parse::<syn::LitStr>()?.value());
                    } else {
                        return Err(
                            meta.error("supported field attributes: rename, masked, description")
                        );
                    }
                    Ok(())
                })?;
            }
        }
        if !wire_names.insert(wire_name.clone()) {
            return Err(syn::Error::new_spanned(
                field,
                "duplicate LittleHorse field name",
            ));
        }
        let description = match description {
            Some(value) => quote!(Some(#value.into())),
            None => quote!(None),
        };
        definitions.push(quote! { fields.insert(#wire_name.into(), ::littlehorse::proto::StructFieldDef {
            field_type: Some({ let mut definition = <#field_type as ::littlehorse::types::LittleHorseValue>::type_definition(); definition.masked = #masked; definition }),
            is_nullable: <#field_type as ::littlehorse::types::LittleHorseValue>::nullable(), default_value: None, description: #description,
        }); });
        serialize.push(quote! { fields.insert(#wire_name.into(), ::littlehorse::proto::StructField { value: Some(<#field_type as ::littlehorse::types::LittleHorseValue>::to_lh(&self.#field_name)?), masked: #masked }); });
        deserialize.push(quote! { #field_name: <#field_type as ::littlehorse::types::LittleHorseValue>::from_lh(content.fields.get(#wire_name).and_then(|field| field.value.as_ref()).unwrap_or(&::littlehorse::proto::VariableValue::default()))? });
    }
    let mut generics = input.generics.clone();
    for field in &fields.named {
        let ty = &field.ty;
        generics
            .make_where_clause()
            .predicates
            .push(syn::parse_quote!(#ty: ::littlehorse::types::LittleHorseValue));
    }
    let (impl_generics, type_generics, where_clause) = generics.split_for_impl();
    Ok(quote! {
        impl #impl_generics ::littlehorse::types::StructSchema for #name #type_generics #where_clause {
            const NAME: &'static str = #schema_name;
            fn inline_definition() -> ::littlehorse::proto::InlineStructDef {
                let mut fields = ::std::collections::HashMap::new(); #(#definitions)*
                ::littlehorse::proto::InlineStructDef { fields }
            }
        }
        impl #impl_generics ::littlehorse::types::LittleHorseValue for #name #type_generics #where_clause {
            fn registrations() -> Vec<::littlehorse::proto::PutStructDefRequest> {
                let mut definitions = Vec::new(); #(#dependencies)*
                definitions.push(<Self as ::littlehorse::types::StructSchema>::registration());
                definitions
            }
            fn type_definition() -> ::littlehorse::proto::TypeDefinition {
                ::littlehorse::proto::TypeDefinition { defined_type: Some(::littlehorse::proto::type_definition::DefinedType::StructDefId(::littlehorse::proto::StructDefId { name: #schema_name.into(), version: -1 })), masked: false }
            }
            fn to_lh(&self) -> Result<::littlehorse::proto::VariableValue, ::littlehorse::types::TypeError> {
                let mut fields = ::std::collections::HashMap::new(); #(#serialize)*
                Ok(::littlehorse::proto::VariableValue { value: Some(::littlehorse::proto::variable_value::Value::Struct(::littlehorse::proto::Struct {
                    struct_def_id: Some(::littlehorse::proto::StructDefId { name: #schema_name.into(), version: -1 }), r#struct: Some(::littlehorse::proto::InlineStruct { fields })
                })) })
            }
            fn from_lh(value: &::littlehorse::proto::VariableValue) -> Result<Self, ::littlehorse::types::TypeError> {
                let Some(::littlehorse::proto::variable_value::Value::Struct(value)) = &value.value else { return Err(::littlehorse::types::TypeError::Expected("STRUCT")); };
                let content = value.r#struct.as_ref().ok_or(::littlehorse::types::TypeError::Expected("struct content"))?;
                Ok(Self { #(#deserialize),* })
            }
        }
    })
}
