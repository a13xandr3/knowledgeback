package br.com.teixeiraesteves.atividades.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeDTO {
    private String name;
    private String url;
    private Map<String, Object> uri;
    private String categoria;
    private String subCategoria;
    private String descricao;
    private Map<String, Object> tag;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dataEntradaManha;
    private LocalDateTime dataSaidaManha;
    private LocalDateTime dataEntradaTarde;
    private LocalDateTime dataSaidaTarde;
    private LocalDateTime dataEntradaNoite;
    private LocalDateTime dataSaidaNoite;
}
