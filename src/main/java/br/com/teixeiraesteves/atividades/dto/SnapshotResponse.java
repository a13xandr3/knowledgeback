package br.com.teixeiraesteves.atividades.dto;

import br.com.teixeiraesteves.atividades.entities.HashMode;

public record SnapshotResponse(
        Long id,
        String filename,
        String mimeType,
        String contentEncoding,     // 'gzip' | 'identity'
        Long gzipSizeBytes,         // pode ser null quando identity
        long originalSizeBytes,
        String sha256Hex,
        String base64Payload        // pode ser null se includeBase64=false
) {}