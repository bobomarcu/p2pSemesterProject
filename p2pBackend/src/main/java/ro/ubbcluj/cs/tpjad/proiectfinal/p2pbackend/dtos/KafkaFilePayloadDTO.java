package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The payload sent to the Kafka topic for the P2P Cluster to consume.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaFilePayloadDTO {
    private String fileId; // Generated UUID
    private String name;
    private String owner;
    private String contentBase64;
}
