package rolevm.transform;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;

import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 * Tests the bytecode transformations that replace
 * <code>invoke{virtual,interface}</code> with <code>invokedynamic</code> and
 * that insert a sender argument on the stack.
 * 
 * @author Martin Morgenstern
 */
public class TransformationTest {
    private DefaultTransformer tfm = new DefaultTransformer(new StandardBlacklist());

    /** Loads class file into a byte array. */
    static byte[] loadClassFile(final Class<?> clazz) throws IOException {
        final String resource = clazz.getName().replace('.', '/') + ".class";
        final InputStream s = ClassLoader.getSystemResourceAsStream(resource);
        try (BufferedInputStream b = new BufferedInputStream(s)) {
            return b.readAllBytes();
        }
    }

    /**
     * "Disassembles" a classfile using {@link TraceClassVisitor}, skipping
     * unnecessary information and excessive whitespace around the printed
     * instructions for easier matching.
     */
    static String disassemble(final byte[] classfile) {
        final StringWriter sw = new StringWriter();
        final TraceClassVisitor visitor = new TraceClassVisitor(new PrintWriter(sw));
        final ClassReader reader = new ClassReader(classfile);
        reader.accept(visitor, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);
        return stream(sw.toString().split("\\n")).map(String::trim).collect(joining("\n"));
    }

    /** For better readability. */
    static String join(String... instructions) {
        return Stream.of(instructions).collect(joining("\n"));
    }

    @Test
    public void originalInstructionsArePresent() throws Exception {
        String instructions = disassemble(loadClassFile(A.class));
        assertThat(countMatches(instructions, "INVOKEVIRTUAL"), is(2));
        assertThat(instructions, not(containsString("INVOKEDYNAMIC")));
    }

    @Test
    public void originalInstructionsAreReplaced() throws Exception {
        byte[] transformed = tfm.transform(null, "", A.class, null, loadClassFile(A.class));
        String instructions = disassemble(transformed);
        assertThat(countMatches(instructions, "INVOKEDYNAMIC"), is(2));
        assertThat(instructions, not(containsString("INVOKEVIRTUAL")));
    }

    @Test
    public void senderLoadInstructionIsPresent() throws Exception {
        byte[] transformed = tfm.transform(null, "", A.class, null, loadClassFile(A.class));
        String instructions = disassemble(transformed);
        String invoke = join("LDC \"C\"", "ALOAD 0", "INVOKEDYNAMIC println");
        assertThat(instructions, containsString(invoke));
    }

    @Test
    public void staticSenderLoadInstructionIsPresent() throws Exception {
        byte[] transformed = tfm.transform(null, "", A.class, null, loadClassFile(A.class));
        String instructions = disassemble(transformed);
        String invoke = join("LDC \"E\"", String.format("LDC L%s;.class", A.class.getName().replace('.', '/')),
                "INVOKEDYNAMIC println");
        assertThat(instructions, containsString(invoke));
    }

    @Test
    public void methodDescriptorIsAdapted() throws Exception {
        byte[] transformed = tfm.transform(null, "", A.class, null, loadClassFile(A.class));
        String instructions = disassemble(transformed);
        assertThat(countMatches(instructions,
                "INVOKEDYNAMIC println(Ljava/io/PrintStream;Ljava/lang/String;Ljava/lang/Object;)V"), is(2));
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
