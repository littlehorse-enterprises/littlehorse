fn main() -> Result<(), Box<dyn std::error::Error>> {
    let manifest = std::path::PathBuf::from(std::env::var("CARGO_MANIFEST_DIR")?);
    let schemas = if let Some(path) = std::env::var_os("LITTLEHORSE_SCHEMA_DIR") {
        std::path::PathBuf::from(path)
    } else if manifest
        .join("../schemas/littlehorse/service.proto")
        .is_file()
    {
        manifest.join("../schemas/littlehorse")
    } else {
        manifest.join("schemas")
    };
    let mut config = tonic_prost_build::Config::new();
    config.protoc_executable(protoc_bin_vendored::protoc_bin_path()?);
    tonic_prost_build::configure()
        .file_descriptor_set_path(
            std::path::PathBuf::from(std::env::var("OUT_DIR")?).join("littlehorse-descriptor.bin"),
        )
        .build_server(true)
        .generate_default_stubs(true)
        .compile_with_config(
            config,
            &[schemas.join("service.proto")],
            std::slice::from_ref(&schemas),
        )?;
    println!("cargo:rerun-if-env-changed=LITTLEHORSE_SCHEMA_DIR");
    println!("cargo:rerun-if-changed={}", schemas.display());
    Ok(())
}
