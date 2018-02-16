package rolevm.examples.simple;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;

public class VarArgsTest {
    private static final String newLine = System.lineSeparator();

    private OutputStream output;
    private PrintStream print;

    @Before
    public void init() {
        output = new ByteArrayOutputStream();
        print = new PrintStream(output);
        VarArgs va = new VarArgs();
        va.bind(print, va.new PrintfWithNewline());
    }

    @Test
    public void appendsNewlineNoArgs() {
        print.printf("test");
        assertEquals("test" + newLine, output.toString());
    }

    @Test
    public void appendsNewline() {
        print.printf("test %d", 123);
        assertEquals("test 123" + newLine, output.toString());
    }

    @Test
    public void originalBehavior() {
        PrintStream anotherPrint = new PrintStream(output);
        anotherPrint.printf("test %d", 321);
        assertEquals("test 321", output.toString());
    }
}
