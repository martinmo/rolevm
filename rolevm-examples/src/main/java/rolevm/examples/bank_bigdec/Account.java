package rolevm.examples.bank_bigdec;

import java.math.BigDecimal;

public class Account {
    private BigDecimal balance;

    public Account(long amount) {
        this(BigDecimal.valueOf(amount));
    }

    public Account(BigDecimal amount) {
        balance = amount;
    }

    public void decrease(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public void increase(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
