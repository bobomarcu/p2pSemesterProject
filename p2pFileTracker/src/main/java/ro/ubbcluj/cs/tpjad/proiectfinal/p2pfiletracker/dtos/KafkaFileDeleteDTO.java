package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaFileDeleteDTO {
    private String fileId;
    private String ownerId;
}
