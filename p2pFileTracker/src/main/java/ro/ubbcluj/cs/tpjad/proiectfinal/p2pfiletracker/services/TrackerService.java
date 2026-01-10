package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.FileUploadRequestDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.KafkaFilePayloadDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.ManifestDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.entities.ManifestEntity;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.repositories.ManifestRepository;

import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.UserStatsDTO;

import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.KafkaFileDeleteDTO;

@Service
@Slf4j
public class TrackerService {

    private final ManifestRepository manifestRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate; // Changed to Object to support multiple types
    
    private static final String KAFKA_TOPIC_UPLOAD = "p2p-file-uploads";
    private static final String KAFKA_TOPIC_DELETE = "p2p-file-deletes";

    public TrackerService(ManifestRepository manifestRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.manifestRepository = manifestRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Transactional
    public ManifestEntity uploadFile(FileUploadRequestDTO request) {
        ManifestDTO meta = request.getMetadata();
        
        log.info("Uploading file for UserID: {}", meta.getUserId());

        // 1. Save metadata to DB
        ManifestEntity entity = new ManifestEntity(
                meta.getName(),
                meta.getDescription(),
                meta.getOwner(),
                meta.getUserId(),
                meta.isPrivate()
        );
        entity = manifestRepository.save(entity);
        log.info("Saved manifest with ID: {} for UserID: {}", entity.getId(), entity.getUserId());

        // 2. Prepare payload for Kafka
        KafkaFilePayloadDTO payload = new KafkaFilePayloadDTO(
                entity.getId(),
                entity.getName(),
                entity.getOwner(),
                request.getFileContentBase64()
        );

        // 3. Send to Kafka
        kafkaTemplate.send(KAFKA_TOPIC_UPLOAD, entity.getId(), payload);
        log.info("Published file payload to Kafka topic: {}", KAFKA_TOPIC_UPLOAD);

        return entity;
    }

    // ... (getAllPublicManifests, getTop5, getUserStats, getUserManifests methods) ...

    public java.util.List<ManifestEntity> getAllPublicManifests() {
        return manifestRepository.findByIsPrivateFalse();
    }

    public java.util.List<ManifestEntity> getTop5PopularManifests() {
        return manifestRepository.findTop5ByIsPrivateFalseOrderByDownloadCountDesc();
    }

    public UserStatsDTO getUserStats(String userId) {
        log.info("Fetching stats for UserID: {}", userId);
        long uploadCount = manifestRepository.countByUserId(userId);
        long totalDownloads = manifestRepository.sumDownloadsByUserId(userId);
        log.info("Stats for {}: uploads={}, downloads={}", userId, uploadCount, totalDownloads);
        return new UserStatsDTO(uploadCount, totalDownloads);
    }

    public java.util.List<ManifestEntity> getUserManifests(String userId) {
        return manifestRepository.findByUserId(userId);
    }

    @Transactional
    public void deleteManifest(String manifestId, String userId) {
        manifestRepository.findById(manifestId).ifPresent(manifest -> {
            if (manifest.getUserId().equals(userId)) {
                manifestRepository.delete(manifest);
                log.info("Deleted manifest {} for UserID: {}", manifestId, userId);
                
                // Trigger P2P deletion
                KafkaFileDeleteDTO deletePayload = new KafkaFileDeleteDTO(manifestId, manifest.getOwner());
                kafkaTemplate.send(KAFKA_TOPIC_DELETE, manifestId, deletePayload);
                log.info("Published delete event to Kafka topic: {}", KAFKA_TOPIC_DELETE);
                
            } else {
                log.warn("User {} attempted to delete manifest {} owned by {}", userId, manifestId, manifest.getUserId());
                throw new RuntimeException("Unauthorized to delete this manifest");
            }
        });
    }

    @Transactional
    public ManifestEntity updateManifest(String id, ManifestDTO updates, String userId) {
        return manifestRepository.findById(id).map(manifest -> {
            if (!manifest.getUserId().equals(userId)) {
                throw new RuntimeException("Unauthorized to edit this manifest");
            }
            manifest.setName(updates.getName());
            manifest.setDescription(updates.getDescription());
            manifest.setPrivate(updates.isPrivate());
            log.info("Updated manifest {} for UserID: {}", id, userId);
            return manifestRepository.save(manifest);
        }).orElseThrow(() -> new RuntimeException("Manifest not found"));
    }
}
