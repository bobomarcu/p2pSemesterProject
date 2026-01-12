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
    private final ObjectMapper objectMapper;

    public FileIngestionService(ClusterService clusterService, ObjectMapper objectMapper) {
        this.clusterService = clusterService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "p2p-file-uploads", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        KafkaFilePayloadDTO payload = objectMapper.convertValue(record.value(), KafkaFilePayloadDTO.class);
        log.info("Received file: {} (ID: {}) from owner: {}", payload.getName(), payload.getFileId(), payload.getOwner());
        
        try {
            byte[] fileContent = Base64.getDecoder().decode(payload.getContentBase64());
            log.info("Decoded file size: {} bytes", fileContent.length);
            
            int totalSize = fileContent.length;
            int shardSize = (int) Math.ceil((double) totalSize / 4);
            
            for (int i = 0; i < 4; i++) {
                int start = i * shardSize;
                int end = Math.min(start + shardSize, totalSize);
                if (start >= totalSize) break;

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
