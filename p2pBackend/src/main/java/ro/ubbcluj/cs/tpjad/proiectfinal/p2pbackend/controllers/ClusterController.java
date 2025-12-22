package ro.ubbcluj.cs.tpjad.proiectfinal.p2pbackend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cluster")
public class ClusterController {

    @GetMapping("/status")
    public ResponseEntity<?> status() {
        return ResponseEntity.status(HttpStatus.OK).body("Cluster OK");
    }

    @PostMapping("/initialize")
    public ResponseEntity<?> initialize() {
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

}
