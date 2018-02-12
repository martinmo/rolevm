package rolevm.examples.noop;

/**
 * Another level of indirection is needed because we exempt package rolevm.bench
 * from transformation.
 * 
 * @author Martin Morgenstern
 */
public class BenchmarkHelper {
    public static String performTest1(Person p) {
        return p.sayHello();
    }

    public static String performTest2(Person p1, Person p2) {
        return p1.sayHelloTo(p2);
    }
}
