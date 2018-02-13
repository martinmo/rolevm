package rolevm.transform;

import static org.junit.Assert.assertEquals;
import static rolevm.transform.IndyMethodAdapter.adaptDescriptor;

import org.junit.Test;

public class MethodAdapterTest {
    @Test
    public void testAdaptDescriptor() {
        assertEquals("(Lpkg/Type;Ljava/lang/Object;)V", adaptDescriptor("pkg/Type", "()V"));
        assertEquals("(Lpkg/Type;Lpkg2/Type2;Ljava/lang/Object;)I", adaptDescriptor("pkg/Type", "(Lpkg2/Type2;)I"));
    }

    @Test
    public void testAdaptDescriptorWhenOwnerIsArrayType() {
        assertEquals("([Lpkg/Type$Inner;Ljava/lang/Object;)V", adaptDescriptor("[Lpkg/Type$Inner;", "()V"));
    }
}
