import java.util.*;

public class DeviceRegistry {
    private final java.util.List<Object> devices = new ArrayList<>();

    public void add(Object d) { devices.add(d); }

    public <T> T getOnly(Class<T> capability) {
        T found = null;
        for (Object d : devices) {
            if (capability.isInstance(d)) {
                if (found != null) {
                    throw new IllegalStateException("Multiple devices for capability: " + capability.getSimpleName());
                }
                found = capability.cast(d);
            }
        }
        if (found == null) {
            throw new IllegalStateException("Missing capability: " + capability.getSimpleName());
        }
        return found;
    }

    public <T> java.util.List<T> getAll(Class<T> capability) {
        java.util.List<T> result = new ArrayList<>();
        for (Object d : devices) {
            if (capability.isInstance(d)) {
                result.add(capability.cast(d));
            }
        }
        return result;
    }
}
