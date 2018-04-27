package rolevm.transform;

import static rolevm.transform.ClassFileUtils.defineClass;
import static rolevm.transform.ClassFileUtils.disassemble;
import static rolevm.transform.ClassFileUtils.loadClassFile;

import java.lang.instrument.IllegalClassFormatException;
import java.util.regex.Pattern;

import org.junit.Before;
import org.junit.Test;

/**
 * Common test infrastructure for bytecode transformation tests.
 * 
 * @see DefaultCallTransformationTest
 * @see ProceedCallTransformationTest
 * @author Martin Morgenstern
 */
public abstract class TransformationTestBase {
    /** Pattern used to check the emitted {@code invokedynamic} bytecodes. */
    protected static final Pattern PATTERN = Pattern.compile("INVOKEDYNAMIC (.*?) \\[(.*?)\\]\\n", Pattern.DOTALL);
    protected final DefaultTransformer tfm = new DefaultTransformer(new StandardBlacklist());

    protected byte[] transformedClass;
    protected String original;
    protected String transformed;

    @Before
    public void setUp() throws IllegalClassFormatException {
        Class<?> clazz = classUnderTest();
        byte[] originalClass = loadClassFile(clazz);
        original = disassemble(originalClass);
        transformedClass = tfm.transform(null, clazz.getName(), clazz, null, originalClass);
        transformed = disassemble(transformedClass);
    }

    @Test
    public void canBeInstantiated() throws Exception {
        Class<?> clazz = defineClass(transformedClass);
        clazz.getConstructor().newInstance();
    }

    protected abstract Class<?> classUnderTest();
}
