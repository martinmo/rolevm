package rolevm.examples.bank_bigdec;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class Bank extends Compartment {
    private List<Account> checkingAccounts = new ArrayList<>();
    private List<Account> savingAccounts = new ArrayList<>();

    public void addCheckingsAccount(Customer customer, Account accout) {
        checkingAccounts.add(accout);
        customer.addAccount(null, accout);
    }

    public List<Account> getCheckingAccounts() {
        return checkingAccounts;
    }

    public void addSavingsAccount(Customer customer, Account account) {
        savingAccounts.add(account);
        customer.addAccount(null, account);
    }

    public List<Account> getSavingAccounts() {
        return savingAccounts;
    }

    public @Role class Customer {
        private final List<Account> accounts = new ArrayList<>();

        public void addAccount(Person base, Account account) {
            accounts.add(account);
        }
    }

    private static final BigDecimal FEE = new BigDecimal("0.1");

    public @Role class SavingsAccount {

        private BigDecimal transactionFee(BigDecimal amount) {
            return amount.multiply(FEE);
        }

        @OverrideBase
        public void increase(DispatchContext ctx, Account base, BigDecimal amount) throws Throwable {
            ctx.proceed().invoke(ctx, base, amount.subtract(transactionFee(amount)));
        }
    }

    private static final BigDecimal LIMIT = BigDecimal.valueOf(100);

    public @Role class CheckingsAccount {
        @OverrideBase
        public void decrease(DispatchContext ctx, Account base, BigDecimal amount) throws Throwable {
            if (amount.compareTo(LIMIT) <= 0) {
                ctx.proceed().invoke(ctx, base, amount);
            } else {
                throw new IllegalArgumentException(amount + " is over the limit: " + LIMIT);
            }
        }
    }

    public @Role class VerboseTransaction {
        @OverrideBase
        public void execute(DispatchContext ctx, Transaction base, BigDecimal amount) throws Throwable {
            System.out.printf("Transfering %.2f from %s to %s%n", amount, base.source, base.target);
            ctx.proceed().invoke(ctx, base, amount);
        }
    }
}
