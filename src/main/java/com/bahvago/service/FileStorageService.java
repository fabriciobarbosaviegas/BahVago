package com.bahvago.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;

import java.util.ArrayList;
import java.util.List;

@Service
public class FileStorageService {

    private final Path uploadBaseDir = Paths.get("uploads").toAbsolutePath().normalize();

    public String salvarArquivo(MultipartFile file, String subpasta) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "imagem.jpg");
        String nomeArquivo = System.currentTimeMillis() + "_" + originalFilename;

        try {
            Path targetDir = this.uploadBaseDir.resolve(subpasta).normalize();
            if (!Files.exists(targetDir)) {
                Files.createDirectories(targetDir);
            }

            Path targetPath = targetDir.resolve(nomeArquivo);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return "/uploads/" + subpasta + "/" + nomeArquivo;
        } catch (IOException e) {
            throw new RuntimeException("Falha ao armazenar o arquivo de imagem: " + originalFilename, e);
        }
    }

    public List<String> salvarArquivos(List<MultipartFile> files, String subpasta) {
        List<String> urls = new ArrayList<>();
        if (files != null && !files.isEmpty()) {
            for (MultipartFile file : files) {
                if (file != null && !file.isEmpty()) {
                    String url = salvarArquivo(file, subpasta);
                    if (url != null) {
                        urls.add(url);
                    }
                }
            }
        }
        return urls;
    }
}
