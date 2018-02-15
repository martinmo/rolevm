package rolevm.api;

import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

import rolevm.api.service.BindingService;
import rolevm.api.service.BindingServiceFactory;

/**
 * Provides a scope for role types and basic binding operations for role-based
 * programming.
 * 
 * @see Role
 * @author Martin Morgenstern
 */
public class Compartment {
    private static final BindingService bindingService = initBindingService();
    private final Set<Binding> bindings = new HashSet<>();

    /**
     * Discover the BindingService, which will be provided by {@link rolevm.runtime}
     * using a {@link ServiceLoader}.
     */
    private static BindingService initBindingService() {
        ServiceLoader<BindingServiceFactory> loader = ServiceLoader.load(BindingServiceFactory.class);
        BindingServiceFactory factory = loader.findFirst()
                .orElseThrow(() -> new RuntimeException("Could not find a BindingService"));
        return factory.getBindingService();
    }

    // TODO: implement the usual compartment stuff (activate, deactivate, ...)

    public final <T> T bind(final Object player, final T role) {
        bindings.add(new Binding(player, role));
        bindingService.bind(player, role);
        return role;
    }

    public final void unbind(final Object player, final Object role) {
        bindings.remove(new Binding(player, role));
        bindingService.unbind(player, role);
    }

    public final void unbindAll() {
        bindings.stream().forEach(b -> bindingService.unbind(b.player, b.role));
        bindings.clear();
    }
}
