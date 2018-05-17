package rolevm.bench.baseline.bank_bigdec;

import java.math.BigDecimal;

public abstract class AccountRole implements Account {
    private final AccountCore core;

    protected AccountRole(final AccountCore core) {
        this.core = core;
    }

    public static AccountRole createFor(final String spec, final AccountCore accountCore) {
        if ("SavingsAccount".equals(spec)) {
            return new SavingsAccount(accountCore);
        }
        if ("CheckingsAccount".equals(spec)) {
            return new CheckingsAccount(accountCore);
        }
        throw new IllegalArgumentException(spec);
    }

    @Override
    public void increase(final BigDecimal amount) {
        core.increase(amount);
    }

    @Override
    public void decrease(final BigDecimal amount) {
        core.decrease(amount);
    }

    @Override
    public BigDecimal getBalance() {
        return core.getBalance();
    }

    @Override
    public AccountRole addRole(final String spec) {
        return core.addRole(spec);
    }

    @Override
    public boolean hasRole(final String spec) {
        return core.hasRole(spec);
    }

    @Override
    public AccountRole getRole(final String spec) {
        return core.getRole(spec);
    }
}
