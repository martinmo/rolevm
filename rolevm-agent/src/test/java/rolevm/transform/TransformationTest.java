package rolevm.transform;

import static java.util.stream.Collectors.joining;
import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static rolevm.transform.ClassFileUtils.defineClass;
import static rolevm.transform.ClassFileUtils.disassemble;
import static rolevm.transform.ClassFileUtils.loadClassFile;
import static rolevm.transform.ClassFileUtils.methodDescriptor;
import static rolevm.transform.ClassFileUtils.typeDescriptor;

import java.io.PrintStream;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private final Pattern pattern = Pattern.compile("INVOKEDYNAMIC (.*?) \\[(.*?)\\]\\n", Pattern.DOTALL);
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
    public void canBeDefined() throws Exception {
        defineClass(transformedClass);
    }

    @Test
    public void originalInstructionsArePresent() throws Exception {
        assertThat(countMatches(original, "INVOKEVIRTUAL"), is(2));
        assertThat(original, not(containsString("INVOKEDYNAMIC")));
    }

    @Test
    public void senderLoadInstructionIsPresent() throws Exception {
        String instructions = join("LDC \"C\"", "ALOAD 0", "INVOKEDYNAMIC println");
        assertThat(transformed, containsString(instructions));
    }

    @Test
    public void staticSenderLoadInstructionIsPresent() throws Exception {
        String invoke = join("LDC \"E\"", "LDC " + typeDescriptor(A.class) + ".class", "INVOKEDYNAMIC println");
        assertThat(transformed, containsString(invoke));
    }

    @Test
    public void methodDescriptorIsAdapted() throws Exception {
        Matcher matcher = pattern.matcher(transformed);
        assertTrue(matcher.find());
        MatchResult firstMatch = matcher.toMatchResult();
        assertTrue(matcher.find());
        MatchResult secondMatch = matcher.toMatchResult();
        assertFalse(matcher.find());
        String descriptor = methodDescriptor(void.class, PrintStream.class, String.class, Object.class);
        assertThat(firstMatch.group(1), containsString("println" + descriptor));
        assertThat(secondMatch.group(1), containsString("println" + descriptor));
        String descriptor2 = methodDescriptor(CallSite.class, Lookup.class, String.class, MethodType.class);
        assertThat(firstMatch.group(2), containsString("rolevm/runtime/Runtime.bootstrap" + descriptor2));
        assertThat(secondMatch.group(2), containsString("rolevm/runtime/Runtime.bootstrap" + descriptor2));
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
