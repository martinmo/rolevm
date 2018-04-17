# RoleVM

[![Build Status](https://travis-ci.org/martinmo/rolevm.svg?branch=master)](https://travis-ci.org/martinmo/rolevm)

RoleVM is a research prototype of a minimal, efficient role-based programming runtime for Java,
based on `java.lang.invoke` and `jdk.dynalink`. It is entirely written in Java and consists of a
small Java agent and an accompanying runtime library. This version can handle more than one role
per player as well as more than one player per role. Furthermore, it solves the inherent memory
leaks and concurrency issues of the first version, which is still available in the `rolevm-1.x`
branch.


## How does it work?

- The RoleVM agent replaces every `invoke{interface,virtual}` instruction in all loaded Java
  classes (except in some blacklisted packages) with equivalent `invokedynamic` instructions. We
  refer to such modified call sites as dynamic call sites.
- At each dynamic call site, a sender argument is pushed to the stack as an additional argument,
  which will later be used during dynamic method dispatch, e.g., to detect base calls or support
  multiple roles per player in a future version of RoleVM.
- Dynamic method dispatch takes sender, receiver and bound roles into account and "jumps" to the
  matching method implementation. The additional sender argument is dropped after the dispatch
  decision has been made.
- A *polymorphic inline cache* is used to cache dispatch decisions at the call site.

For more details, please read the Javadocs. An aggregated Javadoc for all Maven subprojects can be
generated using `mvn javadoc:aggregate`.


## Build

The library is not yet available on Maven Central. You have to build and install it locally using
`mvn install` (requires JDK 9).

Furthermore, it is decoupled into multiple subprojects:

    rolevm
    ├── rolevm-agent
    ├── rolevm-api
    ├── rolevm-bench
    ├── rolevm-examples
    └── rolevm-runtime

The `rolevm-{bench,examples}` submodules are non-essential modules and contain benchmarks and usage
examples.


## Usage

The best way to see RoleVM in action is to take a look at the demos in the `rolevm-examples`
subproject.

### Maven coordinates

First, to use the library, you have to declare a `provided` dependency on the `rolevm-api` Maven
artifact:

```xml
<dependency>
  <groupId>com.github.martinmo.rolevm</groupId>
  <artifactId>rolevm-api</artifactId>
  <version>2.0-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
```

### Role and Base annotations

The RoleVM API can bind roles to arbitrary objects, and base and role types are *not* required to
implement some particular interface or extend a particular class. Instead, role types are annotated
using the `@Role` annotation:

```java
import rolevm.api.Role;
public @Role class RoleType {
}
```

#### Around, Before, After and Replace

Suppose you have a class `A` that you want to use as a base:

```java
public class A {
  public int m(int x) {
    return x * x;
  }
}
```

Then the accompanying, minimum role definition looks like this:

```java
public @Role class R {
}
```

To override a method *m* with the signature *rtype m(ptype0, ptype1, …)* in the base class *A*,
you have to define a method *m* in the role class *R* with a signature of *rtype m(A, ptype0,
ptype1, …)*. During execution of the program, the RoleVM runtime will bind the additional leading
argument to the base object.
(Please note that methods defined in `java.lang.Object`, such as `hashCode()` or `equals()`, cannot
be overridden by a role.)

Here are some examples of how to adjust the behavior of method `int A.m(int)`:

```java
/* Add behavior around, before or after the base method */
public @Role class R1 {
  public int m(A base, int x) {
    System.out.println("Before");
    int r = base.m(x);
    System.out.println("After");
    return r;
  }
}
/* Replace the base method */
public @Role class R2 {
  public int m(A base, int x) {
    // no call to base.m()
    return x * x * x;
  }
}
/* Conditionally replace the base method */
public @Role class R3 {
  public int m(A base, int x) {
    if (x < 42) {
      // no call to base.m()
      return x * x * x;
    }
    return base.m();
  }
}
```

### Compartments

In the RoleVM API, roles depend on compartments. A `@Role` class must be an inner class of a type
that extends `rolevm.api.Compartment`:

```java
import rolevm.api.*;
public MyCompartment extends Compartment {
  public @Role class RoleType {
  }
}
```

### Instantiating and binding roles

Role instantiation and (un)binding are explicit and depend on the compartment.

```java
MyCompartment c = new MyCompartment();
BaseType base = new BaseType();
RoleType role = c.new RoleType();
c.bind(base, role);
c.unbind(base, role);
```

### Running the RoleVM

Invoke `java` with the java agent and the rolevm runtime library on the classpath:

    java -javaagent:rolevm-agent.jar -cp rolevm-runtime.jar pkg.Main

The required JARs can be obtained using the `dependency:copy` goal:

    mvn dependency:copy -DoutputDirectory=path/to/a/directory


## Current limitations

- Only classes with a minimum class format version of 1.7 can be transformed.


## Caveats and workarounds

- You can exclude problematic classes or packages from bytecode transformation using the system
  property `rolevm.exclude`, e.g., `java -Drolevm.exclude=org/example/pkg1/,org/example/pkg2/ ...`.
  (You still can attach roles to instances of untransformed classes, but self calls wont be
  delegated to roles.)
- Be careful not to perform base method calls in anonymous inner classes such as `Runnable` or
  `Callable`, because this will lead to infinite recursion.
- You should disable the "Hot Code Replace" feature of the Eclipse Debugger, if you want to debug
  programs that use the RoleVM agent. There is an awkward interference between that debugger and
  the agent, which will make it jump around to arbitrary breakpoints on its own.


## Performance

### OT/J vs RoleVM vs SCROLL basecall overhead

Measured with JDK 9.0.4 (VM 9.0.4+11) on the same machine using the synthetic NoopCompartment
benchmarks.

OT/J ([benchmark source](https://github.com/martinmo/otjbench)):

    Benchmark                                 Mode  Cnt    Score    Error  Units
    NoopCallinBenchmark.callin_noargs         avgt   10  192,167 ± 11,004  ns/op
    NoopCallinBenchmark.callin_primitiveargs  avgt   10  235,161 ±  8,173  ns/op
    NoopCallinBenchmark.callin_withargs       avgt   10  201,160 ±  6,595  ns/op
    NoopCallinBenchmark.baseline              avgt   10    0,418 ±  0,017  ns/op

RoleVM 1.x ([benchmark source](rolevm-bench/src/main/java/rolevm/bench/noop)):

    Benchmark                                        Mode  Cnt   Score   Error  Units
    NoopCompartmentBenchmark.basecall_noargs         avgt   10  15,118 ± 1,348  ns/op
    NoopCompartmentBenchmark.basecall_primitiveargs  avgt   10  14,519 ± 0,463  ns/op
    NoopCompartmentBenchmark.basecall_withargs       avgt   10  14,934 ± 0,401  ns/op
    NoopCompartmentBenchmark.baseline                avgt   10   0,419 ± 0,015  ns/op

RoleVM 2.x:

    TBD

SCROLL (units are µs instead of ns, [benchmark source][scrollbench]):

    Benchmark                             (cached)  Mode  Cnt   Score    Error  Units
    NoopBenchmark.basecall_noargs             true  avgt   10   3,548 ±  0,155  us/op
    NoopBenchmark.basecall_primitiveargs      true  avgt   10   4,732 ±  0,173  us/op
    NoopBenchmark.basecall_withargs           true  avgt   10   5,590 ±  0,176  us/op
    NoopBenchmark.baseline                     N/A  avgt   10  ≈ 10⁻³           us/op

[scrollbench]: https://github.com/martinmo/SCROLL/tree/noop-benchmarks


## License and copyright

Except otherwise noted in the source code, see [LICENSE](LICENSE).
