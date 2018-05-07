package rolevm.runtime.binder;

import rolevm.api.service.BindingServiceFactory;
import rolevm.runtime.Binder;

/**
 * Implementation of {@link BindingServiceFactory} that returns the singleton
 * instance of the {@link Binder}.
 * 
 * @author Martin Morgenstern
 */
public class BinderFactory implements BindingServiceFactory {
    private static final CacheAwareBinder binder = new CacheAwareBinder();

    @Override
    public CacheAwareBinder getBindingService() {
        return binder;
    }
}
