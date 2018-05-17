package rolevm.bench.baseline.bank;

public interface Person {
    PersonRole addRole(String spec);

    PersonRole getRole(String spec);

    boolean hasRole(String spec);
}
