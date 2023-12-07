package ru.itmo.zavar.service.impl;

import com.google.protobuf.ByteString;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itmo.zavar.service.CloudLoggingService;
import ru.itmo.zavar.service.SpeechKitService;
import yandex.cloud.api.ai.stt.v3.RecognizerGrpc;
import yandex.cloud.api.ai.stt.v3.Stt;
import yandex.cloud.api.ai.tts.v3.SynthesizerGrpc;
import yandex.cloud.api.ai.tts.v3.Tts;
import yandex.cloud.api.logging.v1.LogEntryOuterClass;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j(topic = "SpeechKitService")
public class SpeechKitServiceImpl implements SpeechKitService {

    private final SynthesizerGrpc.SynthesizerStub ttsClient;
    private final RecognizerGrpc.RecognizerStub sttClient;
    private final CloudLoggingService cloudLoggingService;

    @Override
    public String recognize(byte[] audioInBytes) {
        var responseObserver = new SttStreamObserver(cloudLoggingService, log);
        var requestObserver = sttClient.recognizeStreaming(responseObserver);
        var frameRateHertz = 22050;

        log.info("Sending recognize request");
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "SpeechKitService", "Sending recognize request");
        requestObserver.onNext(initialSttRequest(frameRateHertz));

        var chunkStart = 0;
        var chunkSize = (int) (frameRateHertz * 2 * 0.2);
        while (chunkStart < audioInBytes.length) {
            chunkSize = Math.min(chunkSize, audioInBytes.length - chunkStart);
            var reqBuilder = Stt.StreamingRequest.newBuilder();
            reqBuilder.getChunkBuilder().setData(ByteString.copyFrom(audioInBytes, chunkStart, chunkSize));
            requestObserver.onNext(reqBuilder.build());
            chunkStart = chunkStart + chunkSize;
        }
        requestObserver.onCompleted();
        log.info("Done sending");
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "SpeechKitService", "Done sending");
        return responseObserver.awaitResult(5);
    }

    @Override
    public byte[] synthesize(String text) throws UnsupportedAudioFileException, IOException {
        var request = Tts.UtteranceSynthesisRequest
                .newBuilder()
                .setText(text)
                .setOutputAudioSpec(Tts.AudioFormatOptions
                        .newBuilder()
                        .setContainerAudio(Tts.ContainerAudio
                                .newBuilder()
                                .setContainerAudioType(Tts.ContainerAudio.ContainerAudioType.WAV)
                                .build()))
                .setLoudnessNormalizationType(Tts.UtteranceSynthesisRequest.LoudnessNormalizationType.LUFS)
                .build();

        var observer = new TtsStreamObserver(cloudLoggingService, log);

        log.info("Sending synthesize request");
        cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "SpeechKitService", "Sending synthesize request");
        ttsClient.utteranceSynthesis(request, observer);
        log.info("Done sending");
        var bytes = observer.awaitResult(5000);

        var audioStream = AudioSystem.getAudioInputStream(new ByteArrayInputStream(bytes));

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        AudioSystem.write(audioStream, AudioFileFormat.Type.WAVE, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    @RequiredArgsConstructor
    private static class TtsStreamObserver implements StreamObserver<Tts.UtteranceSynthesisResponse> {

        private final ByteArrayOutputStream result = new ByteArrayOutputStream();
        private static final CountDownLatch count = new CountDownLatch(1);
        private final CloudLoggingService cloudLoggingService;
        private final Logger log;

        @Override
        public void onNext(Tts.UtteranceSynthesisResponse response) {
            if (response.hasAudioChunk()) {
                try {
                    result.write(response.getAudioChunk().getData().toByteArray());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

        }

        @Override
        public void onError(Throwable t) {
            log.error("Tts streaming error occurred", t);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, "SpeechKitService", "Tts streaming error occurred", t);
        }

        @Override
        public void onCompleted() {
            log.info("Tts stream completed");
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "SpeechKitService", "Tts stream completed");
            count.countDown();
        }

        public byte[] awaitResult(int timeoutSeconds) {
            try {
                count.await(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return result.toByteArray();
        }
    }

    @RequiredArgsConstructor
    private static class SttStreamObserver implements StreamObserver<Stt.StreamingResponse> {

        private final StringBuilder result = new StringBuilder();
        private static final CountDownLatch count = new CountDownLatch(1);
        private final CloudLoggingService cloudLoggingService;
        private final Logger log;

        @Override
        public void onNext(Stt.StreamingResponse response) {
            response.getFinal()
                    .getAlternativesList()
                    .forEach(a -> result.append(a.getText().trim()).append(" "));
        }

        @Override
        public void onError(Throwable t) {
            log.error("Stt streaming error occurred", t);
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.ERROR, "SpeechKitService", "Stt streaming error occurred", t);
        }

        @Override
        public void onCompleted() {
            log.info("Stt stream completed");
            cloudLoggingService.log(LogEntryOuterClass.LogLevel.Level.INFO, "SpeechKitService", "Stt stream completed");
            count.countDown();
        }

        public String awaitResult(int timeoutSeconds) {
            try {
                count.await(timeoutSeconds, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return result.toString();
        }
    }

    private static Stt.StreamingRequest initialSttRequest(long frameRateHertz) {
        var builder = Stt.StreamingRequest.newBuilder();
        builder.getSessionOptionsBuilder()
                .setRecognitionModel(Stt.RecognitionModelOptions.newBuilder()
                        .setLanguageRestriction(Stt.LanguageRestrictionOptions.newBuilder()
                                .addLanguageCode("ru-RU")
                                .setRestrictionType(Stt.LanguageRestrictionOptions.LanguageRestrictionType.WHITELIST)
                                .build())
                        .setAudioFormat(Stt.AudioFormatOptions.newBuilder()
                                .setRawAudio(Stt.RawAudio.newBuilder()
                                        .setAudioChannelCount(1)
                                        .setSampleRateHertz(frameRateHertz)
                                        .setAudioEncoding(Stt.RawAudio.AudioEncoding.LINEAR16_PCM)
                                        .build()))
                        .setAudioProcessingType(Stt.RecognitionModelOptions.AudioProcessingType.REAL_TIME)
                        .build());
        return builder.build();
    }
}
