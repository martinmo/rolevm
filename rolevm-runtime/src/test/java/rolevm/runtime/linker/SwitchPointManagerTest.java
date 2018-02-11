package rolevm.runtime.linker;

import static org.junit.Assert.assertEquals;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

import rolevm.runtime.linker.SwitchPointManager;

public class SwitchPointManagerTest {
    private SwitchPointManager registry = new SwitchPointManager();

    @Test
    public void computeTypeForClassSimple() {
        assertEquals(List.of(Object.class), registry.assignmentCompatibleTypes(Object.class));
        assertEquals(List.of(Math.class, Object.class), registry.assignmentCompatibleTypes(Math.class));
    }

    @Test
    public void computeTypeForIface() {
        assertEquals(List.of(Runnable.class, Object.class), registry.assignmentCompatibleTypes(Runnable.class));
    }

    @Test
    public void computeTypeForIfaceComplex() {
        assertEquals(List.of(Serializable.class, Number.class, Object.class), registry.assignmentCompatibleTypes(Number.class));
        assertEquals(List.of(Serializable.class, AtomicInteger.class, Number.class, Object.class),
                registry.assignmentCompatibleTypes(AtomicInteger.class));
    }
}
