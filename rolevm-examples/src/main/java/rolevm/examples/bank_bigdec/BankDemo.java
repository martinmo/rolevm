package rolevm.examples.bank_bigdec;

import java.math.BigDecimal;

import rolevm.examples.bank_bigdec.Bank.Customer;

public class BankDemo {
    public static void main(String[] args) {
        Bank bank = new Bank();
        int iterations = 5;
        for (int i = 0; i < iterations; ++i) {
            Person p = new Person();
            Customer c = bank.bind(p, bank.new Customer());

            Account sa = new Account(500);
            Account ca = new Account(500);

            bank.bind(sa, bank.new SavingsAccount());
            bank.bind(ca, bank.new CheckingsAccount());

            bank.addSavingsAccount(c, sa);
            bank.addCheckingsAccount(c, ca);
        }
        for (Account from : bank.getCheckingAccounts()) {
            BigDecimal amount = from.getBalance().divide(BigDecimal.valueOf(iterations));
            for (Account to : bank.getSavingAccounts()) {
                Transaction transaction = new Transaction(new Transaction.Source(from), new Transaction.Target(to));
                bank.bind(transaction, bank.new VerboseTransaction());
                transaction.execute(amount);
            }
        }

        bank.getCheckingAccounts().stream().forEach(a -> System.out.println(a + ": " + a.getBalance()));
        bank.getSavingAccounts().stream().forEach(a -> System.out.println(a + ": " + a.getBalance()));
    }
}
