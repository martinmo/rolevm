package rolevm.runtime.dynalink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.linker.GuardingDynamicLinker;
import rolevm.runtime.Binder;
import rolevm.runtime.GuardedQuery;

/**
 * Builder for a composite {@link DynamicLinker} consisting of the component
 * linkers in this package.
 * 
 * @author Martin Morgenstern
 */
public class DynalinkLinkerBuilder {
    private static final String THRESHOLD_PROPERTY = "rolevm.unstableRelinkThreshold";
    private static final int THRESHOLD_DEFAULT = 8;
    private int unstableRelinkThreshold = THRESHOLD_DEFAULT;
    private Binder binder;
    private GuardedQuery guardedQuery;

    /**
     * Builds a {@link DynamicLinker} with the current configuration.
     * 
     * @return a new {@link DynamicLinker} instance
     */
    public DynamicLinker build() {
        if (binder == null || guardedQuery == null) {
            throw new IllegalArgumentException("binder and guardedQuery must not be null");
        }
        DynamicLinkerFactory factory = new DynamicLinkerFactory();
        List<GuardingDynamicLinker> componentLinkers = new ArrayList<>();
        componentLinkers.add(new JLOLinker());
        componentLinkers.add(new FastpathLinker(guardedQuery));
        componentLinkers.add(new StableLinker(guardedQuery));
        componentLinkers.add(new UnstableLinker(binder.createGetContextHandle()));
        factory.setPrioritizedLinkers(componentLinkers);
        factory.setUnstableRelinkThreshold(unstableRelinkThreshold);
        factory.setFallbackLinkers();
        return factory.createLinker();
    }

    /** Configure the {@link Binder} implementation to be used. */
    public DynalinkLinkerBuilder withBinder(final Binder binder) {
        this.binder = Objects.requireNonNull(binder);
        return this;
    }

    /** Configure the {@link GuardedQuery} implementation to be used. */
    public DynalinkLinkerBuilder withGuardedQuery(final GuardedQuery guardedQuery) {
        this.guardedQuery = Objects.requireNonNull(guardedQuery);
        return this;
    }

    /**
     * Configure the threshold after which Dynalink regards the call site as
     * unstable.
     * 
     * @see DynamicLinkerFactory#setUnstableRelinkThreshold(int)
     */
    public DynalinkLinkerBuilder withUnstableRelinkThreshold(final int unstableRelinkThreshold) {
        this.unstableRelinkThreshold = unstableRelinkThreshold;
        return this;
    }

    /**
     * Configure this builder from system properties. Currently, only
     * {@link #withUnstableRelinkThreshold(int)} can be configured via the
     * {@code rolevm.unstableRelinkThreshold} property.
     */
    public DynalinkLinkerBuilder fromSystemProperties() {
        final String threshold = System.getProperty(THRESHOLD_PROPERTY);
        if (threshold != null) {
            withUnstableRelinkThreshold(Integer.parseInt(threshold));
        }
        return this;
    }
}
