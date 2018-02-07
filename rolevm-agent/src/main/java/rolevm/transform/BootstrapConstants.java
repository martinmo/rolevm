package rolevm.transform;

import static java.lang.invoke.MethodType.methodType;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;

public interface BootstrapConstants {
    static final String BSM_CLASS = "rolevm/runtime/Runtime";
    static final String BSM_NAME = "bootstrap";
    static final String BSM_TYPE = methodType(CallSite.class, Lookup.class, String.class, MethodType.class)
            .toMethodDescriptorString();
}
