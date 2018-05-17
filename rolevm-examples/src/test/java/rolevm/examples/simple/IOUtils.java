package rolevm.examples.simple;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

public class IOUtils {
    public static String interceptSystemOut(final Runnable runnable) {
        final PrintStream old = System.out;
        final ByteArrayOutputStream capture = new ByteArrayOutputStream();
        System.setOut(new PrintStream(capture));
        try {
            runnable.run();
        } finally {
            System.setOut(old);
        }
        return capture.toString();
    }

    public static String record(final Consumer<PrintStream> consumer) {
        final ByteArrayOutputStream record = new ByteArrayOutputStream();
        final PrintStream printer = new PrintStream(record);
        consumer.accept(printer);
        return record.toString();
    }
}
