package rolevm.examples.university;

import java.util.List;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class University extends Compartment {
    public @Role class Advisor {
        @Base
        Person base;

        private final List<Student> students;
        private final String title;

        public Advisor(String title, List<Student> students) {
            this.students = students;
            this.title = title;
        }

        @OverrideBase
        public String getName() {
            return title + " " + base.getName();
        }

        @OverrideBase
        public void live() {
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
        @Base
        Person base;
        private int motivation = 0;

        @OverrideBase
        public void greet(Person other) {
            base.greet(other);
            if (motivation <= 0) {
                System.out.println("I have no motivation :(");
            } else {
                System.out.println("I am motivated!!!");
            }
        }

        @OverrideBase
        public void live() {
            System.out.println("Procrastinating...");
            motivation -= 5;
            base.live();
        }
    }
}
