package rolevm.examples.university;

import java.util.List;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
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
        public String getName(DispatchContext ctx, Person base) throws Throwable {
            return title + " " + (String) ctx.proceed().invoke(ctx, base);
        }

        @OverrideBase
        public void live(DispatchContext ctx, Person base) throws Throwable {
            advise();
            ctx.proceed().invoke(ctx, base);
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
        public void greet(DispatchContext ctx, Person base, Person other) throws Throwable {
            ctx.proceed().invoke(ctx, base, other);
            if (motivation <= 0) {
                System.out.println("I have no motivation :(");
            } else {
                System.out.println("I am motivated!!!");
            }
        }

        @OverrideBase
        public void live(DispatchContext ctx, Person base) throws Throwable {
            System.out.println("Procrastinating...");
            motivation -= 5;
            ctx.proceed().invoke(ctx, base);
        }
    }
}
