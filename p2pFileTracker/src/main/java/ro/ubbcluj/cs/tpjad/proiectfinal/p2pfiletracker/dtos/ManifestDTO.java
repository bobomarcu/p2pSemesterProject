package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ManifestDTO {
    private String name;
    private String description;
    private String owner;
    private String userId;
    private boolean isPrivate;
}
