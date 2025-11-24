package es.luigi.chefsitoLuigi.Service;

import org.springframework.web.multipart.MultipartFile;

public interface WhisperService {
    String transcribeAudio(MultipartFile audioFile);
    String transcribeAudio(byte[] audioData);
}