package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/node")
public class NodeController {

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.status(HttpStatus.OK).body("node OK");
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<String> downloadFile(@org.springframework.web.bind.annotation.PathVariable String fileId) {
        // TODO: Implement actual file retrieval logic. For now, returning a mock response.
        return ResponseEntity.ok("Content of file " + fileId + " from P2P Node.");
    }

}


