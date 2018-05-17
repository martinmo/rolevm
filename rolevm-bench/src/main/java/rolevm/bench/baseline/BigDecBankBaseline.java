package rolevm.bench.baseline;

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

import rolevm.bench.baseline.bank_bigdec.Account;
import rolevm.bench.baseline.bank_bigdec.AccountCore;
import rolevm.bench.baseline.bank_bigdec.Bank;
import rolevm.bench.baseline.bank_bigdec.Customer;
import rolevm.bench.baseline.bank_bigdec.Person;
import rolevm.bench.baseline.bank_bigdec.PersonCore;
import rolevm.bench.baseline.bank_bigdec.Transaction;

@Fork(warmups = 1)
@BenchmarkMode(Mode.SingleShotTime)
@State(Scope.Benchmark)
public class BigDecBankBaseline {
    @Param("1500")
    int N;

    BigDecimal nAsBigDecimal;

    Bank bank;

    @Setup(Level.Iteration)
    public void setup() {
        bank = new Bank();
        nAsBigDecimal = BigDecimal.valueOf(N);
        for (int i = 0; i < N; ++i) {
            Person p = new PersonCore();
            Customer c = (Customer) p.addRole("Customer");
            bank.addSavingsAccount(c, new AccountCore(1000));
            bank.addCheckingsAccount(c, new AccountCore(1000));
        }
    }

    @Benchmark
    public boolean process_transactions_NxN() {
        for (Account from : bank.getCheckingAccounts()) {
            BigDecimal amount = from.getBalance().divide(nAsBigDecimal);
            for (Account to : bank.getSavingAccounts()) {
                Transaction transaction = new Transaction(new Transaction.Source(from.getRole("CheckingsAccount")),
                        new Transaction.Target(to.getRole("SavingsAccount")));
                transaction.execute(amount);
            }
        }
        return true;
    }

}
