package eflect.sampler;

import static java.util.concurrent.Executors.newScheduledThreadPool;

import eflect.protos.sampler.ReadRequest;
import eflect.protos.sampler.ReadResponse;
import eflect.protos.sampler.SamplerGrpc;
import eflect.protos.sampler.StartRequest;
import eflect.protos.sampler.StartResponse;
import eflect.protos.sampler.StopRequest;
import eflect.protos.sampler.StopResponse;
import eflect.sample.JiffiesDataSources;
import eflect.sample.RaplDataSources;
import eflect.sample.SampleCollector;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/** A clerk that collects samples into a data set. */
public final class SamplerServer {
  private static final Duration DEFAULT_PERIOD = Duration.ofMillis(50);
  private static final AtomicInteger counter = new AtomicInteger();
  private static final ThreadFactory threadFactory =
      r -> {
        Thread t = new Thread(r, "eflect-" + counter.getAndIncrement());
        t.setDaemon(true);
        return t;
      };

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;
    ScheduledExecutorService executor = newScheduledThreadPool(3, threadFactory);
    server =
        ServerBuilder.forPort(port)
            .addService(new SamplerImpl(executor, DEFAULT_PERIOD))
            .build()
            .start();
    System.out.println("Server started, listening on " + port);
    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown
                // hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                  SamplerServer.this.stop();
                } catch (InterruptedException e) {
                  e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
              }
            });
  }

  private void stop() throws InterruptedException {
    if (server != null) {
      server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
    }
  }

  /** Await termination on the main thread since the grpc library uses daemon threads. */
  private void blockUntilShutdown() throws InterruptedException {
    if (server != null) {
      server.awaitTermination();
    }
  }

  static class SamplerImpl extends SamplerGrpc.SamplerImplBase {
    private final SampleCollector collector;
    private final Duration period;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);

    public SamplerImpl(ScheduledExecutorService executor, Duration period) {
      this.collector = new SampleCollector(executor);
      this.period = period;
    }

    @Override
    public void start(StartRequest req, StreamObserver<StartResponse> responseObserver) {
      if (!isRunning.getAndSet(true)) {
        collector.start(JiffiesDataSources::sampleCpuStats, period);
        collector.start(RaplDataSources::sampleRapl, period);
        collector.start(JiffiesDataSources::sampleTaskStats, period);
      }

      responseObserver.onNext(StartResponse.getDefaultInstance());
      responseObserver.onCompleted();
    }

    @Override
    public void stop(StopRequest req, StreamObserver<StopResponse> responseObserver) {
      if (isRunning.getAndSet(false)) {
        collector.stop();
      }

      responseObserver.onNext(StopResponse.getDefaultInstance());
      responseObserver.onCompleted();
    }

    @Override
    public void read(ReadRequest req, StreamObserver<ReadResponse> responseObserver) {
      responseObserver.onNext(ReadResponse.newBuilder().setData(collector.read()).build());
      responseObserver.onCompleted();
    }
  }

  public static void main(String[] args) throws Exception {
    SamplerServer server = new SamplerServer();
    server.start();
    server.blockUntilShutdown();
  }
}
