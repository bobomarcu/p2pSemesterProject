package ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.FileUploadRequestDTO;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.entities.ManifestEntity;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.services.TrackerService;

import ro.ubbcluj.cs.tpjad.proiectfinal.p2pfiletracker.dtos.UserStatsDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/tracker")
@CrossOrigin(origins = "*") // Allow frontend access
public class TrackerController {

    private static final Logger log = LoggerFactory.getLogger(TrackerController.class);
    private final TrackerService trackerService;

    public TrackerController(TrackerService trackerService) {
        this.trackerService = trackerService;
    }

    @GetMapping("/manifests")
    public ResponseEntity<java.util.List<ManifestEntity>> getManifests() {
        return ResponseEntity.ok(trackerService.getAllPublicManifests());
    }

    @GetMapping("/manifests/top5")
    public ResponseEntity<java.util.List<ManifestEntity>> getTop5Manifests() {
        return ResponseEntity.ok(trackerService.getTop5PopularManifests());
    }

    @GetMapping("/stats")
    public ResponseEntity<UserStatsDTO> getUserStats(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Stats request for UserID from Token: {}", userId);
        return ResponseEntity.ok(trackerService.getUserStats(userId));
    }

    @GetMapping("/user/manifests")
    public ResponseEntity<java.util.List<ManifestEntity>> getUserManifests(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(trackerService.getUserManifests(userId));
    }

    @DeleteMapping("/manifests/{id}")
    public ResponseEntity<Void> deleteManifest(@PathVariable String id, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        trackerService.deleteManifest(id, userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/upload")
    public ResponseEntity<ManifestEntity> uploadFile(@RequestBody FileUploadRequestDTO request, @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        log.info("Upload request for UserID from Token: {}", userId);
        // Overwrite userId with the one from the token
        request.getMetadata().setUserId(userId);
        ManifestEntity created = trackerService.uploadFile(request);
        return ResponseEntity.ok(created);
    }
}
