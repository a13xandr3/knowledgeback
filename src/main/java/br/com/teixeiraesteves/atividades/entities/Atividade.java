package br.com.teixeiraesteves.atividades.entities;

import br.com.teixeiraesteves.atividades.persistence.JsonMapConverter;
import jakarta.persistence.*;
import lombok.Data;

import java.util.Map;

@Data
@Entity
@Table(name="link")
public class Atividade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "TEXT")
    private String url;

    @Column(columnDefinition = "TEXT")
    private String categoria;

    @Column(columnDefinition = "TEXT")
    private String subCategoria;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "json")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> tag;

}
