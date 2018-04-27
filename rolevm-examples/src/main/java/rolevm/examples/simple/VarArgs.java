package rolevm.examples.simple;

import java.io.PrintStream;

import rolevm.api.Compartment;
import rolevm.api.DispatchContext;
import rolevm.api.Role;

public class VarArgs extends Compartment {
    public @Role class PrintfWithNewline {
        public PrintStream printf(DispatchContext ctx, PrintStream base, String format, Object... args)
                throws Throwable {
            return (PrintStream) ctx.proceed().invoke(ctx, base, format + "%n", args);
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
