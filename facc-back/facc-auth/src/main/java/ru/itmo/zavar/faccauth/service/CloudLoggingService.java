package ru.itmo.zavar.faccauth.service;

import yandex.cloud.api.logging.v1.LogEntryOuterClass;
import yandex.cloud.api.logging.v1.LogIngestionServiceOuterClass;

public interface CloudLoggingService {
    void log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Object... arguments);

    void log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Throwable throwable);
}
