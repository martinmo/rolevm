package rolevm.examples.bank;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class BankTest {
    private Bank bank;
    private Account account;

    @Before
    public void init() {
        account = new Account(100.0f);
        bank = new Bank();
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
        bank.bind(account, bank.new SavingsAccount());
        assertEquals(100.0f, account.getBalance(), 0);
        account.decrease(10.0f);
        assertEquals(90.0f, account.getBalance(), 0);
        account.increase(20.0f);
        assertEquals(108.0f, account.getBalance(), 0);
    }

    @Test
    public void testCheckingsAccount() {
        bank.bind(account, bank.new CheckingsAccount());
        assertEquals(100.0f, account.getBalance(), 0);
        account.decrease(10.0f);
        assertEquals(90.0f, account.getBalance(), 0);
        account.increase(20.0f);
        assertEquals(110.0f, account.getBalance(), 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCheckingsAccountThrowsException() {
        Account account = new Account(100.0f);
        Bank bank = new Bank();
        bank.bind(account, bank.new CheckingsAccount());
        account.decrease(150.0f);
    }

    @Test
    public void testTransaction() {
        Account anotherAccount = new Account(50.0f);
        bank.bind(account, bank.new CheckingsAccount());
        bank.bind(anotherAccount, bank.new SavingsAccount());
        Transaction transaction = new Transaction(new Transaction.Source(account),
                new Transaction.Target(anotherAccount));
        transaction.execute(50.0f);
        assertEquals(50.0f, account.getBalance(), 0);
        assertEquals(95.0f, anotherAccount.getBalance(), 0);
    }
}
