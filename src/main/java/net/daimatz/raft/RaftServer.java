package net.daimatz.raft;

import com.google.protobuf.Message;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

import net.daimatz.raft.proto.RaftServerGrpc;
import net.daimatz.raft.proto.rpc.AppendEntriesRequest;
import net.daimatz.raft.proto.rpc.AppendEntriesResponse;
import net.daimatz.raft.proto.rpc.LogEntry;
import net.daimatz.raft.proto.rpc.RequestVoteRequest;
import net.daimatz.raft.proto.rpc.RequestVoteResponse;

public class RaftServer {
    // constant
    private final int port;
    private final PrintStreamLogger stdout;

    // states
    private Server server;
    private final ConcurrentHashMap<String, String> storage
        = new ConcurrentHashMap<>();
    private final AtomicInteger currentTerm = new AtomicInteger(-1);
    private final AtomicInteger votedFor = new AtomicInteger(-1);
    private final ConcurrentLinkedDeque<LogEntry> log
        = new ConcurrentLinkedDeque<>();

    // utility
    private final Object lock = new Object();

    public RaftServer(int port, PrintStreamLogger stdout) {
        this.stdout = stdout;
        this.port = port;
    }

    public String get(String key) {
        return storage.get(key);
    }

    public void put(String key, String value) {
        storage.put(key, value);
    }

    public void start() throws IOException, InterruptedException {
        synchronized (lock) {
            if (server != null) {
                throw new IllegalStateException("Server is already running!");
            }
            server = ServerBuilder.forPort(port)
                    .addService(new RaftServerImpl(this))
                    .build();
            server.start();
        }
        server.awaitTermination();
    }

    public void stop() throws IOException, InterruptedException {
        synchronized (lock) {
            if (server == null) {
                throw new IllegalStateException("Server is not running!");
            }
            server.shutdown();
            server = null;
        }
    }

    public void log(Message message) {
        stdout.log(message);
    }

    static class RaftServerImpl extends RaftServerGrpc.RaftServerImplBase {
        private final RaftServer parent;
        RaftServerImpl(RaftServer parent) {
            this.parent = parent;
        }
        @Override
        public void appendEntries(
            AppendEntriesRequest req,
            StreamObserver<AppendEntriesResponse> resObserver
        ) {
            parent.log(req);
            AppendEntriesResponse res = AppendEntriesResponse.newBuilder()
                    .setTerm(1)
                    .setSuccess(true)
                    .build();
            resObserver.onNext(res);
            resObserver.onCompleted();
        }
        @Override
        public void requestVote(
            RequestVoteRequest req,
            StreamObserver<RequestVoteResponse> resObserver
        ) {
            parent.log(req);
            RequestVoteResponse res = RequestVoteResponse.newBuilder()
                    .setTerm(1)
                    .setVoteGranted(true)
                    .build();
            resObserver.onNext(res);
            resObserver.onCompleted();
        }
    }
}
