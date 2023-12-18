package ru.itmo.zavar.service.impl;

import com.google.protobuf.Timestamp;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itmo.zavar.service.CloudLoggingService;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;
import yandex.cloud.api.logging.v1.LogIngestionServiceGrpc;
import yandex.cloud.api.logging.v1.LogIngestionServiceOuterClass;
import yandex.cloud.api.logging.v1.LogResource;
import yandex.cloud.sdk.ServiceFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudLoggingServiceImpl implements CloudLoggingService {

    private final ServiceFactory serviceFactory;

    @Value("${yandex.log-group-id}")
    private String logGroupId;
    private LogIngestionServiceGrpc.LogIngestionServiceBlockingStub logIngestionServiceBlockingStub;

    @Value("${yandex.cloud-logging-enable}")
    private boolean cloudLoggingEnable;

    @PostConstruct
    public void init() {
        logIngestionServiceBlockingStub = serviceFactory.create(LogIngestionServiceGrpc.LogIngestionServiceBlockingStub.class,
                LogIngestionServiceGrpc::newBlockingStub);
    }

    @Override
    public LogIngestionServiceOuterClass.WriteResponse log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Object... arguments) {
        Instant now = Instant.now();
        message = MessageFormatter.arrayFormat(message, arguments).getMessage();
        LogIngestionServiceOuterClass.WriteRequest writeRequest = LogIngestionServiceOuterClass.WriteRequest.newBuilder()
                .setDestination(LogEntryOuterClass.Destination.newBuilder()
                        .setLogGroupId(logGroupId).build())
                .setResource(LogResource.LogEntryResource.newBuilder()
                        .setId(resourceId).build())
                .addEntries(LogEntryOuterClass.IncomingLogEntry.newBuilder()
                        .setLevel(level)
                        .setMessage(message)
                        .setTimestamp(Timestamp.newBuilder()
                                .setSeconds(now.getEpochSecond())
                                .setNanos(now.getNano()))
                        .build())
                .build();
        if(cloudLoggingEnable)
            return logIngestionServiceBlockingStub.write(writeRequest);
        else
            return null;
    }

    @Override
    public LogIngestionServiceOuterClass.WriteResponse log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        return this.log(level, resourceId, message.concat(sw.toString()));
    }
}
