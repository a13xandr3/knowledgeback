package br.com.teixeiraesteves.atividades.services;

import br.com.teixeiraesteves.atividades.entities.FileEntity;
import br.com.teixeiraesteves.atividades.repositories.FileRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
public class FileService {
    private final FileRepository repo;

    public FileService(FileRepository repo) { this.repo = repo; }

    /**
     * Salva (ou reaproveita) um arquivo enviado.
     * Idempotência garantida por UNIQUE(sha256_hex) no banco.
     */
    @Transactional
    public Long saveUploaded(
            String filename,
            String mimeType,
            String contentEncoding,  // 'gzip' | 'identity'
            String sha256Hex,
            long originalSizeBytes,
            Long gzipSizeBytes,
            byte[] payload
    ) {
        // --- validações e normalizações mínimas ---
        Objects.requireNonNull(filename, "filename");
        Objects.requireNonNull(sha256Hex, "sha256Hex");
        Objects.requireNonNull(payload, "payload");

        final String enc = contentEncoding == null ? "identity"
                : contentEncoding.trim().toLowerCase(Locale.ROOT);
        if (!enc.equals("gzip") && !enc.equals("identity")) {
            throw new IllegalArgumentException("contentEncoding inválido: " + enc);
        }

        final String normalizedHash = sha256Hex.trim().toLowerCase(Locale.ROOT);
        if (normalizedHash.length() != 64 || !normalizedHash.matches("^[0-9a-f]{64}$")) {
            throw new IllegalArgumentException("sha256Hex inválido (esperado 64 hex): " + sha256Hex);
        }

        final String safeMime = (mimeType == null || mimeType.isBlank())
                ? "application/octet-stream" : mimeType.trim();
        final String safeName = filename.isBlank() ? "file.bin" : filename.trim();

        // --- upsert por hash com proteção contra corrida via UNIQUE no DB ---
        FileEntity e = repo.findBySha256Hex(normalizedHash).orElse(null);
        if (e == null) {
            try {
                e = new FileEntity();
                e.setSha256Hex(normalizedHash);       // CHAVE DE IDEMPOTÊNCIA
                e.setFilename(safeName);
                e.setMimeType(safeMime);
                e.setContentEncoding(enc);
                e.setOriginalSizeBytes(originalSizeBytes);
                e.setGzipSizeBytes(gzipSizeBytes);
                e.setPayload(payload);

                // saveAndFlush aumenta a chance de detectar violação de unique nesta transação
                repo.saveAndFlush(e);
            } catch (DataIntegrityViolationException ex) {
                // Outro thread salvou primeiro → reaproveita o já persistido
                e = repo.findBySha256Hex(normalizedHash).orElseThrow(() -> ex);
            }
        } else {
            // Já existe com o mesmo hash: atualiza metadados se necessário.
            // Se preferir “congelar” metadados após o 1º upload, remova este bloco.
            e.setFilename(safeName);
            e.setMimeType(safeMime);
            e.setContentEncoding(enc);
            e.setOriginalSizeBytes(originalSizeBytes);
            e.setGzipSizeBytes(gzipSizeBytes);
            e.setPayload(payload);
            // Não precisa saveAndFlush: a transação fará flush.
        }
        return e.getId();
    }

    @Transactional(readOnly = true)
    public FileEntity getByIdOrThrow(Long id) {
        return repo.findById(id).orElseThrow(() ->
                new jakarta.persistence.EntityNotFoundException("File not found: " + id)
        );
    }

    /** Retorna Base64 dos bytes (payload) */
    public String toBase64(byte[] bytes) {
        if (bytes == null) return null;
        return Base64.getEncoder().encodeToString(bytes);
    }

    /** Descompacta GZIP para bytes originais */
    public byte[] gunzip(byte[] gzipBytes) {
        if (gzipBytes == null) return null;
        try (var gis = new java.util.zip.GZIPInputStream(new ByteArrayInputStream(gzipBytes));
             var bos = new ByteArrayOutputStream(Math.max(32, gzipBytes.length / 2))) {
            gis.transferTo(bos);
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao descomprimir gzip", e);
        }
    }

    /** Compacta bytes em GZIP (para /download-gzip quando payload é identity) */
    public byte[] gzip(byte[] bytes) {
        if (bytes == null) return null;
        try (var bos = new ByteArrayOutputStream();
             var gos = new java.util.zip.GZIPOutputStream(bos)) {
            gos.write(bytes);
            gos.finish();
            return bos.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao comprimir gzip", e);
        }
    }

    @Transactional(readOnly = true)
    public List<FileEntity> findAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        return repo.findAllById(ids);
    }

    @Transactional
    public void delete(FileEntity fe) {
        repo.delete(fe);
    }

    @Transactional
    public void deleteById(Long id) {
        FileEntity fe = getByIdOrThrow(id);
        repo.delete(fe);
    }

    @Transactional
    public void deleteAllByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return;
        repo.deleteAllByIdInBatch(ids);
    }
}
