package rolevm.examples.university;

import java.util.List;

import rolevm.examples.university.University.Advisor;
import rolevm.examples.university.University.Student;

public class UniversityDemo {
    public static void main(String args[]) throws Exception {
        University u = new University();
        Person max = new Person("Max");
        Person martin = new Person("Martin");

        System.out.println(">>> Initial, not bound:");
        demo(max, martin);

        Student student = u.bind(martin, u.new Student());
        Advisor advisor = u.bind(max, u.new Advisor("Dr.-Ing.", List.of(student)));
        System.out.println(">>> Bound:");
        demo(max, martin);

        System.out.println(">>> Unbound:");
        u.unbind(martin, student);
        u.unbind(max, advisor);
        demo(max, martin);
    }

    static void demo(Person max, Person martin) {
        System.out.println(max.getClass());
        System.out.println(martin.getClass());
        max.greet(martin);
        martin.greet(max);
        martin.live();
        max.live();
    }
}
