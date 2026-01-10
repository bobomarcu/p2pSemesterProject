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

@Service
@Slf4j
public class TrackerService {

    private final ManifestRepository manifestRepository;
    private final KafkaTemplate<String, KafkaFilePayloadDTO> kafkaTemplate;
    
    // TODO: externalize this to application.yml
    private static final String KAFKA_TOPIC = "p2p-file-uploads";

    public TrackerService(ManifestRepository manifestRepository, KafkaTemplate<String, KafkaFilePayloadDTO> kafkaTemplate) {
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
        kafkaTemplate.send(KAFKA_TOPIC, entity.getId(), payload);
        log.info("Published file payload to Kafka topic: {}", KAFKA_TOPIC);

        return entity;
    }

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
            } else {
                log.warn("User {} attempted to delete manifest {} owned by {}", userId, manifestId, manifest.getUserId());
                throw new RuntimeException("Unauthorized to delete this manifest");
            }
        });
    }
}
