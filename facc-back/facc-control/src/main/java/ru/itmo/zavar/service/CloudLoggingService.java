package ru.itmo.zavar.service;

import yandex.cloud.api.logging.v1.LogEntryOuterClass;
import yandex.cloud.api.logging.v1.LogIngestionServiceOuterClass;

public interface CloudLoggingService {
    LogIngestionServiceOuterClass.WriteResponse log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Object... arguments);

    LogIngestionServiceOuterClass.WriteResponse log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Throwable throwable);
}
