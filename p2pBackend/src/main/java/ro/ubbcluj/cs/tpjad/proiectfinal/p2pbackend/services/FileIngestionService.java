package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos.KafkaFilePayloadDTO;

import java.util.Base64;

@Service
@Slf4j
public class FileIngestionService {

    private final ClusterService clusterService;

    public FileIngestionService(ClusterService clusterService) {
        this.clusterService = clusterService;
    }

    @KafkaListener(topics = "p2p-file-uploads", containerFactory = "kafkaListenerContainerFactory")
    public void listen(KafkaFilePayloadDTO payload) {
        log.info("Received file: {} (ID: {}) from owner: {}", payload.getName(), payload.getFileId(), payload.getOwner());
        
        try {
            byte[] fileContent = Base64.getDecoder().decode(payload.getContentBase64());
            log.info("Decoded file size: {} bytes", fileContent.length);
            
            // TODO: Implement sharding and distribution logic
            // distributeFile(payload.getFileId(), fileContent);
            
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 content for file ID: {}", payload.getFileId(), e);
        }
    }
}
