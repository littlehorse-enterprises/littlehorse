#![doc = include_str!("../README.md")]
//! Async Rust client, workflow construction, and task workers for LittleHorse.
//! The complete pinned-master protobuf API is available in [`proto`].
pub mod proto {
    #![allow(
        clippy::all,
        rustdoc::broken_intra_doc_links,
        rustdoc::invalid_html_tags
    )]
    tonic::include_proto!("littlehorse");
}
pub mod client;
pub mod value;
pub mod worker;
pub mod workflow;
pub use client::{Client, ClientConfig};
pub use value::{ValueError, from_value, to_value};
pub use workflow::{Expression, Workflow, WorkflowError};

extern crate self as littlehorse;
pub mod types;
pub use littlehorse_derive::LittleHorseValue;
pub use types::{LittleHorseValue, StructSchema};

pub mod auth;

pub mod dsl;
pub use dsl::{EventOptions, NodeOutput, TaskOptions, ThreadBuilder, Variable};

pub mod ids;

pub mod json;
pub mod pagination;
pub mod schema;
