package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos.KafkaFileDeleteDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.grpc.ShardId;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.grpc.ShardResponse;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models.Node;

import java.util.List;

import org.apache.kafka.clients.consumer.ConsumerRecord;

@Service
@Slf4j
public class FileDeletionService {

    private final ClusterService clusterService;
    private final ShardStorageService shardStorageService;
    private final ObjectMapper objectMapper;

    public FileDeletionService(ClusterService clusterService, ShardStorageService shardStorageService, ObjectMapper objectMapper) {
        this.clusterService = clusterService;
        this.shardStorageService = shardStorageService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "p2p-file-deletes", containerFactory = "kafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, Object> record) {
        KafkaFileDeleteDTO payload = objectMapper.convertValue(record.value(), KafkaFileDeleteDTO.class);
        log.info("Received delete request for file: {} (Owner: {})", payload.getFileId(), payload.getOwnerId());
        
        deleteLocally(payload.getFileId(), payload.getOwnerId());
        broadcastDelete(payload.getFileId(), payload.getOwnerId());
    }

    private void deleteLocally(String fileId, String ownerId) {
        for (int i = 0; i < 4; i++) {
            String shardId = fileId + "_" + i;
            shardStorageService.deleteShard(shardId, fileId, ownerId);
        }
    }

    private void broadcastDelete(String fileId, String ownerId) {
        List<Node> peers = clusterService.getPeers();
        if (peers.isEmpty()) {
            log.info("No peers to broadcast delete to.");
            return;
        }

        for (Node peer : peers) {
            try {
                for (int i = 0; i < 4; i++) {
                    String shardId = fileId + "_" + i;
                    log.info("Requesting node {} to delete shard {}", peer.getId(), shardId);
                    
                    ShardResponse response = clusterService.getPeerStub(peer).deleteShard(
                            ShardId.newBuilder()
                                    .setShardId(shardId)
                                    .setDocumentId(fileId)
                                    .setOwnerId(ownerId)
                                    .build()
                    );
                    
                    if (response.getSuccess()) {
                        log.debug("Node {} confirmed deletion of {}", peer.getId(), shardId);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to broadcast delete to node {}", peer.getId(), e);
            }
        }
    }
}
