package rolevm.examples.bank;

import java.util.LinkedList;
import java.util.List;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class Bank extends Compartment {
    private List<Account> checkingAccounts = new LinkedList<>();
    private List<Account> savingAccounts = new LinkedList<>();

    public void addCheckingAccount(Customer customer, Account accout) {
        checkingAccounts.add(accout);
        customer.addAccount(accout);
    }

    public List<Account> getCheckingAccounts() {
        return checkingAccounts;
    }

    public void addSavingAccount(Customer customer, Account accout) {
        savingAccounts.add(accout);
        customer.addAccount(accout);
    }

    public List<Account> getSavingAccounts() {
        return savingAccounts;
    }

    public @Role class Customer {
        private @Base Person base;
        private final List<Account> accounts = new LinkedList<>();

        public void addAccount(Account account) {
            accounts.add(account);
        }
    }

    public @Role class SavingsAccount {
        private @Base Account base;
        private static final float FEE = 0.1f;

        private float transactionFee(float amount) {
            return amount * FEE;
        }

        @OverrideBase
        public void increase(float amount) {
            base.increase(amount - transactionFee(amount));
        }
    }

    public @Role class CheckingsAccount {
        private @Base Account base;
        private static final float LIMIT = 100.0f;

        @OverrideBase
        public void decrease(float amount) {
            if (amount <= LIMIT) {
                base.decrease(amount);
            } else {
                throw new RuntimeException(amount + " is over the limit: " + LIMIT);
            }
        }
    }

    public @Role class VerboseTransaction {
        private @Base Transaction base;

        @OverrideBase
        public void execute(float amount) {
            System.out.printf("Transfering %.2f from %s to %s%n", amount, base.source, base.target);
            base.execute(amount);
        }
    }
}
