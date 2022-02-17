mod protos {
    tonic::include_proto!("eflect.protos.sample");
}

use clap::App;

use protos::sampler_client::SamplerClient;
use protos::{ReadRequest, StartRequest, StopRequest};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let matches = App::new("eflect")
        .subcommand(App::new("start"))
        .subcommand(App::new("stop"))
        .subcommand(App::new("read"))
        .arg_from_usage("--pid=<pid> 'The id of the process to monitor'")
        .get_matches();
    let (cmd, _) = matches.subcommand();

    let pid: Option<u64> = Some(matches.value_of("pid").unwrap().parse().unwrap());
    let mut client = SamplerClient::connect("http://[::1]:50051").await?;
    match cmd {
        "start" => {
            client
                .start(tonic::Request::new(StartRequest { pid }))
                .await?;
            ()
        }
        "stop" => {
            client
                .stop(tonic::Request::new(StopRequest { pid }))
                .await?;
            ()
        }
        "read" => println!(
            "{:?}",
            client
                .read(tonic::Request::new(ReadRequest { pid }))
                .await?
        ),
        _ => println!("don't understand {}", cmd),
    };

    Ok(())
}
