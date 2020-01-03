package net.daimatz.raft;

import com.google.common.util.concurrent.ListenableFuture;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import net.daimatz.raft.proto.RaftServerGrpc;
import net.daimatz.raft.proto.rpc.AppendEntriesRequest;
import net.daimatz.raft.proto.rpc.AppendEntriesResponse;
import net.daimatz.raft.proto.rpc.RequestVoteRequest;
import net.daimatz.raft.proto.rpc.RequestVoteResponse;

public class RaftClient {
    private final RaftServerGrpc.RaftServerFutureStub futureStub;
    private RaftClient(RaftServerGrpc.RaftServerFutureStub futureStub) {
        this.futureStub = futureStub;
    }

    static public RaftClient newClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();
        return new RaftClient(RaftServerGrpc.newFutureStub(channel));
    }

    public ListenableFuture<AppendEntriesResponse> appendEntries(
        AppendEntriesRequest req
    ) {
        return futureStub.appendEntries(req);
    }

    public ListenableFuture<RequestVoteResponse> requestVote(
        RequestVoteRequest req
    ) {
        return futureStub.requestVote(req);
    }
}
