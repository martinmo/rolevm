package rolevm.bench.baseline.bank;

public class CheckingsAccount extends AccountRole {
    private static final float LIMIT = 100.0f;

    protected CheckingsAccount(AccountCore core) {
        super(core);
    }

    @Override
    public void decrease(final float amount) {
        if (amount <= LIMIT) {
            super.decrease(amount);
        } else {
            throw new IllegalArgumentException(amount + " is over the limit: " + LIMIT);
        }
    }
}
