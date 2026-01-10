package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services;

import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.grpc.ShardId;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.grpc.ShardResponse;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.models.Node;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.kafka.core.KafkaTemplate;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos.KafkaDownloadEventDTO;

@Service
@Slf4j
public class FileRetrievalService {

    private final ClusterService clusterService;
    private final ShardStorageService shardStorageService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    private static final String KAFKA_TOPIC_DOWNLOAD = "p2p-file-downloads";

    public FileRetrievalService(ClusterService clusterService, ShardStorageService shardStorageService, KafkaTemplate<String, Object> kafkaTemplate) {
        this.clusterService = clusterService;
        this.shardStorageService = shardStorageService;
        this.kafkaTemplate = kafkaTemplate;
    }

    public byte[] retrieveAndReconstruct(String fileId, String ownerId) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        // We know there are 4 shards: fileId_0, fileId_1, fileId_2, fileId_3
        for (int i = 0; i < 4; i++) {
            String shardId = fileId + "_" + i;
            byte[] shardContent = fetchShard(shardId, fileId, ownerId);
            
            if (shardContent == null) {
                throw new IOException("Failed to retrieve shard " + shardId + ". File reconstruction impossible.");
            }
            
            outputStream.write(shardContent);
        }

        log.info("Successfully reconstructed file {}", fileId);
        
        // Notify Tracker about download
        try {
            KafkaDownloadEventDTO downloadEvent = new KafkaDownloadEventDTO(fileId, System.currentTimeMillis());
            kafkaTemplate.send(KAFKA_TOPIC_DOWNLOAD, fileId, downloadEvent);
            log.info("Published download event for fileId: {}", fileId);
        } catch (Exception e) {
            log.error("Failed to publish download event for fileId: {}", fileId, e);
        }
        
        return outputStream.toByteArray();
    }

    private byte[] fetchShard(String shardId, String documentId, String ownerId) {
        // 1. Try local storage
        try {
            byte[] local = shardStorageService.getShard(shardId, documentId, ownerId);
            log.info("Found shard {} locally", shardId);
            return local;
        } catch (IOException e) {
            log.info("Shard {} not found locally (Reason: {}), querying cluster...", shardId, e.getMessage());
        }

        // 2. Query peers via gRPC
        List<Node> peers = clusterService.getPeers(); // Don't need self since we already checked local
        for (Node peer : peers) {
            try {
                log.debug("Querying node {} for shard {}", peer.getId(), shardId);
                ShardResponse response = clusterService.getPeerStub(peer).getShard(
                        ShardId.newBuilder()
                                .setShardId(shardId)
                                .setDocumentId(documentId)
                                .setOwnerId(ownerId)
                                .build()
                );

                if (response.getSuccess()) {
                    log.info("Retrieved shard {} from node {}", shardId, peer.getId());
                    return response.getContent().toByteArray();
                }
            } catch (Exception e) {
                log.warn("Failed to query node {} for shard {}: {}", peer.getId(), shardId, e.getMessage());
            }
        }

        log.error("Shard {} could not be found in the cluster", shardId);
        return null;
    }
}
