package eflect.sampler;

import eflect.protos.sample.DataSet;
import eflect.protos.sampler.ReadRequest;
import eflect.protos.sampler.SamplerGrpc;
import eflect.protos.sampler.StartRequest;
import eflect.protos.sampler.StopRequest;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;

/** Client that can talk to an eflect sampler. */
public final class SamplerClient extends SamplerGrpc.SamplerImplBase {
  private final SamplerGrpc.SamplerBlockingStub stub;

  public SamplerClient(Channel channel) {
    stub = SamplerGrpc.newBlockingStub(channel);
  }

  /** Tells eflect to monitor our runtime. */
  public void start() {
    start(ProcessHandle.current().pid());
  }

  /** Tells eflect to monitor the runtime with {@code pid}. */
  public void start(long pid) {
    try {
      stub.start(StartRequest.newBuilder().setPid(pid).build());
    } catch (StatusRuntimeException e) {
      return;
    }
  }

  /** Tells eflect to stop monitoring. */
  public void stop() {
    try {
      stub.stop(StopRequest.getDefaultInstance());
    } catch (StatusRuntimeException e) {
      return;
    }
  }

  /** Asks eflect for the currently recorded data. */
  public DataSet read() {
    try {
      return stub.read(ReadRequest.getDefaultInstance()).getData();
    } catch (StatusRuntimeException e) {
      return DataSet.getDefaultInstance();
    }
  }

  public static void main(String[] args) throws Exception {
    String command = args[0];
    long pid = 1; // Long.parseLong(args[2]);

    // Channels are secure by default (via SSL/TLS). For the example we disable TLS
    // to avoid needing certificates.
    ManagedChannel channel = ManagedChannelBuilder.forTarget("[::1]:50051").usePlaintext().build();

    try {
      SamplerClient client = new SamplerClient(channel);
      switch (command) {
        case "start":
          client.start(pid);
          break;
        case "stop":
          client.stop();
          break;
        case "read":
          System.out.println(
              client.read().getTaskList().stream().reduce((a, b) -> b).orElse(null).getTimestamp());
          break;
        case "test":
          client.start(pid);
          Thread.sleep(1000);
          client.stop();
          System.out.println(
              client.read().getTaskList().stream().reduce((a, b) -> b).orElse(null).getTimestamp());
          break;
      }
    } finally {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
