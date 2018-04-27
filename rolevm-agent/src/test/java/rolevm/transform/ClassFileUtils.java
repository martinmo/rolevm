package rolevm.transform;

import static java.lang.invoke.MethodType.methodType;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.TraceClassVisitor;

public class ClassFileUtils {
    /** Loads class file into a byte array. */
    public static byte[] loadClassFile(final Class<?> clazz) {
        final String resource = clazz.getName().replace('.', '/') + ".class";
        final InputStream s = ClassLoader.getSystemResourceAsStream(resource);
        try (BufferedInputStream b = new BufferedInputStream(s)) {
            return b.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * "Disassembles" a classfile using {@link TraceClassVisitor}, skipping
     * unnecessary information and excessive whitespace around the printed
     * instructions for easier matching.
     */
    public static String disassemble(final byte[] classfile) {
        final StringWriter sw = new StringWriter();
        final TraceClassVisitor visitor = new TraceClassVisitor(new PrintWriter(sw));
        final ClassReader reader = new ClassReader(classfile);
        reader.accept(visitor, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);
        return stream(sw.toString().split("\\n")).map(String::trim).collect(joining("\n"));
    }

    /** Returns a method descriptor, such as {@code (Ljava/lang/String;)I}. */
    public static String methodDescriptor(Class<?> rtype, Class<?>... ptypes) {
        return methodType(rtype, ptypes).toMethodDescriptorString();
    }

    /** Returns a type descriptor, such as {@code Ljava/lang/Object;}. */
    public static String typeDescriptor(Class<?> clazz) {
        return Type.getType(clazz).getDescriptor();
    }
}
