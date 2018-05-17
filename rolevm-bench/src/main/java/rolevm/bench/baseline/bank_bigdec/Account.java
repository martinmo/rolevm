package rolevm.bench.baseline.bank_bigdec;

import java.math.BigDecimal;

public interface Account {
    void increase(BigDecimal amount);

    void decrease(BigDecimal amount);

    BigDecimal getBalance();

    AccountRole addRole(String spec);

    AccountRole getRole(String spec);

    boolean hasRole(String spec);
}
