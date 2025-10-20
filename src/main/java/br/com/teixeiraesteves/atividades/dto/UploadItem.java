package br.com.teixeiraesteves.atividades.dto;

import jakarta.validation.constraints.*;
import br.com.teixeiraesteves.atividades.entities.HashMode;

public record UploadItem(
        @NotBlank String filename,
        @NotBlank String mimeType,
        @Positive long sizeBytes,
        @Positive long gzipSizeBytes,
        @NotBlank @Size(min=64, max=64) String hashSha256Hex,
        @NotNull HashMode hashMode
) {}