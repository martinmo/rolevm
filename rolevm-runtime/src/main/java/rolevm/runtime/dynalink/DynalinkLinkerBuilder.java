package rolevm.runtime.dynalink;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jdk.dynalink.DynamicLinker;
import jdk.dynalink.DynamicLinkerFactory;
import jdk.dynalink.linker.GuardingDynamicLinker;
import rolevm.runtime.Binder;
import rolevm.runtime.GuardedQuery;

public class DynalinkLinkerBuilder {
    private static final String THRESHOLD_PROPERTY = "rolevm.unstableRelinkThreshold";
    private static final int THRESHOLD_DEFAULT = 8;
    private int unstableRelinkThreshold = THRESHOLD_DEFAULT;
    private Binder binder;
    private GuardedQuery guardedQuery;

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

    public DynalinkLinkerBuilder withBinder(final Binder binder) {
        this.binder = Objects.requireNonNull(binder);
        return this;
    }

    public DynalinkLinkerBuilder withGuardedQuery(final GuardedQuery guardedQuery) {
        this.guardedQuery = Objects.requireNonNull(guardedQuery);
        return this;
    }

    public DynalinkLinkerBuilder withUnstableRelinkThreshold(final int unstableRelinkThreshold) {
        this.unstableRelinkThreshold = unstableRelinkThreshold;
        return this;
    }

    public DynalinkLinkerBuilder fromSystemProperties() {
        final String threshold = System.getProperty(THRESHOLD_PROPERTY);
        if (threshold != null) {
            withUnstableRelinkThreshold(Integer.parseInt(threshold));
        }
        return this;
    }
}
