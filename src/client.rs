// a simple client that talks to an eflect server. currently assumes the server can only watch
// a single process
mod protos {
    tonic::include_proto!("eflect.protos.sample");
}

use std::fs::File;
use std::io::Write;

use bytes::buf::BufMut;
use clap::{App, Arg};
use prost::Message;

use protos::sampler_client::SamplerClient;
use protos::{ReadRequest, StartRequest, StopRequest};

#[tokio::main]
async fn main() -> Result<(), Box<dyn std::error::Error>> {
    let matches = App::new("eflect")
        .subcommand(App::new("start")
            .arg_from_usage("--pid=<pid> 'The id of the process to monitor'"))
        .subcommand(App::new("stop"))
        .subcommand(App::new("read")
            .arg_from_usage("--output=[output] 'The path to write the data set to'"))
        .subcommand(App::new("ping"))
        .arg_from_usage("--addr [address] 'The address the eflect server is hosted at'")
        .get_matches();
    let (cmd, submatches) = matches.subcommand();

    let mut client = SamplerClient::connect("http://[::1]:50051").await?;
    match cmd {
        "start" => client
            .start(tonic::Request::new(StartRequest {
                pid: submatches.unwrap().value_of("pid").unwrap().parse().ok()
            }))
            .await?
        "stop" => client.stop(tonic::Request::new(StopRequest { pid: None })).await?
        "read" => {
            let message = client
                .read(tonic::Request::new(ReadRequest { pid: None }))
                .await?;
            let message = message.get_ref().data.as_ref().unwrap();
            match submatches.unwrap().value_of("output") {
                Some(path) => {
                    let mut buffer = vec![];
                    match message.encode(&mut buffer) {
                        Ok(_) => {
                            let mut file = File::create(path)?;
                            file.write_all(&buffer)?;
                        }
                        Err(e) => println!("error encoding message: {}", e)
                    }
                }
                _ => println!("{:?}", message)
            }
        }
        "ping" => {
            client
                .start(tonic::Request::new(StartRequest { pid: Some(1) }))
                .await?;
            std::thread::sleep(std::time::Duration::from_secs(1));
            client
                .stop(tonic::Request::new(StopRequest { pid: None }))
                .await?;
            let message = client
                .read(tonic::Request::new(ReadRequest { pid: None }))
                .await?;
            let message = message.get_ref().data.as_ref().unwrap();
            let mut buffer = vec![];
            match message.encode(&mut buffer) {
                Ok(_) => {
                    let mut file = File::create("eflect-ping-data.pb")?;
                    file.write_all(&buffer)?;
                }
                Err(e) => println!("error encoding message: {}", e)
            }
        }
        _ => println!("don't understand {}", cmd),
    };

    Ok(())
}
