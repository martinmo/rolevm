package rolevm.bench.baseline;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import rolevm.bench.baseline.bank.Account;
import rolevm.bench.baseline.bank.AccountCore;
import rolevm.bench.baseline.bank.Transaction;

public class BankTest {
    private Account account;

    @Before
    public void init() {
        account = new AccountCore(100.0f);
    }

    @Test
    public void testAccount() {
        assertEquals(100.0f, account.getBalance(), 0);
        account.decrease(10.0f);
        assertEquals(90.0f, account.getBalance(), 0);
        account.increase(20.0f);
        assertEquals(110.0f, account.getBalance(), 0);
    }

    @Test
    public void testSavingsAccount() {
        Account savingsAccount = account.addRole("SavingsAccount");
        assertEquals(100.0f, savingsAccount.getBalance(), 0);
        savingsAccount.decrease(10.0f);
        assertEquals(90.0f, savingsAccount.getBalance(), 0);
        savingsAccount.increase(20.0f);
        assertEquals(108.0f, savingsAccount.getBalance(), 0);
    }

    @Test
    public void testCheckingsAccount() {
        Account checkingsAccount = account.addRole("CheckingsAccount");
        assertEquals(100.0f, checkingsAccount.getBalance(), 0);
        checkingsAccount.decrease(10.0f);
        assertEquals(90.0f, checkingsAccount.getBalance(), 0);
        checkingsAccount.increase(20.0f);
        assertEquals(110.0f, checkingsAccount.getBalance(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckingsAccountThrowsException() {
        Account checkingsAccount = account.addRole("CheckingsAccount");
        checkingsAccount.decrease(150.0f);
    }

    @Test
    public void testTransaction() {
        Account anotherAccount = new AccountCore(50.0f);
        account.addRole("CheckingsAccount");
        anotherAccount.addRole("SavingsAccount");
        Transaction transaction = new Transaction(new Transaction.Source(account.getRole("CheckingsAccount")),
                new Transaction.Target(anotherAccount.getRole("SavingsAccount")));
        transaction.execute(50.0f);
        assertEquals(50.0f, account.getBalance(), 0);
        assertEquals(95.0f, anotherAccount.getBalance(), 0);
    }
}
