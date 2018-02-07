package rolevm.examples.bank;

import rolevm.examples.bank.Bank.Customer;

public class BankDemo {
    public static void main(String[] args) {
        Bank bank = new Bank();
        int iterations = 5;
        for (int i = 0; i < iterations; ++i) {
            Person p = new Person();
            Customer c = bank.bind(p, bank.new Customer());

            Account sa = new Account(500.0f);
            Account ca = new Account(500.0f);

            bank.bind(sa, bank.new SavingsAccount());
            bank.bind(ca, bank.new CheckingsAccount());

            bank.addSavingAccount(c, sa);
            bank.addCheckingAccount(c, ca);
        }

        for (Account from : bank.getCheckingAccounts()) {
            float amount = from.getBalance() / iterations;
            for (Account to : bank.getSavingAccounts()) {
                Bank.Transaction transaction = new Bank.Transaction(from, to);
                bank.bind(transaction, bank.new VerboseTransaction());
                transaction.execute(amount);
            }
        }

        bank.getCheckingAccounts().stream().forEach(a -> System.out.println(a + ": " + a.getBalance()));
        bank.getSavingAccounts().stream().forEach(a -> System.out.println(a + ": " + a.getBalance()));
    }
}
