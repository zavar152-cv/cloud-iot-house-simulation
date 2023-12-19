package ru.itmo.zavar;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import yandex.cloud.api.ai.stt.v3.RecognizerGrpc;
import yandex.cloud.api.ai.tts.v3.SynthesizerGrpc;

import java.util.UUID;

@SpringBootApplication
public class FaccControlApplication {
    @Value("${yandex.speech-kit.api-key}")
    private String speechKitApiKey;
    @Value("${yandex.speech-kit.tts.host}")
    private String ttsHost;
    @Value("${yandex.speech-kit.tts.port}")
    private int ttsPort;
    @Value("${yandex.speech-kit.stt.host}")
    private String sttHost;
    @Value("${yandex.speech-kit.stt.port}")
    private int sttPort;

    public static void main(String[] args) {
        SpringApplication.run(FaccControlApplication.class, args);
    }

    @Bean
    public SynthesizerGrpc.SynthesizerStub ttsV3Client() {
        var channel = ManagedChannelBuilder
                .forAddress(ttsHost, ttsPort)
                .build();

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Api-Key " + speechKitApiKey);
        var requestId = UUID.randomUUID().toString();
        headers.put(Metadata.Key.of("x-client-request-id", Metadata.ASCII_STRING_MARSHALLER), requestId);

        return SynthesizerGrpc.newStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }

    @Bean
    public RecognizerGrpc.RecognizerStub sttV3Client() {
        var channel = ManagedChannelBuilder
                .forAddress(sttHost, sttPort)
                .build();

        Metadata headers = new Metadata();
        headers.put(Metadata.Key.of("authorization", Metadata.ASCII_STRING_MARSHALLER), "Api-Key " + speechKitApiKey);
        var requestId = UUID.randomUUID().toString();
        headers.put(Metadata.Key.of("x-client-request-id", Metadata.ASCII_STRING_MARSHALLER), requestId);

        return RecognizerGrpc.newStub(channel)
                .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
    }
}
