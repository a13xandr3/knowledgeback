package br.com.teixeiraesteves.atividades.controllers;

import br.com.teixeiraesteves.atividades.commands.CreateAtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.DeleteAtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.UpdateAtividadeCommand;
import br.com.teixeiraesteves.atividades.dto.AtividadeDTO;
import br.com.teixeiraesteves.atividades.entities.Atividade;
import br.com.teixeiraesteves.atividades.records.AtividadeRecord;
import br.com.teixeiraesteves.atividades.services.AtividadeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

// imports necessários
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;
import java.util.stream.Collectors;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path="/api/atividade")
public class AtividadeController {

    @Autowired
    public AtividadeService atividadeService;

    private static final ObjectMapper JSON = new ObjectMapper();

    @GetMapping
    public ResponseEntity<?> getAllLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "", required = false) List<String> excessao,
            @RequestParam(defaultValue = "", required = false) List<String> categoria,
            @RequestParam(defaultValue = "", required = false) List<String> tag
    ) {
        // ----- normalização de categoria (como já fazia)
        categoria = (categoria.size() == 1 && "todos".equalsIgnoreCase(categoria.get(0)))
                ? List.of()
                : categoria;

        var categoriaFiltrada = categoria.isEmpty()
                ? List.<String>of()
                : categoria.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        // ----- NOVO: normalização de tag (querystring)
        var tagsFiltradas = normalizeTagParam(tag); // tudo lower-case, sem vazios, sem "todos"

        var filteredLinks = atividadeService.getAll().stream()
                .map(atividade -> new AtividadeRecord(
                        atividade.getId(),
                        atividade.getName(),
                        atividade.getUrl(),
                        atividade.getUri(),
                        atividade.getCategoria(),
                        atividade.getSubCategoria(),
                        atividade.getDescricao(),
                        atividade.getTag(),
                        atividade.getDataEntradaManha(),
                        atividade.getDataSaidaManha(),
                        atividade.getDataEntradaTarde(),
                        atividade.getDataSaidaTarde(),
                        atividade.getDataEntradaNoite(),
                        atividade.getDataSaidaNoite()
                ))
                .filter(atividade -> excessao.isEmpty() || !excessao.contains(atividade.categoria().trim()))
                .filter(atividade -> categoriaFiltrada.isEmpty() || categoriaFiltrada.contains(atividade.categoria().trim()))
                // ----- NOVO: filtro por tag
                .filter(atividade -> tagsFiltradas.isEmpty() || anyTagMatches(atividade.tag(), tagsFiltradas))
                .toList();

        int total = filteredLinks.size();
        int fromIndex = Math.min(page * limit, total);
        int toIndex = Math.min(fromIndex + limit, total);
        var pagedLinks = filteredLinks.subList(fromIndex, toIndex);

        var response = Map.of(
                "atividades", pagedLinks,
                "total", total
        );

        return ResponseEntity.ok(response);
    }

// --------- Helpers ---------

    /**
     * Normaliza o parâmetro 'tag' recebido via querystring.
     * Remove vazios, trim, opcionalmente ignora "todos", deixa em lower-case e remove duplicatas.
     */
    private List<String> normalizeTagParam(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(s -> !"todos".equalsIgnoreCase(s))
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    /**
     * Retorna true se existir interseção entre as tags da atividade e as tags filtradas da querystring.
     */
    private boolean anyTagMatches(Object tagField, List<String> tagsFiltradasLower) {
        if (tagsFiltradasLower.isEmpty()) return true; // sem filtro
        var atvTagsLower = extractTags(tagField).stream()
                .map(String::toLowerCase)
                .collect(Collectors.toSet());
        // interseção rápida
        for (String q : tagsFiltradasLower) {
            if (atvTagsLower.contains(q)) return true;
        }
        return false;
    }

    /**
     * Extrai lista de strings do campo 'tag' que pode vir em vários formatos:
     * - Map -> { "tags": [...] }
     * - String JSON -> "{\"tags\":[...]}"
     * - POJO com getTags()
     * - Qualquer outra coisa retorna lista vazia
     */
    @SuppressWarnings("unchecked")
    private List<String> extractTags(Object tagField) {
        if (tagField == null) return List.of();

        // Caso 1: Map (ex.: vindo de JSONB/Mongo/Hibernate com conversor)
        if (tagField instanceof Map<?, ?> map) {
            Object raw = map.get("tags");
            if (raw instanceof List<?> list) {
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList();
            }
        }

        // Caso 2: String JSON
        if (tagField instanceof String s) {
            try {
                JsonNode node = JSON.readTree(s);
                JsonNode arr = node.get("tags");
                if (arr != null && arr.isArray()) {
                    List<String> out = new ArrayList<>();
                    arr.forEach(n -> {
                        if (n.isTextual()) out.add(n.asText());
                        else out.add(n.toString());
                    });
                    return out.stream()
                            .map(String::trim)
                            .filter(t -> !t.isBlank())
                            .toList();
                }
            } catch (Exception ignored) {
                // não é JSON ou formato inesperado
            }
        }

        // Caso 3: POJO com getTags()
        try {
            var m = tagField.getClass().getMethod("getTags");
            Object raw = m.invoke(tagField);
            if (raw instanceof List<?> list) {
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::trim)
                        .filter(s -> !s.isBlank())
                        .toList();
            }
        } catch (Exception ignored) { /* sem getTags ou erro de reflexão */ }

        return List.of();
    }

    // ... demais endpoints inalterados ...

    @GetMapping("/categorias")
    public ResponseEntity<?> getCategorias() {
        var filteredLinks = atividadeService.getAll().stream()
                .map(atividade -> new AtividadeRecord(
                        atividade.getId(),
                        atividade.getName(),
                        atividade.getUrl(),
                        atividade.getUri(),
                        atividade.getCategoria(),
                        atividade.getSubCategoria(),
                        atividade.getDescricao(),
                        atividade.getTag(),
                        atividade.getDataEntradaManha(),
                        atividade.getDataSaidaManha(),
                        atividade.getDataEntradaTarde(),
                        atividade.getDataSaidaTarde(),
                        atividade.getDataEntradaNoite(),
                        atividade.getDataSaidaNoite()
                ))
                .toList();
        return ResponseEntity.ok(filteredLinks);
    }

    @GetMapping("/tags")
    public ResponseEntity<?> getTags() {
        var filteredLinks = atividadeService.getAll().stream()
                .map(atividade -> new AtividadeRecord(
                    atividade.getId(),
                    atividade.getName(),
                    atividade.getUrl(),
                    atividade.getUri(),
                    atividade.getCategoria(),
                    atividade.getSubCategoria(),
                    atividade.getDescricao(),
                    atividade.getTag(),
                    atividade.getDataEntradaManha(),
                    atividade.getDataSaidaManha(),
                    atividade.getDataEntradaTarde(),
                    atividade.getDataSaidaTarde(),
                    atividade.getDataEntradaNoite(),
                    atividade.getDataSaidaNoite()
                ))
                .toList();
        return ResponseEntity.ok(filteredLinks);
    }
    @GetMapping("/{id}")
    public ResponseEntity<Atividade> getLink(@PathVariable Long id) {
        return ResponseEntity.ok(atividadeService.getById(id));
    }

    @PostMapping
    public ResponseEntity<Void> create(@RequestBody CreateAtividadeCommand command) {
        atividadeService.processCommand(command);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<Void> update(@PathVariable Long id, @RequestBody UpdateAtividadeCommand body) {
        UpdateAtividadeCommand command = new UpdateAtividadeCommand(id,
                body.name(),
                body.url(),
                body.uri(),
                body.categoria(),
                body.subCategoria(),
                body.descricao(),
                body.tag(),
                body.dataEntradaManha(),
                body.dataSaidaManha(),
                body.dataEntradaTarde(),
                body.dataSaidaTarde(),
                body.dataEntradaNoite(),
                body.dataSaidaNoite()
        );
        atividadeService.processCommand(command);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        atividadeService.processCommand(new DeleteAtividadeCommand(id));
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    AtividadeDTO toLinkDTO(Atividade atividade) {
        AtividadeDTO dto = new AtividadeDTO();
        dto.setName(atividade.getName());
        dto.setUrl(atividade.getUrl());
        dto.setUri(atividade.getUri());
        dto.setCategoria(atividade.getCategoria());
        dto.setSubCategoria(atividade.getSubCategoria());
        dto.setDescricao(atividade.getDescricao());
        dto.setTag(atividade.getTag());
        dto.setDataEntradaManha(atividade.getDataEntradaManha());
        dto.setDataSaidaManha(atividade.getDataSaidaManha());
        dto.setDataEntradaTarde(atividade.getDataEntradaTarde());
        dto.setDataSaidaTarde(atividade.getDataSaidaTarde());
        dto.setDataEntradaNoite(atividade.getDataEntradaNoite());
        dto.setDataSaidaNoite(atividade.getDataSaidaNoite());
        return dto;
    }

}
