package rolevm.bench.baseline.bank;

import java.util.HashMap;
import java.util.Map;

public class AccountCore implements Account {
    private final Map<String, AccountRole> roles = new HashMap<>();
    private float balance;

    public AccountCore(final float balance) {
        this.balance = balance;
    }

    @Override
    public void increase(final float amount) {
        balance += amount;
    }

    @Override
    public void decrease(final float amount) {
        balance -= amount;
    }

    @Override
    public float getBalance() {
        return balance;
    }

    @Override
    public AccountRole addRole(final String spec) {
        if (roles.containsKey(spec)) {
            throw new IllegalStateException(this + " cannot play " + spec + " more than once");
        }
        final AccountRole role = AccountRole.createFor(spec, this);
        roles.put(spec, role);
        return role;
    }

    @Override
    public AccountRole getRole(final String spec) {
        final AccountRole role = roles.get(spec);
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
