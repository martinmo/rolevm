package rolevm.examples.bank;

public class Account {
    private float balance;

    public Account(float amount) {
        balance = amount;
    }

    public void decrease(float amount) {
        balance -= amount;
    }

    public void increase(float amount) {
        balance += amount;
    }

    public float getBalance() {
        return balance;
    }
}
