package rolevm.api;

import java.util.Objects;

/**
 * Internal (currently unused) object-to-role mapping with reference-based
 * equality.
 * 
 * @author Martin Morgenstern
 */
class Binding {
    final Object player;
    final Object role;

    public Binding(final Object player, final Object role) {
        this.player = Objects.requireNonNull(player);
        this.role = Objects.requireNonNull(role);
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Binding) {
            Binding that = (Binding) o;
            return that.player == player && that.role == role;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return 31 * System.identityHashCode(player) + System.identityHashCode(role);
    }
}
