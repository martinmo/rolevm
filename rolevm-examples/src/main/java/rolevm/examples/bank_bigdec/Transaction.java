package rolevm.examples.bank_bigdec;

import java.math.BigDecimal;

public class Transaction {
    public static class Source {
        private final Account account;

        public Source(final Account account) {
            this.account = account;
        }

        public void withdraw(final BigDecimal amount) {
            account.decrease(amount);
        }
    }

    public static class Target {
        private final Account account;

        public Target(final Account account) {
            this.account = account;
        }

        public void deposit(final BigDecimal amount) {
            account.increase(amount);
        }
    }

    final Source source;
    final Target target;

    public Transaction(final Source source, final Target target) {
        this.source = source;
        this.target = target;
    }

    public void execute(final BigDecimal amount) {
        source.withdraw(amount);
        target.deposit(amount);
    }
}
