package rolevm.transform;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static rolevm.transform.ClassFileUtils.disassemble;
import static rolevm.transform.ClassFileUtils.loadClassFile;
import static rolevm.transform.ClassFileUtils.methodDescriptor;
import static rolevm.transform.ClassFileUtils.typeDescriptor;

import java.io.PrintStream;
import java.lang.instrument.IllegalClassFormatException;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests the bytecode transformations that replace
 * <code>invoke{virtual,interface}</code> with <code>invokedynamic</code> and
 * that insert a sender argument on the stack.
 * 
 * @author Martin Morgenstern
 */
public class TransformationTest {
    private final DefaultTransformer tfm = new DefaultTransformer(new StandardBlacklist());
    private byte[] transformedClass;
    private String original;
    private String transformed;

    @Before
    public void setUp() throws IllegalClassFormatException {
        Class<?> clazz = A.class;
        byte[] originalClass = loadClassFile(clazz);
        original = disassemble(originalClass);
        transformedClass = tfm.transform(null, clazz.getName(), clazz, null, originalClass);
        transformed = disassemble(transformedClass);
    }

    /** For better readability. */
    static String join(String... instructions) {
        return Stream.of(instructions).collect(joining("\n"));
    }

    @Test
    public void defineClass() throws Exception {
        // TODO
    }

    @Test
    public void originalInstructionsArePresent() throws Exception {
        assertThat(countMatches(original, "INVOKEVIRTUAL"), is(2));
        assertThat(original, not(containsString("INVOKEDYNAMIC")));
    }

    @Test
    public void originalInstructionsAreReplaced() throws Exception {
        assertThat(countMatches(transformed, "INVOKEDYNAMIC"), is(2));
        assertThat(transformed, not(containsString("INVOKEVIRTUAL")));
    }

    @Test
    public void senderLoadInstructionIsPresent() throws Exception {
        String invoke = join("LDC \"C\"", "ALOAD 0", "INVOKEDYNAMIC println");
        assertThat(transformed, containsString(invoke));
    }

    @Test
    public void staticSenderLoadInstructionIsPresent() throws Exception {
        String invoke = join("LDC \"E\"", String.format("LDC %s.class", typeDescriptor(A.class)),
                "INVOKEDYNAMIC println");
        assertThat(transformed, containsString(invoke));
    }

    @Test
    public void methodDescriptorIsAdapted() throws Exception {
        String descriptor = methodDescriptor(void.class, PrintStream.class, String.class, Object.class);
        assertThat(countMatches(transformed, "INVOKEDYNAMIC println" + descriptor), is(2));
    }

    // Test class to be transformed
    public static class A {
        public void B() {
            System.out.println("C");
        }

        public static void D() {
            System.out.println("E");
        }
    }
}
