package rolevm.api.service;

public interface BindingService {
    void bind(Object player, Object role);

    void unbind(Object player, Object role);
}
