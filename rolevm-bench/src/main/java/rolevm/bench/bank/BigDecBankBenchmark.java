package rolevm.bench.bank;

import java.math.BigDecimal;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;

import rolevm.examples.bank_bigdec.Account;
import rolevm.examples.bank_bigdec.Bank;
import rolevm.examples.bank_bigdec.Bank.Customer;
import rolevm.examples.bank_bigdec.Person;
import rolevm.examples.bank_bigdec.Transaction;

@Fork(warmups = 1, value = 15, jvmArgsAppend = { "@rolevm-bench/jvm.options" })
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class BigDecBankBenchmark {
    @Param({ "250", "500", "1000", "2000", "4000" })
    int N;

    /**
     * We use our own {@code gc} option, because JMH's {@code -gc} flag fails with a
     * {@code NoClassDefFoundError} for {@code rolevm/runtime/Bootstrap}.
     */
    @Param("false")
    boolean gc;

    BigDecimal nAsBigDecimal;
    Bank bank;

    @Setup(Level.Iteration)
    public void setup() {
        bank = new Bank();
        nAsBigDecimal = BigDecimal.valueOf(N);
        for (int i = 0; i < N; ++i) {
            Person p = new Person();
            Customer c = bank.bind(p, bank.new Customer());
            Account sa = new Account(1000);
            Account ca = new Account(1000);
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
            BigDecimal amount = from.getBalance().divide(nAsBigDecimal);
            for (Account to : bank.getSavingAccounts()) {
                Transaction transaction = new Transaction(new Transaction.Source(from), new Transaction.Target(to));
                transaction.execute(amount);
            }
        }
        return true;
    }

}
