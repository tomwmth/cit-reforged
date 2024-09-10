package dev.tomwmth.citreforged.api;

/**
 * @author Thomas Wearmouth
 * Created on 07/09/2024
 */
public interface CITRegistrar {
    void register(CITDisposable disposable);

    void register(String namespace, CITGlobalProperties globalProperties);

    void register(String namespace, CITTypeContainer<?> type);

    void register(String namespace, CITConditionContainer<?> condition);
}
