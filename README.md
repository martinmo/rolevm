# RoleVM

[![Build Status](https://travis-ci.org/martinmo/rolevm.svg?branch=master)](https://travis-ci.org/martinmo/rolevm)

RoleVM is a research prototype of a minimal, efficient role-based programming runtime for Java,
based on `java.lang.invoke` and `jdk.dynalink`. It is entirely written in Java and consists of a
small Java agent and an accompanying runtime library. This version can handle more than one role
per player as well as more than one player per role. Furthermore, it solves the inherent memory
leaks and concurrency issues of the first version, which is still available in the `rolevm-1.x`
branch.


## How does it work?

- Using load-time weaving, RoleVM replaces every `invokeinterface` and `invokevirtual`
  instruction in Java classes with an equivalent `invokedynamic` instruction. We refer to such
  modified call sites as dynamic call sites: ![Load-time weaving](docs/figures/defaultcall.png)
- Each dynamic call site dispatches method calls using *role semantics*. It means that the dispatch
  result depends on the currently bound roles of the receiver and can vary over time.
- Dispatching through multiple bound roles results in a call chain such as *roleA.someMethod()* 
  → *roleB.someMethod()* → *roleC.someMethod()* → *core.someMethod()*.
- Role methods have an extended calling convention (compared to ordinary methods). In addition
  to the receiver argument (`this`), a role method expects a `DispatchContext` object and the core
  object on the stack, followed by the remaining arguments. We have provided some examples below.
- In role method bodies, RoleVM uses delegation semantics for calls to the core object.
  Furthermore, there is a special `proceed()` method that can be used to call the next role of the
  ongoing method call.
- RoleVM uses [polymorphic inline caching][dynalink_pic] to make method calls fast despite the
  gained flexibility.
- RoleVM stores object/role mappings in a global concurrent weak hash map that uses
  reference equality comparison (instead of object equality). This is the current bottleneck
  of the implementation, because this mapping must be queried during each method call.
  However, this affects only a subset of the dynamic call sites, and the majority of them is
  guarded with [switch points][switchpoint].

For more details, please read the Javadocs. An aggregated Javadoc for all Maven subprojects can be
generated using `mvn javadoc:aggregate`.

[switchpoint]: https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/SwitchPoint.html
[dynalink_pic]: https://docs.oracle.com/javase/9/docs/api/jdk/dynalink/support/ChainedCallSite.html


## Build

The library is not yet available on Maven Central. You have to build and install it locally using
`mvn install`. The build currently requires JDK 9, but the resulting JARs can be executed on
JDK 10 as well.

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

### Role annotation

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
you have to define a method *m* in the role class *R* with a signature of *rtype m(DispatchContext,
A, ptype0, ptype1, …)*. During method dispatch, the RoleVM runtime will bind the additional leading
arguments.
(Please note that methods defined in `java.lang.Object`, such as `hashCode()` or `equals()`, cannot
be overridden by a role.)

Here are some examples of how to adjust the behavior of method `int A.m(int)`:

```java
/* Add behavior around, before or after the base method */
public @Role class R1 {
  public int m(DispatchContext ctx, A base, int x) {
    System.out.println("Before");
    int r = (int) ctx.proceed().invoke(ctx, base, x);
    System.out.println("After");
    return r;
  }
}
/* Replace the base method */
public @Role class R2 {
  public int m(DispatchContext ctx, A base, int x) {
    // no call to proceed()
    return x * x * x;
  }
}
/* Conditionally replace the base method */
public @Role class R3 {
  public int m(DispatchContext ctx, A base, int x) {
    if (x < 42) {
      // no call to proceed()
      return x * x * x;
    }
    return (int) ctx.proceed().invoke(ctx, base, x);
  }
}
```

Of course, `ctx.proceed().invoke()` is a very crude looking construct. It is needed to conform to
the syntactic rules of plain Java and in order to avoid boxing all arguments into an `Object[]`. It
takes advantage of the fact that `MethodHandle.invoke()` is a [signature polymorphic
method][sigpoly]. Ideally, we would have a domain specific language that translates like this:

![toy language translation](docs/figures/toy_language_proceed.png)

[sigpoly]: https://docs.oracle.com/javase/9/docs/api/java/lang/invoke/MethodHandle.html#sigpoly


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
- Proceed calls in lambdas or anonymous inner classes result in `NoSuchMethodError`s at runtime.
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

RoleVM 2.x with the default `ConcurrentWeakHashMap` as backing store:

    Benchmark                                        (numRoles)  Mode  Cnt   Score   Error  Units
    NoopCompartmentBenchmark.basecall_noargs                  1  avgt   10  53.478 ± 1.212  ns/op
    NoopCompartmentBenchmark.basecall_primitiveargs           1  avgt   10  52.213 ± 1.383  ns/op
    NoopCompartmentBenchmark.basecall_withargs                1  avgt   10  52.130 ± 1.836  ns/op
    NoopCompartmentBenchmark.baseline                       N/A  avgt   10   0.443 ± 0.007  ns/op

RoleVM 2.x with `IdentityHashMap` as backing store (`-Drolevm.map=IdentityHashMap`):

    Benchmark                                        (numRoles)  Mode  Cnt   Score    Error  Units
    NoopCompartmentBenchmark.basecall_noargs                  1  avgt   10  15.601 ±  3.070  ns/op
    NoopCompartmentBenchmark.basecall_primitiveargs           1  avgt   10  18.370 ± 10.167  ns/op
    NoopCompartmentBenchmark.basecall_withargs                1  avgt   10  16.670 ±  4.615  ns/op
    NoopCompartmentBenchmark.baseline                       N/A  avgt   10   0.426 ±  0.016  ns/op

SCROLL (units are µs instead of ns, [benchmark source][scrollbench]):

    Benchmark                             (cached)  Mode  Cnt   Score    Error  Units
    NoopBenchmark.basecall_noargs             true  avgt   10   3,548 ±  0,155  us/op
    NoopBenchmark.basecall_primitiveargs      true  avgt   10   4,732 ±  0,173  us/op
    NoopBenchmark.basecall_withargs           true  avgt   10   5,590 ±  0,176  us/op
    NoopBenchmark.baseline                     N/A  avgt   10  ≈ 10⁻³           us/op

[scrollbench]: https://github.com/martinmo/SCROLL/tree/noop-benchmarks


## License and copyright

Except otherwise noted in the source code, see [LICENSE](LICENSE).
