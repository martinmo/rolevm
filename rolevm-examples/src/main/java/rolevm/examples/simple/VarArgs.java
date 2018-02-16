package rolevm.examples.simple;

import java.io.PrintStream;

import rolevm.api.Base;
import rolevm.api.Compartment;
import rolevm.api.Role;

public class VarArgs extends Compartment {
    public @Role class PrintfWithNewline {
        private @Base PrintStream base;

        public PrintStream printf(String format, Object... args) {
            return base.printf(format + "%n", args);
        }
    }

    public static void main(String[] args) {
        printfLoop();
        System.out.println();
        VarArgs va = new VarArgs();
        va.bind(System.out, va.new PrintfWithNewline());
        printfLoop();
    }

    private static void printfLoop() {
        for (int i = 0; i < 10; i++) {
            System.out.printf("Line %d", i);
        }
    }
}
