package es.luigi.chefsitoLuigi.Service.Impl;

import es.luigi.chefsitoLuigi.Service.WhisperService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@Slf4j
public class WhisperServiceImpl implements WhisperService {

    // TODO: Integrar con OpenAI Whisper API
    // Por ahora, implementación dummy para testing

    @Override
    public String transcribeAudio(MultipartFile audioFile) {
        try {
            log.info("🎤 Transcribiendo archivo de audio: {}", audioFile.getOriginalFilename());

            // Simular procesamiento
            Thread.sleep(1000);

            // Por ahora, devolver texto dummy basado en el nombre del archivo
            return generateDummyTranscription(audioFile.getOriginalFilename());

        } catch (Exception e) {
            log.error("❌ Error transcribiendo audio: {}", e.getMessage());
            return "No se pudo transcribir el audio";
        }
    }

    @Override
    public String transcribeAudio(byte[] audioData) {
        log.info("🎤 Transcribiendo datos de audio ({} bytes)", audioData.length);

        // Simular procesamiento
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Texto dummy para testing
        return "tomate cebolla ajo pollo";
    }

    private String generateDummyTranscription(String filename) {
        if (filename == null) return "ingredientes para cocinar";

        String lowerName = filename.toLowerCase();

        if (lowerName.contains("tomato") || lowerName.contains("tomate")) {
            return "tomate";
        } else if (lowerName.contains("chicken") || lowerName.contains("pollo")) {
            return "pollo";
        } else if (lowerName.contains("onion") || lowerName.contains("cebolla")) {
            return "cebolla";
        } else if (lowerName.contains("garlic") || lowerName.contains("ajo")) {
            return "ajo";
        } else {
            return "ingredientes para cocinar";
        }
    }
}