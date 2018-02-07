package rolevm.transform;

/**
 * Internally used container type to pass method metadata to the
 * {@link IndyMethodAdapter}.
 */
class MethodInfo {
    final String className;
    final int access;
    final String name;
    final String desc;

    public MethodInfo(String className, int access, String name, String desc) {
        this.className = className;
        this.access = access;
        this.name = name;
        this.desc = desc;
    }
}
