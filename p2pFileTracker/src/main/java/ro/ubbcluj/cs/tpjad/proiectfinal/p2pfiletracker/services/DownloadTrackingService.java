package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.KafkaDownloadEventDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.entities.ManifestEntity;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.repositories.ManifestRepository;

import java.util.Optional;

@Service
@Slf4j
public class DownloadTrackingService {

    private final ManifestRepository manifestRepository;
    private final ObjectMapper objectMapper;

    public DownloadTrackingService(ManifestRepository manifestRepository, ObjectMapper objectMapper) {
        this.manifestRepository = manifestRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "p2p-file-downloads", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        try {
            KafkaDownloadEventDTO event = objectMapper.convertValue(record.value(), KafkaDownloadEventDTO.class);
            log.info("Received download event for fileId: {}", event.getFileId());

            Optional<ManifestEntity> manifestOpt = manifestRepository.findById(event.getFileId());
            if (manifestOpt.isPresent()) {
                ManifestEntity manifest = manifestOpt.get();
                manifest.setDownloadCount(manifest.getDownloadCount() + 1);
                manifestRepository.save(manifest);
                log.info("Incremented download count for fileId: {}. New count: {}", event.getFileId(), manifest.getDownloadCount());
            } else {
                log.warn("Manifest not found for fileId: {}", event.getFileId());
            }
        } catch (Exception e) {
            log.error("Error processing download event", e);
        }
    }
}
