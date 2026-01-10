package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.configurations.ClusterConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class ShardStorageService {

    private final ClusterConfiguration clusterConfiguration;
    private final ObjectMapper objectMapper;

    public ShardStorageService(ClusterConfiguration clusterConfiguration, ObjectMapper objectMapper) {
        this.clusterConfiguration = clusterConfiguration;
        this.objectMapper = objectMapper;
    }

    public void saveShard(String shardId, String documentId, String ownerId, byte[] content) throws IOException {
        String basePath = clusterConfiguration.getStoragePath();
        // Path: parentPathFromConfig/ownerId/documentId/shardId.json
        Path dirPath = Paths.get(basePath, ownerId, documentId);
        Files.createDirectories(dirPath);

        File file = dirPath.resolve(shardId + ".json").toFile();
        
        ShardJson json = new ShardJson(shardId, documentId, ownerId, Base64.getEncoder().encodeToString(content));
        objectMapper.writeValue(file, json);
        
        log.info("Saved shard {} to {}", shardId, file.getAbsolutePath());
    }

    public byte[] getShard(String shardId, String documentId, String ownerId) throws IOException {
        String basePath = clusterConfiguration.getStoragePath();
        Path filePath = Paths.get(basePath, ownerId, documentId, shardId + ".json");
        
        if (!Files.exists(filePath)) {
            log.warn("Shard file not found at: {}", filePath.toAbsolutePath());
            throw new IOException("Shard not found: " + filePath.toAbsolutePath());
        }

        try {
            ShardJson json = objectMapper.readValue(filePath.toFile(), ShardJson.class);
            return Base64.getDecoder().decode(json.content);
        } catch (Exception e) {
            log.error("Failed to parse shard file at {}", filePath, e);
            throw new IOException("Corrupt shard file", e);
        }
    }

    public boolean deleteShard(String shardId, String documentId, String ownerId) {
        String basePath = clusterConfiguration.getStoragePath();
        Path filePath = Paths.get(basePath, ownerId, documentId, shardId + ".json");
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Deleted shard {} at {}", shardId, filePath);
                // Clean up empty directories if possible (optional)
                try {
                    Files.deleteIfExists(Paths.get(basePath, ownerId, documentId)); // delete doc dir if empty
                    Files.deleteIfExists(Paths.get(basePath, ownerId)); // delete owner dir if empty
                } catch (IOException ignored) {}
            } else {
                log.warn("Shard {} not found for deletion at {}", shardId, filePath);
            }
            return deleted;
        } catch (IOException e) {
            log.error("Failed to delete shard {}", filePath, e);
            return false;
        }
    }

    @Data
    @AllArgsConstructor
    @lombok.NoArgsConstructor
    public static class ShardJson {
        public String shardId;
        public String documentId;
        public String ownerId;
        public String content;
    }
}