package ru.itmo.zavar.service;

import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;

public interface SpeechKitService {
    String recognize(byte[] audioInBytes);

    byte[] synthesize(String text) throws UnsupportedAudioFileException, IOException;
}
