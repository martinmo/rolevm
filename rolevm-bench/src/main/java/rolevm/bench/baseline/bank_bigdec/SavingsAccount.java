package rolevm.bench.baseline.bank_bigdec;

import java.math.BigDecimal;

public class SavingsAccount extends AccountRole {
    private static final BigDecimal FEE = new BigDecimal("0.1");

    protected SavingsAccount(AccountCore core) {
        super(core);
    }

    private BigDecimal transactionFee(final BigDecimal amount) {
        return amount.multiply(FEE);
    }

    @Override
    public void increase(final BigDecimal amount) {
        super.increase(amount.subtract(transactionFee(amount)));
    }
}
