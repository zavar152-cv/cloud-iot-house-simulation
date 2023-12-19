package ru.itmo.zavar.service.impl;

import com.google.protobuf.Timestamp;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudLoggingServiceImpl implements CloudLoggingService {

    @Value("${yandex.logging.log-group-id}")
    private String logGroupId;
    @Value("${yandex.logging.enable}")
    private boolean cloudLoggingEnable;
    @Value("${yandex.logging.host}")
    private String loggingHost;
    @Value("${yandex.logging.port}")
    private int loggingPort;
    @Value("${yandex.logging.iam-key}")
    private String loggingIamKey;

    private LogIngestionServiceGrpc.LogIngestionServiceStub logIngestionServiceStub;
    private final SimpleAsyncTaskExecutor sate = new SimpleAsyncTaskExecutor();

    @PostConstruct
    public void init() {

        var channel = ManagedChannelBuilder
                .forAddress(loggingHost, loggingPort)
                .build();

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Bearer " + loggingIamKey);
        var requestId = UUID.randomUUID().toString();
        headers.put(Metadata.Key.of("x-client-request-id", Metadata.ASCII_STRING_MARSHALLER), requestId);

        logIngestionServiceStub = LogIngestionServiceGrpc.newStub(channel).withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @Override
    public void log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Object... arguments) {
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
        if(cloudLoggingEnable) {
            sate.execute(() -> logIngestionServiceStub.write(writeRequest, new LogStreamObserver()));
        }
    }

    private static class LogStreamObserver implements StreamObserver<LogIngestionServiceOuterClass.WriteResponse> {

        @Override
        public void onNext(LogIngestionServiceOuterClass.WriteResponse writeResponse) {
            log.debug(writeResponse.toString());
        }

        @Override
        public void onError(Throwable throwable) {
            log.error("LogStreamObserver error", throwable);
        }

        @Override
        public void onCompleted() {
            log.debug("Completed LogStreamObserver");
        }
    }

    @Override
    public void log(LogEntryOuterClass.LogLevel.Level level, String resourceId, String message, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        this.log(level, resourceId, message.concat(sw.toString()));
    }
}
