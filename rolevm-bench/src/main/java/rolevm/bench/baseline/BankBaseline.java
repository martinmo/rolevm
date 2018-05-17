package rolevm.bench.baseline;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

import rolevm.bench.baseline.bank.Account;
import rolevm.bench.baseline.bank.AccountCore;
import rolevm.bench.baseline.bank.Bank;
import rolevm.bench.baseline.bank.Customer;
import rolevm.bench.baseline.bank.Person;
import rolevm.bench.baseline.bank.PersonCore;
import rolevm.bench.baseline.bank.Transaction;

@Fork(warmups = 1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class BankBaseline {
    @Param("1500")
    int N;

    Bank bank;

    @Setup(Level.Iteration)
    public void setup() {
        bank = new Bank();
        for (int i = 0; i < N; ++i) {
            Person p = new PersonCore();
            Customer c = (Customer) p.addRole("Customer");
            bank.addSavingsAccount(c, new AccountCore(1000.0f));
            bank.addCheckingsAccount(c, new AccountCore(1000.0f));
        }
    }

    @Benchmark
    public boolean process_transactions_NxN() {
        for (Account from : bank.getCheckingAccounts()) {
            float amount = from.getBalance() / N;
            for (Account to : bank.getSavingAccounts()) {
                Transaction transaction = new Transaction(new Transaction.Source(from.getRole("CheckingsAccount")),
                        new Transaction.Target(to.getRole("SavingsAccount")));
                transaction.execute(amount);
            }
        }
        return true;
    }

}
