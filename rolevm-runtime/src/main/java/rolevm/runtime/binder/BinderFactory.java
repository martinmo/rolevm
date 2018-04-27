package rolevm.runtime.binder;

import rolevm.api.service.BindingServiceFactory;

/**
 * Implementation of {@link BindingServiceFactory} that returns the singleton
 * instance of the {@link Binder}.
 * 
 * @author Martin Morgenstern
 */
public class BinderFactory implements BindingServiceFactory {
    private static final BinderNG binder = new BinderNG();

    @Override
    public BinderNG getBindingService() {
        return binder;
    }
}
