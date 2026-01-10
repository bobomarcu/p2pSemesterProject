package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos.KafkaFilePayloadDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.grpc.ShardRequest;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models.Node;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@Service
@Slf4j
public class FileIngestionService {

    private final ClusterService clusterService;
    private final ShardStorageService shardStorageService;
    private final ObjectMapper objectMapper;

    public FileIngestionService(ClusterService clusterService, ShardStorageService shardStorageService, ObjectMapper objectMapper) {
        this.clusterService = clusterService;
        this.shardStorageService = shardStorageService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "p2p-file-uploads", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        KafkaFilePayloadDTO payload = objectMapper.convertValue(record.value(), KafkaFilePayloadDTO.class);
        log.info("Received file: {} (ID: {}) from owner: {}", payload.getName(), payload.getFileId(), payload.getOwner());
        
        try {
            byte[] fileContent = Base64.getDecoder().decode(payload.getContentBase64());
            log.info("Decoded file size: {} bytes", fileContent.length);
            
            // Shard into 4 sections
            int totalSize = fileContent.length;
            int shardSize = (int) Math.ceil((double) totalSize / 4);
            
            for (int i = 0; i < 4; i++) {
                int start = i * shardSize;
                int end = Math.min(start + shardSize, totalSize);
                if (start >= totalSize) break; // Should not happen if size > 0

                byte[] shardContent = Arrays.copyOfRange(fileContent, start, end);
                String shardId = payload.getFileId() + "_" + i;
                
                distributeShard(shardId, payload.getFileId(), payload.getOwner(), shardContent);
            }
            
        } catch (IllegalArgumentException e) {
            log.error("Failed to decode Base64 content for file ID: {}", payload.getFileId(), e);
        }
    }

    private void distributeShard(String shardId, String docId, String ownerId, byte[] content) {
        List<Node> allNodes = clusterService.getAllNodes();
        if (allNodes.isEmpty()) {
            log.error("No nodes available to distribute shard {}", shardId);
            return;
        }

        // Replication factor 2
        int replication = Math.min(2, allNodes.size());
        List<Node> targets = new ArrayList<>(allNodes);
        Collections.shuffle(targets);
        List<Node> selectedNodes = targets.subList(0, replication);

        for (Node node : selectedNodes) {
            try {
                // If storing to self, use local service to save overhead
                // Note: comparing ID might need exact port match. getAllNodes includes self with correct ports.
                // Assuming ClusterService.getSelfNode() matches one of the nodes or is added to list.
                // Actually getAllNodes adds self.
                
                // Ideally we check if node is local. 
                // Simple check: host is localhost/hostname and ports match. 
                // Or just use gRPC for everyone for simplicity and uniformity, 
                // but local optimization is requested in plan.
                // Since I cannot easily check "is this me" perfectly without a dedicated ID in config,
                // I will use the ID string comparison if configured correctly.
                
                // But wait, ClusterService generates self node on the fly. 
                // Let's assume we just use gRPC for now to ensure it works, 
                // or try-catch the storage service if it's "localhost".
                
                // Using gRPC for all is safer for this prototype stage to ensure correct protocol usage.
                // However, I will try to use local if possible.
                // Let's stick to gRPC distribution as requested ("sharding should go with grpc").
                
                log.info("Replicating shard {} to node {}", shardId, node.getId());
                clusterService.getPeerStub(node).storeShard(ShardRequest.newBuilder()
                        .setShardId(shardId)
                        .setDocumentId(docId)
                        .setOwnerId(ownerId)
                        .setContent(ByteString.copyFrom(content))
                        .build());
                        
            } catch (Exception e) {
                log.error("Failed to replicate shard {} to node {}", shardId, node.getId(), e);
            }
        }
    }
}
