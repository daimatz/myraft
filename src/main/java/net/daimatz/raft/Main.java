package net.daimatz.raft;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.concurrent.Executors;

import net.daimatz.raft.proto.rpc.AppendEntriesRequest;
import net.daimatz.raft.proto.rpc.AppendEntriesResponse;

public class Main {
    private final ListeningExecutorService service
        = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(10));
    private final PrintStreamLogger stdout = new PrintStreamLogger(System.out);

    public static void main(String[] args) throws Exception {
        final int port = Integer.parseInt(args[0]);
        Main m = new Main();
        m.startAnotherThread(port);
        m.startServerAndWait(port);
    }

    public void startServerAndWait(int port) {
        RaftServer server = new RaftServer(port, stdout);
        try {
            server.start();
        } catch (Throwable e) {
            System.err.println("Failed to start server");
            e.printStackTrace();
        }
    }

    public void startAnotherThread(int port) {
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (Throwable e) {}
                RaftClient client = RaftClient.newClient("127.0.0.1", port);
                AppendEntriesRequest req = AppendEntriesRequest.newBuilder()
                        .setTerm(1)
                        .setLeaderId(2)
                        .setPrevLogIndex(3)
                        .setPrevLogTerm(4)
                        .setLeaderCommit(6)
                        .build();
                ListenableFuture<AppendEntriesResponse> f = client.appendEntries(req);
                Futures.addCallback(
                    f,
                    new FutureCallback<AppendEntriesResponse>() {
                        public void onSuccess(AppendEntriesResponse res) {
                            stdout.log(res);
                        }
                        public void onFailure(Throwable e) {
                            e.printStackTrace();
                        }
                    },
                    service
                );
            }
        }.start();
    }
}
