package rolevm.runtime.binder;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.Serializable;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

public class SupertypesTest {
    private CacheAwareBinder.Supertypes supertypes;

    @Before
    public void setUp() {
        supertypes = new CacheAwareBinder.Supertypes();
    }

    @Test
    public void object() {
        Set<Class<?>> computed = supertypes.get(Object.class);
        assertThat(computed.size(), is(1));
        assertThat(computed, hasItem(Object.class));
    }

    @Test
    public void simple() {
        Set<Class<?>> computed = supertypes.get(Number.class);
        assertThat(computed.size(), is(3));
        assertThat(computed, hasItems(Object.class, Serializable.class, Number.class));
    }

    @Test
    public void complex() {
        Set<Class<?>> computed = supertypes.get(Integer.class);
        assertThat(computed.size(), is(5));
        assertThat(computed, hasItems(Object.class, Number.class, Integer.class));
        assertThat(computed, hasItems(Serializable.class, Comparable.class));
    }

    @Test
    public void edgeCases() {
        Set<Class<?>> computed = supertypes.get(B.class);
        assertThat(computed.size(), is(6));
        assertThat(computed, hasItems(Object.class, A.class, B.class));
        assertThat(computed, hasItems(I1.class, I2.class, I3.class));
    }

    @Test
    public void assumptions() {
        assertTrue(Serializable.class.isAssignableFrom(Integer.class));
        assertTrue(I1.class.isAssignableFrom(B.class));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutable1() {
        supertypes.get(Object.class).clear();
    }

    @Test(expected = UnsupportedOperationException.class)
    public void immutable2() {
        supertypes.get(B.class).clear();
    }

    @Test
    public void caching() {
        assertSame(supertypes.get(Object.class), supertypes.get(Object.class));
        assertSame(supertypes.get(B.class), supertypes.get(B.class));
    }

    interface I1 {
    }

    interface I2 {
    }

    interface I3 extends I1, I2 {
    }

    class A implements I3 {
    }

    class B extends A {
    }
}
