package rolevm.bench.bank;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.bench.DefaultBenchmark;
import rolevm.examples.bank.Account;
import rolevm.examples.bank.Bank;
import rolevm.examples.bank.Bank.Customer;
import rolevm.examples.bank.Person;
import rolevm.examples.bank.Transaction;

@Fork(jvmArgsAppend = { "@rolevm-bench/jvm.options" })
@State(Scope.Benchmark)
public class BankBenchmark extends DefaultBenchmark {
    @Param("1500")
    int N;

    /**
     * We use our own {@code gc} option, because JMH's {@code -gc} flag fails with a
     * {@code NoClassDefFoundError} for {@code rolevm/runtime/Bootstrap}.
     */
    @Param("false")
    boolean gc;

    Bank bank;

    @Setup(Level.Iteration)
    public void setup() {
        bank = new Bank();
        for (int i = 0; i < N; ++i) {
            Person p = new Person();
            Customer c = bank.bind(p, bank.new Customer());
            Account sa = new Account(1000.0f);
            Account ca = new Account(1000.0f);
            bank.bind(sa, bank.new SavingsAccount());
            bank.bind(ca, bank.new CheckingsAccount());
            bank.addSavingsAccount(c, sa);
            bank.addCheckingsAccount(c, ca);
        }
    }

    @TearDown(Level.Iteration)
    public void teardown() {
        bank = null;
        if (gc) {
            System.runFinalization();
            System.gc();
            System.runFinalization();
            System.gc();
        }
    }

    @Benchmark
    public boolean process_transactions_NxN() {
        for (Account from : bank.getCheckingAccounts()) {
            float amount = from.getBalance() / N;
            for (Account to : bank.getSavingAccounts()) {
                Transaction transaction = new Transaction(new Transaction.Source(from), new Transaction.Target(to));
                transaction.execute(amount);
            }
        }
        return true;
    }

}
