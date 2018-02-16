# RoleVM

[![Build Status](https://travis-ci.org/martinmo/rolevm.svg?branch=master)](https://travis-ci.org/martinmo/rolevm)

RoleVM is a minimal, efficient role-based programming runtime for Java, based on
`java.lang.invoke` and `jdk.dynalink`. It is entirely written in Java and consists of a small Java
agent and an accompanying runtime library.

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
  <version>1.0-SNAPSHOT</version>
  <scope>provided</scope>
</dependency>
```

### Role and Base annotations

The RoleVM API can bind roles to arbitrary objects, and base and role types are *not* required to
implement some particular interface or extend a particular class. Instead, role types are annotated
using `@Role` and `@Base` annotations:

```java
import rolevm.api.*;
public @Role class RoleType {
  private @Base BaseType base;
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
  private @Base A base;
}
```

To override a method of the base class, it must exactly *match the method signature* in the base
class. (Methods defined in `java.lang.Object`, such as `hashCode()` or `equals()`, cannot be
overridden by a role.)

Here are some examples of how to adjust the behavior of method `int A.m(int)`:

```java
/* Add behavior around, before or after the base method */
public @Role class R1 {
  private @Base A base;
  public int m(int x) {
    System.out.println("Before");
    int r = base.m(x);
    System.out.println("After");
    return r;
  }
}
/* Replace the base method */
public @Role class R2 {
  private @Base A base;
  public int m(int x) {
    // no call to base.m()
    return x * x * x;
  }
}
/* Conditionally replace the base method */
public @Role class R3 {
  private @Base A base;
  public int m(int x) {
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
    private @Base BaseType base;
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

- Only one role may be bound to an object.
- Roles must be explicitly unbound to make the base (and role) object eligible for garbage
  collection. This is a result of the way RoleVM associates players with roles and vice versa, and
  cannot be fixed without JVM support for [Ephemerons](https://en.wikipedia.org/wiki/Ephemeron). 
- Only classes with a minimum class format version of 1.7 can be transformed.

## Caveats and workarounds

- You can exclude problematic classes or packages from bytecode transformation using the system
  property `rolevm.exclude`, e.g., `java -Drolevm.exclude=org/example/pkg1/,org/example/pkg2/ ...`.
  (You still can attach roles to instances of untransformed classes, but self calls wont be
  delegated to roles.)
- Be careful not to perform base method calls in anonymous inner classes such as `Runnable` or
  `Callable`, because this will lead to infinite recursion. You should use lambdas instead. For
  more details, have a look at the role types in the [FastFib example compartment][fastfib].
- You should disable the "Hot Code Replace" feature of the Eclipse Debugger, if you want to debug
  programs that use the RoleVM agent. There is an awkward interference between that debugger and
  the agent, which will make it jump around to arbitrary breakpoints on its own.

[fastfib]: rolevm-examples/src/main/java/rolevm/examples/fib/FastFib.java

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

RoleVM ([benchmark source](rolevm-bench/src/main/java/rolevm/bench/noop)):

    Benchmark                                        Mode  Cnt   Score   Error  Units
    NoopCompartmentBenchmark.basecall_noargs         avgt   10  15,118 ± 1,348  ns/op
    NoopCompartmentBenchmark.basecall_primitiveargs  avgt   10  14,519 ± 0,463  ns/op
    NoopCompartmentBenchmark.basecall_withargs       avgt   10  14,934 ± 0,401  ns/op
    NoopCompartmentBenchmark.baseline                avgt   10   0,419 ± 0,015  ns/op

SCROLL (units are µs instead of ns, [benchmark source][scrollbench]):

    Benchmark                             (cached)  Mode  Cnt   Score    Error  Units
    NoopBenchmark.basecall_noargs             true  avgt   10   3,548 ±  0,155  us/op
    NoopBenchmark.basecall_primitiveargs      true  avgt   10   4,732 ±  0,173  us/op
    NoopBenchmark.basecall_withargs           true  avgt   10   5,590 ±  0,176  us/op
    NoopBenchmark.baseline                     N/A  avgt   10  ≈ 10⁻³           us/op

[scrollbench]: https://github.com/martinmo/SCROLL/tree/noop-benchmarks

### Recursive Fibonacci

Calculating `fib(n) = (n > 1) ? fib(n - 1) + fib(n - 2) : n` ([benchmark source][fibbench]).

[fibbench]: rolevm-bench/src/main/java/rolevm/bench/fib

Plain Java (untransformed classes):

    Benchmark                                          (n)  Mode  Cnt    Score   Error  Units
    RecursiveFibPlainJava.without_role                  20  avgt   10   46,068 ± 0,889  us/op

With transformed classes and a role that caches results of previous `fib()` calls:

    Benchmark                                          (n)  Mode  Cnt    Score   Error  Units
    RecursiveFibWithAgent.with_role                     20  avgt   10    0,309 ± 0,011  us/op

With transformed classes, no bound role:

    Benchmark                                          (n)  Mode  Cnt    Score   Error  Units
    RecursiveFibWithAgent.without_role                  20  avgt   10   47,069 ± 0,969  us/op

With transformed classes, no bound role (after unbind of role):

    Benchmark                                          (n)  Mode  Cnt    Score   Error  Units
    RecursiveFibWithAgent.without_role_invalidated_sp   20  avgt   10  118,872 ± 2,383  us/op

## License and copyright

See [LICENSE](LICENSE).
