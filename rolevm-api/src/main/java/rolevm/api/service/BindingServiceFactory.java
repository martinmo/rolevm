package rolevm.api.service;

/**
 * Well known service type that is used to discover a provider for a concrete
 * {@link BindingService} implementation using {@link java.util.ServiceLoader}.
 * 
 * @author Martin Morgenstern
 */
public interface BindingServiceFactory {
    BindingService getBindingService();
}
