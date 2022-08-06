use std::collections::HashMap;
use std::fs;
use std::io::Read;
use std::vec::Vec;
use std::time::SystemTime;

use android_trace_log;
use android_trace_log::Time;

use serde_derive::Serialize;
use serde_json;

use wild;

#[derive(Serialize)]
pub struct InstantEvent {
    name: Option<String>,
    cat: Option<String>,
    ph: String,
    ts: u128,
    pid: u64,
    tid: u64,
    sf: usize,
}

impl InstantEvent {
    pub fn new(
        event_name: String,
        event_category: Vec<String>,
        process_id: u64,
        thread_id: u64,
        timestamp: u128,
        stack_trace_id: usize,
    ) -> InstantEvent {
        InstantEvent {
            name: Some(event_name),
            cat: Some(event_category.join(",")),
            ph: "i".to_string(),
            ts: timestamp,
            pid: process_id,
            tid: thread_id,
            sf: stack_trace_id,
        }
    }
}

#[derive(Serialize)]
pub enum DisplayTimeUnit {
    ms,
    ns,
}

#[derive(Serialize)]
pub struct StackFrame {
    pub name: String,
    pub category: String,
}

#[derive(Serialize)]
pub struct TraceEvents {
    pub traceEvents: Vec<InstantEvent>,
    pub displayTimeUnit: Option<DisplayTimeUnit>,
    systemTraceEvents: Option<String>,
    otherData: Option<Vec<String>>,
    pub stackFrames: Option<HashMap<usize, StackFrame>>,
    samples: Option<Vec<String>>,
}

impl TraceEvents {
    pub fn new() -> TraceEvents {
        TraceEvents {
            traceEvents: vec![],
            displayTimeUnit: None,
            systemTraceEvents: None,
            otherData: None,
            stackFrames: None,
            samples: None,
        }
    }
}


fn main() -> Result<(), Box<dyn std::error::Error>> {
    for file in wild::args().skip(1) {
        let mut trace_bytes = Vec::new();
        fs::File::open(file.clone())?.read_to_end(&mut trace_bytes)?;
        let trace = android_trace_log::parse(&trace_bytes[..]).unwrap();

        // TODO(atpoverload): this is probably bad since the pid is required; something weird may
        //   happen if don't have a pid?
        let pid = match trace.pid {
            Some(pid) => pid,
            None => 0,
        };
        let start = trace.start_time.timestamp_millis() as u128;

        let mut events = TraceEvents::new();
        events.displayTimeUnit = Some(DisplayTimeUnit::ms);
        for event in trace.events.iter() {
            events.traceEvents.push(InstantEvent::new(
                "".to_string(),
                vec![],
                pid as u64,
                event.thread_id as u64,
                match event.time {
                    Time::Global(time) => start + time.as_millis(),
                    Time::Monotonic{ wall: Some(time), .. } => start + time.as_millis(),
                    _ => 0,
                },
                event.method_id as usize,
            ));
        }
        events.stackFrames = Some(trace.methods.clone().into_iter().map(|m| (
            m.id as usize,
            StackFrame { name: m.name, category: m.class_name}
        )).collect());

        let start = SystemTime::now();
        serde_json::to_writer(&fs::File::create(format!("{}.json", file))?, &events)?;
    }

    Ok(())
}
