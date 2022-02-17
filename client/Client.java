package eflect;

import eflect.protos.sample.DataSet;
import eflect.protos.sample.ReadRequest;
import eflect.protos.sample.SamplerGrpc;
import eflect.protos.sample.StartRequest;
import eflect.protos.sample.StopRequest;
import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.concurrent.TimeUnit;

/** Client that can talk to an eflect sampler. */
public final class Client extends SamplerGrpc.SamplerImplBase {
  private final SamplerGrpc.SamplerBlockingStub stub;

  public Client(Channel channel) {
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
    long pid = Long.parseLong(args[2]);

    ManagedChannel channel =
        ManagedChannelBuilder.forTarget("[::1]:50051")
            // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
            // needing certificates.
            .usePlaintext()
            .build();
    try {
      Client client = new Client(channel);
      switch (command) {
        case "start":
          client.start(pid);
          break;
        case "stop":
          client.stop();
          break;
        case "read":
          System.out.println(client.read());
          break;
      }
    } finally {
      channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
    }
  }
}
