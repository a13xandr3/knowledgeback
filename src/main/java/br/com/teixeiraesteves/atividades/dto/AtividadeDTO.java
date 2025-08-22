package br.com.teixeiraesteves.atividades.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AtividadeDTO {

    private String name;
    private String url;
    private String categoria;
    private String subCategoria;
    private String descricao;
    private Map<String, Object> tag;

}
