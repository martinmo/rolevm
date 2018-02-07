package rolevm.examples.university;

public class Person {
    private String name;

    public Person(String name) {
        this.name = name;
    }

    public void greet(Person other) {
        System.out.printf("Hi %s, I am %s!%n", other.getName(), getName());
    }

    @Override
    public String toString() {
        return getClass().getName() + ": " + name;
    }

    public String getName() {
        return name;
    }

    public void live() {
        System.out.println("Living...");
    }
}
