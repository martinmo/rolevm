package rolevm.bench.baseline.bank;

public interface Account {
    void increase(float amount);

    void decrease(float amount);

    float getBalance();

    AccountRole addRole(String spec);

    AccountRole getRole(String spec);

    boolean hasRole(String spec);
}
