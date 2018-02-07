package rolevm.examples.person;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.OverrideBase;
import rolevm.api.Role;

public class NoopCompartment extends Compartment {
    public @Role class AdvancedPerson {
        private @Base Person base;

        @OverrideBase
        public String sayHello() {
            return base.sayHello();
        }

        @OverrideBase
        public String sayHelloTo(Person otherPerson) {
            return base.sayHelloTo(otherPerson);
        }
    }
}
