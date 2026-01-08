package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaFilePayloadDTO {
    private String fileId; 
    private String name;
    private String owner;
    private String contentBase64;
}
