package rolevm.bench.baseline.bank_bigdec;

import java.util.ArrayList;
import java.util.List;

public class Bank {
    private final List<CheckingsAccount> checkingAccounts = new ArrayList<>();
    private final List<SavingsAccount> savingAccounts = new ArrayList<>();

    public void addCheckingsAccount(final Customer customer, final Account account) {
        CheckingsAccount checkingsAccount = (CheckingsAccount) account.addRole("CheckingsAccount");
        checkingAccounts.add(checkingsAccount);
        customer.addAccount(checkingsAccount);
    }

    public List<CheckingsAccount> getCheckingAccounts() {
        return checkingAccounts;
    }

    public void addSavingsAccount(final Customer customer, final Account account) {
        SavingsAccount savingsAccount = (SavingsAccount) account.addRole("SavingsAccount");
        savingAccounts.add(savingsAccount);
        customer.addAccount(savingsAccount);
    }

    public List<SavingsAccount> getSavingAccounts() {
        return savingAccounts;
    }
}
