package rolevm.bench.baseline.noop;

import java.util.HashMap;
import java.util.Map;

public class ComponentCore implements Component {
    private final Map<String, ComponentRole> roles = new HashMap<>();

    @Override
    public Object noArgs() {
        return this;
    }

    @Override
    public Object referenceArgAndReturn(Object o) {
        return o;
    }

    @Override
    public int primitiveArgsAndReturn(int x, int y) {
        return x + y;
    }

    @Override
    public ComponentRole getRole(final String spec) {
        final ComponentRole role = roles.get(spec);
        if (role == null) {
            throw new IllegalStateException(this + " plays no such role: " + spec);
        }
        return roles.get(spec);
    }

    @Override
    public ComponentRole addRole(final String spec) {
        if (roles.containsKey(spec)) {
            throw new IllegalStateException(this + " cannot play " + spec + " more than once");
        }
        final ComponentRole role = ComponentRole.createFor(spec, this);
        roles.put(spec, role);
        return role;
    }

    @Override
    public boolean hasRole(final String spec) {
        return roles.containsKey(spec);
    }
}
