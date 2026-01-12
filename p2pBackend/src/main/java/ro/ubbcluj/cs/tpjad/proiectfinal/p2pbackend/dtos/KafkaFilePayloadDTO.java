package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class KafkaFilePayloadDTO {
    private String fileId; // Generated UUID
    private String name;
    private String owner;
    private String contentBase64;
}
