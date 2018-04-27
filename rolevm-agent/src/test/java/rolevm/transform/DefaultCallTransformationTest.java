package rolevm.transform;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static rolevm.transform.ClassFileUtils.defineClass;
import static rolevm.transform.ClassFileUtils.methodDescriptor;

import java.io.PrintStream;
import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import org.junit.Test;

/**
 * Verifies that the default <code>invokevirtual</code> and
 * <code>invokeinterface</code> calls (i.e., not <code>proceed()</code> calls)
 * are correctly rewritten to <code>invokedynamic</code> calls.
 * 
 * @author Martin Morgenstern
 */
public class DefaultCallTransformationTest extends TransformationTestBase {
    @Override
    protected Class<?> classUnderTest() {
        return ClassWithDefaultCalls.class;
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
    public void replacedInstructions() throws Exception {
        Matcher matcher = PATTERN.matcher(transformed);
        assertTrue(matcher.find());
        MatchResult firstMatch = matcher.toMatchResult();
        assertTrue(matcher.find());
        MatchResult secondMatch = matcher.toMatchResult();
        assertFalse(matcher.find());
        String printlnSignature = "println" + methodDescriptor(void.class, PrintStream.class, String.class);
        assertThat(firstMatch.group(1), containsString(printlnSignature));
        assertThat(secondMatch.group(1), containsString(printlnSignature));
        String bootstrapSignature = "rolevm/runtime/Bootstrap.defaultcall"
                + methodDescriptor(CallSite.class, Lookup.class, String.class, MethodType.class);
        assertThat(firstMatch.group(2), containsString(bootstrapSignature));
        assertThat(secondMatch.group(2), containsString(bootstrapSignature));
    }

    /** The class to be transformed in this test. */
    public static class ClassWithDefaultCalls {
        public void B() {
            System.out.println("C");
        }

        public static void D() {
            System.out.println("E");
        }
    }
}
