package rolevm.bench.baseline.bank;

import java.util.ArrayList;
import java.util.List;

public class Customer extends PersonRole {
    private final List<Account> accounts = new ArrayList<>();

    protected Customer(PersonCore core) {
        super(core);
    }

    public void addAccount(final Account account) {
        accounts.add(account);
    }
}
