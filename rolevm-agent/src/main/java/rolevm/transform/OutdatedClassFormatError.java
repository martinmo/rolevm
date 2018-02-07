package rolevm.transform;

/**
 * Thrown when we encounter a pre 1.7 class without Stack Frame Maps.
 * <p>
 * Must be unchecked because we can't change ASM signatures.
 * 
 * @author Martin Morgenstern
 */
class OutdatedClassFormatError extends RuntimeException {
    private static final long serialVersionUID = 0;

    public OutdatedClassFormatError(int version) {
        super("" + version);
    }
}
