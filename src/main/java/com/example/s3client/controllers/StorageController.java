package com.example.s3client.controllers;

import com.example.s3client.domain.ResponseModel;
import com.example.s3client.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * @author Vyacheslav Aleferov
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class StorageController {
    private final StorageService storageService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<UUID> saveFile(@PathVariable MultipartFile file) throws IOException {
        return ResponseEntity.ok(storageService.upload(file));
    }

    @GetMapping(value = "/{fileName}")
    public ResponseEntity<Resource> getFile(@PathVariable String fileName) throws IOException {
        ResponseModel responseModel = storageService.download(fileName);
        ByteArrayResource byteArrayResource = new ByteArrayResource(responseModel.getData());
        return ResponseEntity.ok()
                .contentLength(responseModel.getData().length)
                .header("Content-Disposition", "file; filename=" + responseModel.getNameEncoded())
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .body(byteArrayResource);
    }

    @GetMapping("/print")
    public ResponseEntity<Map<String, Long>> printAllFiles() {
        return ResponseEntity.ok(storageService.printAllFiles());
    }

    @DeleteMapping("/{fileName}")
    public ResponseEntity deleteFile(@PathVariable String fileName) {
        storageService.delete(fileName);
        return ResponseEntity.noContent().build();
    }


}
