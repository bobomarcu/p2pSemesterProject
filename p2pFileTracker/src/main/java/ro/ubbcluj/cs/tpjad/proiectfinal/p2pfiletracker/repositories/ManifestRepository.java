package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.entities.ManifestEntity;

import java.util.List;

public interface ManifestRepository extends JpaRepository<ManifestEntity, String> {
    List<ManifestEntity> findByIsPrivateFalse();
    List<ManifestEntity> findTop5ByIsPrivateFalseOrderByDownloadCountDesc();

    long countByUserId(String userId);
    
    List<ManifestEntity> findByUserId(String userId);

    @Query("SELECT COALESCE(SUM(m.downloadCount), 0) FROM ManifestEntity m WHERE m.userId = :userId")
    long sumDownloadsByUserId(String userId);
}
