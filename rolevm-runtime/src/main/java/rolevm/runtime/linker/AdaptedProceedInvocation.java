package rolevm.runtime.linker;

import static java.lang.invoke.MethodHandles.dropArguments;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

import jdk.dynalink.DynamicLinker;

public class AdaptedProceedInvocation extends ProceedInvocation {
    private final Class<?> parameterType;

    public AdaptedProceedInvocation(DynamicLinker linker, Lookup lookup, String name, MethodType type) {
        super(linker, lookup, name, type.dropParameterTypes(0, 1));
        parameterType = type.parameterType(0);
    }

    @Override
    public MethodHandle getHandle() {
        return dropArguments(super.getHandle(), 0, parameterType);
    }
}
