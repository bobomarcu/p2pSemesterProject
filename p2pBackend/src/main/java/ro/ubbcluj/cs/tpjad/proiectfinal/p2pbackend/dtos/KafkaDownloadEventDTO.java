package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaDownloadEventDTO {
    private String fileId;
    private long timestamp;
}
