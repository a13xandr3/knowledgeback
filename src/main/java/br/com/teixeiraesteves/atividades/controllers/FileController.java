package br.com.teixeiraesteves.atividades.controllers;

import br.com.teixeiraesteves.atividades.dto.IdsRequest;
import br.com.teixeiraesteves.atividades.entities.FileEntity;
import br.com.teixeiraesteves.atividades.services.FileService;
import br.com.teixeiraesteves.atividades.dto.FileSavedResponse;
import br.com.teixeiraesteves.atividades.dto.SnapshotResponse;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/files")
public class FileController {
    private final FileService svc;

    public FileController(FileService svc) { this.svc = svc; }

    /**
     * Upload de um arquivo binário (payload) com seus metadados.
     * O 'file' contém exatamente o que veio do front em 'payloadBytes'.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileSavedResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam @NotBlank String contentEncoding, // 'gzip' | 'identity'
            @RequestParam @NotBlank String hashSha256Hex,   // 64 chars
            @RequestParam long originalSizeBytes,
            @RequestParam(required = false) Long gzipSizeBytes,
            @RequestParam(required = false) String mimeType,
            @RequestParam(required = false) String filename
    ) throws Exception {

        final String enc = contentEncoding.trim().toLowerCase();
        if (!enc.equals("gzip") && !enc.equals("identity")) {
            return ResponseEntity.badRequest().build();
        }
        if (hashSha256Hex == null || hashSha256Hex.length() != 64) {
            return ResponseEntity.badRequest().build();
        }

        final byte[] bytes = file.getBytes(); // exatamente o binário persistido
        final String fname = (filename != null && !filename.isBlank())
                ? filename
                : (file.getOriginalFilename() != null ? file.getOriginalFilename() : "file.bin");
        final String mt = (mimeType != null && !mimeType.isBlank())
                ? mimeType
                : (file.getContentType() != null ? file.getContentType() : MediaType.APPLICATION_OCTET_STREAM_VALUE);

        Long id = svc.saveUploaded(fname, mt, enc, hashSha256Hex, originalSizeBytes, gzipSizeBytes, bytes);
        return ResponseEntity.ok(new FileSavedResponse(id, hashSha256Hex));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        // Service já lança 404 se não existir
        svc.deleteById(id);
        return ResponseEntity.noContent().build(); // 204
    }

    /** Snapshot com metadados e Base64 do payload (opcional) */
    @GetMapping("/{id}")
    public ResponseEntity<SnapshotResponse> snapshot(
            @PathVariable Long id,
            @RequestParam(defaultValue = "true") boolean includeBase64
    ) {
        FileEntity fe = svc.getByIdOrThrow(id);
        String base64 = includeBase64 ? svc.toBase64(fe.getPayload()) : null;

        var resp = new SnapshotResponse(
                fe.getId(),
                fe.getFilename(),
                fe.getMimeType(),
                fe.getContentEncoding(),
                fe.getGzipSizeBytes(),
                fe.getOriginalSizeBytes(),
                fe.getSha256Hex(),
                base64
        );
        return ResponseEntity.ok(resp);
    }

    /** Download do ARQUIVO ORIGINAL (descompactado se content_encoding='gzip') */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        FileEntity fe = svc.getByIdOrThrow(id);
        byte[] original = Objects.equals("gzip", fe.getContentEncoding())
                ? svc.gunzip(fe.getPayload())
                : fe.getPayload();

        String fname = fe.getFilename();
        MediaType mt = safeMediaType(fe.getMimeType());
        return ResponseEntity.ok()
                .contentType(mt)
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(fname))
                .body(original);
    }

    /** Download do payload em GZIP (se identity, compacta on-the-fly) */
    @GetMapping("/{id}/download-gzip")
    public ResponseEntity<byte[]> downloadGzip(@PathVariable Long id) {
        FileEntity fe = svc.getByIdOrThrow(id);
        byte[] gz = Objects.equals("gzip", fe.getContentEncoding())
                ? fe.getPayload()
                : svc.gzip(fe.getPayload());

        String fname = fe.getFilename() + ".gz";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/gzip"))
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(fname))
                .body(gz);
    }

    // GET com query string curta: /api/files/batch/snapshots?ids=1,2,3&includeBase64=false
    @GetMapping("/batch/snapshots")
    public ResponseEntity<List<SnapshotResponse>> batchSnapshotsGet(
            @RequestParam List<Long> ids,
            @RequestParam(defaultValue = "false") boolean includeBase64
    ) {
        if (ids == null || ids.isEmpty()) return ResponseEntity.badRequest().build();
        return ResponseEntity.ok(buildSnapshots(ids, includeBase64));
    }

    // POST com JSON para listas longas: { "ids": [...], "includeBase64": false }
    @PostMapping(path = "/batch/snapshots", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<SnapshotResponse>> batchSnapshotsPost(@RequestBody IdsRequest req) {
        if (req == null || req.ids() == null || req.ids().isEmpty()) return ResponseEntity.badRequest().build();
        boolean includeBase64 = Boolean.TRUE.equals(req.includeBase64());
        return ResponseEntity.ok(buildSnapshots(req.ids(), includeBase64));
    }

    // ------- helper interno do controller: preserva a ordem dos IDs -------
    private List<SnapshotResponse> buildSnapshots(List<Long> ids, boolean includeBase64) {
        var list = svc.findAllByIds(ids);
        var byId = new java.util.HashMap<Long, FileEntity>(list.size());
        for (var fe : list) byId.put(fe.getId(), fe);

        var out = new java.util.ArrayList<SnapshotResponse>(ids.size());
        for (Long id : ids) {
            FileEntity fe = byId.get(id);
            if (fe == null) continue; // ignora IDs inexistentes (ou crie placeholder se preferir)
            String base64 = includeBase64 ? svc.toBase64(fe.getPayload()) : null;
            out.add(new SnapshotResponse(
                    fe.getId(),
                    fe.getFilename(),
                    fe.getMimeType(),
                    fe.getContentEncoding(),
                    fe.getGzipSizeBytes(),
                    fe.getOriginalSizeBytes(),
                    fe.getSha256Hex(),
                    base64
            ));
        }
        return out;
    }

    // ---- helpers HTTP ----
    private static MediaType safeMediaType(String mt) {
        if (mt == null || mt.isBlank()) return MediaType.APPLICATION_OCTET_STREAM;
        try { return MediaType.parseMediaType(mt); }
        catch (Exception e) { return MediaType.APPLICATION_OCTET_STREAM; }
    }

    private static String contentDisposition(String filename) {
        // RFC 5987 para lidar com nomes UTF-8
        String encoded = java.net.URLEncoder.encode(filename, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename*=UTF-8''" + encoded;
    }
}
