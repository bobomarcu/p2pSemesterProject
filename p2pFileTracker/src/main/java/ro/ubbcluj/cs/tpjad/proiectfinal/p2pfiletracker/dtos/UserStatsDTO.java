package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserStatsDTO {
    private long uploadCount;
    private long totalDownloadsReceived;
}
