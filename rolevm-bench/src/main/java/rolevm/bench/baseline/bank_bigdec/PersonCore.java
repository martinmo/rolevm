package rolevm.bench.baseline.bank_bigdec;

import java.util.HashMap;
import java.util.Map;

public class PersonCore implements Person {
    private final Map<String, PersonRole> roles = new HashMap<>();

    @Override
    public PersonRole addRole(final String spec) {
        if (roles.containsKey(spec)) {
            throw new IllegalStateException(this + " cannot play " + spec + " more than once");
        }
        PersonRole role = PersonRole.createFor(spec, this);
        roles.put(spec, role);
        return role;
    }

    @Override
    public PersonRole getRole(final String spec) {
        final PersonRole role = roles.get(spec);
        if (role == null) {
            throw new IllegalStateException(this + " plays no such role: " + spec);
        }
        return role;
    }

    @Override
    public boolean hasRole(final String spec) {
        return roles.containsKey(spec);
    }
}
