package rolevm.examples.university;

import java.util.List;

import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class University extends Compartment {
    public @Role class Advisor {
        private final List<Student> students;
        private final String title;

        public Advisor(String title, List<Student> students) {
            this.students = students;
            this.title = title;
        }

        @OverrideBase
        public String getName(Person base) {
            return title + " " + base.getName();
        }

        @OverrideBase
        public void live(Person base) {
            advise();
            base.live();
        }

        public void advise() {
            System.out.println("Advising...");
            for (Student s : students) {
                s.motivation += 10;
            }
        }
    }

    public @Role class Student {
        private int motivation = 0;

        @OverrideBase
        public void greet(Person base, Person other) {
            base.greet(other);
            if (motivation <= 0) {
                System.out.println("I have no motivation :(");
            } else {
                System.out.println("I am motivated!!!");
            }
        }

        @OverrideBase
        public void live(Person base) {
            System.out.println("Procrastinating...");
            motivation -= 5;
            base.live();
        }
    }
}
