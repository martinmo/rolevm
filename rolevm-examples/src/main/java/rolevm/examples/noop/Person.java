package rolevm.examples.noop;

public class Person {
    private final String name;

    public Person(String name) {
        this.name = name;
    }

    public String sayHello() {
        return "Hi, I'm " + name + "!";
    }

    public String sayHelloTo(Person otherPerson) {
        return "Hi," + otherPerson.name + ", I'm " + name + "!";
    }
}
