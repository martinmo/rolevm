package rolevm.bench.baseline.bank_bigdec;

import java.math.BigDecimal;

public class CheckingsAccount extends AccountRole {
    private static final BigDecimal LIMIT = BigDecimal.valueOf(100);

    protected CheckingsAccount(AccountCore core) {
        super(core);
    }

    @Override
    public void decrease(final BigDecimal amount) {
        if (amount.compareTo(LIMIT) <= 0) {
            super.decrease(amount);
        } else {
            throw new IllegalArgumentException(amount + " is over the limit: " + LIMIT);
        }
    }
}
