package rolevm.bench.baseline.bank;

public class SavingsAccount extends AccountRole {
    private static final float FEE = 0.1f;

    protected SavingsAccount(AccountCore core) {
        super(core);
    }

    private float transactionFee(final float amount) {
        return amount * FEE;
    }

    @Override
    public void increase(final float amount) {
        super.increase(amount - transactionFee(amount));
    }
}
