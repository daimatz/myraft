package net.daimatz.raft;

import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import java.io.PrintStream;

public class PrintStreamLogger {
    private final PrintStream out;
    public PrintStreamLogger(PrintStream out) {
        this.out = out;
    }
    public void log(Message message) {
        try {
            out.println(JsonFormat.printer().print(message));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
