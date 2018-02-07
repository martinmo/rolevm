package rolevm.examples.simple;

/**
 * Demo of a memory leak which happens if we forget to unbind a role.
 * 
 * Run this with {@code java -Xmx64m} to quickly see the
 * {@link OutOfMemoryError}.
 * 
 * @author Martin Morgenstern
 */
public class MemleakDemo {
    public static void main(String[] args) {
        SimpleCompartment c = new SimpleCompartment();
        while (true) {
            BaseType base = new BaseType();
            c.bind(base, c.new RoleType(0));
            base.delegation();
            // note: no c.unbind()!
        }
    }
}
