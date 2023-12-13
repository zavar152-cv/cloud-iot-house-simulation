package ru.itmo.zavar;

import com.google.common.io.Resources;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.itmo.zavar.service.impl.CloudLoggingServiceImpl;
import yandex.cloud.api.ai.stt.v3.RecognizerGrpc;
import yandex.cloud.api.ai.tts.v3.SynthesizerGrpc;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.auth.provider.CredentialProvider;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.UUID;

@SpringBootApplication
public class FaccControlApplication {

    @Value("${yandex.auth-key-file}")
    private String authKeyFile;
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
    public ServiceFactory yandexCloudServiceFactory() throws URISyntaxException, IOException {
        URL url = getClass().getResource(authKeyFile);
        String content = Resources.toString(url, StandardCharsets.UTF_8);
        CredentialProvider credentialProvider =
                Auth.apiKeyBuilder().fromJson(content)
                        .build();

        return ServiceFactory.builder()
                .credentialProvider(credentialProvider)
                .build();
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
