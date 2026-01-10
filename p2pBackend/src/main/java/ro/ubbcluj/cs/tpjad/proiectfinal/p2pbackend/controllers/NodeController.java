package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.services.FileRetrievalService;

@RestController
@RequestMapping("/node")
public class NodeController {

    private final FileRetrievalService fileRetrievalService;

    public NodeController(FileRetrievalService fileRetrievalService) {
        this.fileRetrievalService = fileRetrievalService;
    }

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.status(HttpStatus.OK).body("node OK");
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<ByteArrayResource> downloadFile(
            @org.springframework.web.bind.annotation.PathVariable String fileId,
            @org.springframework.web.bind.annotation.RequestParam String ownerId) {
        
        try {
            byte[] content = fileRetrievalService.retrieveAndReconstruct(fileId, ownerId);
            ByteArrayResource resource = new ByteArrayResource(content);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileId + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(content.length)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}


