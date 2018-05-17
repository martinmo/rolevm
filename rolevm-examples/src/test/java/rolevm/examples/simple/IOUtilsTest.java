package rolevm.examples.simple;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class IOUtilsTest {
    @Test
    public void testIntercepting() {
        String output = IOUtils.interceptSystemOut(() -> {
            System.out.println("foo");
        });
        assertEquals("foo" + System.lineSeparator(), output);
    }

    @Test
    public void testRecording() {
        String output = IOUtils.record((p) -> {
            p.println("bar");
        });
        assertEquals("bar" + System.lineSeparator(), output);
    }
}
