package rolevm.examples.bank;

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

    public @Role class SavingsAccount {
        private static final float FEE = 0.1f;

        private float transactionFee(float amount) {
            return amount * FEE;
        }

        @OverrideBase
        public void increase(DispatchContext ctx, Account base, float amount) throws Throwable {
            ctx.proceed().invoke(ctx, base, amount - transactionFee(amount));
        }
    }

    public @Role class CheckingsAccount {
        private static final float LIMIT = 100.0f;

        @OverrideBase
        public void decrease(DispatchContext ctx, Account base, float amount) throws Throwable {
            if (amount <= LIMIT) {
                ctx.proceed().invoke(ctx, base, amount);
            } else {
                throw new IllegalArgumentException(amount + " is over the limit: " + LIMIT);
            }
        }
    }

    public @Role class VerboseTransaction {
        @OverrideBase
        public void execute(DispatchContext ctx, Transaction base, float amount) throws Throwable {
            System.out.printf("Transfering %.2f from %s to %s%n", amount, base.source, base.target);
            ctx.proceed().invoke(ctx, base, amount);
        }
    }
}
