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

    public void setFileID(Map<String, Object> uri) {
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

    private String name;
    private Map<String, Object> uri;
    private String categoria;
    private String subCategoria;
    private String descricao;
    private Map<String, Object> tag;
    private Map<String, Object> fileID;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dataEntradaManha;
    private LocalDateTime dataSaidaManha;
    private LocalDateTime dataEntradaTarde;
    private LocalDateTime dataSaidaTarde;
    private LocalDateTime dataEntradaNoite;
    private LocalDateTime dataSaidaNoite;
}
