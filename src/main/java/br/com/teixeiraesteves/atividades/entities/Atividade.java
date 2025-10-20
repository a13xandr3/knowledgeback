package br.com.teixeiraesteves.atividades.entities;

import br.com.teixeiraesteves.atividades.persistence.JsonMapConverter;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.GenerationTime;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Entity
@Table(name="link")
@EntityListeners(AuditingEntityListener.class)
public class Atividade {

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getUri() {
        return uri;
    }

    public void setUri(Map<String, Object> uri) {
        this.uri = uri;
    }

    public Map<String, Object> getFileID() {
        return fileID;
    }

    public void setFileID(Map<String, Object> fileID) {
        this.fileID = fileID;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getSubCategoria() {
        return subCategoria;
    }

    public void setSubCategoria(String subCategoria) {
        this.subCategoria = subCategoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Map<String, Object> getTag() {
        return tag;
    }

    public void setTag(Map<String, Object> tag) {
        this.tag = tag;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getDataEntradaManha() {
        return dataEntradaManha;
    }

    public void setDataEntradaManha(LocalDateTime dataEntradaManha) {
        this.dataEntradaManha = dataEntradaManha;
    }

    public LocalDateTime getDataSaidaManha() {
        return dataSaidaManha;
    }

    public void setDataSaidaManha(LocalDateTime dataSaidaManha) {
        this.dataSaidaManha = dataSaidaManha;
    }

    public LocalDateTime getDataEntradaTarde() {
        return dataEntradaTarde;
    }

    public void setDataEntradaTarde(LocalDateTime dataEntradaTarde) {
        this.dataEntradaTarde = dataEntradaTarde;
    }

    public LocalDateTime getDataSaidaTarde() {
        return dataSaidaTarde;
    }

    public void setDataSaidaTarde(LocalDateTime dataSaidaTarde) {
        this.dataSaidaTarde = dataSaidaTarde;
    }

    public LocalDateTime getDataEntradaNoite() {
        return dataEntradaNoite;
    }

    public void setDataEntradaNoite(LocalDateTime dataEntradaNoite) {
        this.dataEntradaNoite = dataEntradaNoite;
    }

    public LocalDateTime getDataSaidaNoite() {
        return dataSaidaNoite;
    }

    public void setDataSaidaNoite(LocalDateTime dataSaidaNoite) {
        this.dataSaidaNoite = dataSaidaNoite;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(columnDefinition = "TEXT")
    private String name;

    @Column(columnDefinition = "json")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> uri;

    @Column(columnDefinition = "TEXT")
    private String categoria;

    @Column(columnDefinition = "TEXT")
    private String subCategoria;

    @Column(columnDefinition = "TEXT")
    private String descricao;

    @Column(columnDefinition = "json")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> tag;

    @Column(columnDefinition = "json")
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Object> fileID;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime createdAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime updatedAt;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataEntradaManha;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataSaidaManha;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataEntradaTarde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataSaidaTarde;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataEntradaNoite;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataSaidaNoite;

}
