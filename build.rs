extern crate tonic_build;

fn main() -> std::io::Result<()> {
    tonic_build::configure()
        .compile(&[
            "protos/sample/jiffies.proto",
            "protos/sample/rapl.proto",
            "protos/sample/sample.proto",
            "protos/sample/sampler.proto"
        ], &["."])?;
    Ok(())
}
