package rolevm.transform;

import static org.apache.commons.lang3.StringUtils.countMatches;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static rolevm.transform.ClassFileUtils.methodDescriptor;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;

import org.junit.Test;

import rolevm.api.DispatchContext;

/**
 * Verifies that the <code>invokevirtual</code> and <code>invokeinterface</code>
 * calls of <code>proceed()</code> calls are correctly rewritten to
 * <code>invokedynamic</code> calls.
 * 
 * @author Martin Morgenstern
 */
public class ProceedCallTransformationTest extends TransformationTestBase {
    @Override
    protected Class<?> classUnderTest() {
        return ClassWithProceedCall.class;
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
        assertFalse(matcher.find());
        String signature = "someMethod"
                + methodDescriptor(int.class, MethodHandle.class, DispatchContext.class, String.class);
        assertThat(firstMatch.group(1), containsString(signature));
        String bootstrapSignature = "rolevm/runtime/Bootstrap.proceedcall"
                + methodDescriptor(CallSite.class, Lookup.class, String.class, MethodType.class);
        assertThat(firstMatch.group(2), containsString(bootstrapSignature));
    }

    // Replace INVOKEVIRTUAL DispatchContext.proceed() with POP+ACONST_NULL

    /** The class to be transformed in this test. */
    public static class ClassWithProceedCall {
        public int someMethod(DispatchContext ctx, String core) throws Throwable {
            return (int) ctx.proceed().invoke(ctx, core, "test");
        }
    }
}
