package br.com.teixeiraesteves.atividades.controllers;

import br.com.teixeiraesteves.atividades.commands.CreateAtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.DeleteAtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.UpdateAtividadeCommand;
import br.com.teixeiraesteves.atividades.dto.AtividadeDTO;
import br.com.teixeiraesteves.atividades.entities.Atividade;
import br.com.teixeiraesteves.atividades.records.AtividadeRecord;
import br.com.teixeiraesteves.atividades.services.AtividadeService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import static org.apache.coyote.http11.Constants.a;

@RestController
@RequestMapping(path = "/api/atividade")
public class AtividadeController {

    private static final Logger log = LoggerFactory.getLogger(AtividadeController.class);

    private final AtividadeService atividadeService;
    private final ObjectMapper objectMapper; // bean singleton do Spring (thread-safe após configuração)

    public AtividadeController(AtividadeService atividadeService, ObjectMapper objectMapper) {
        this.atividadeService = Objects.requireNonNull(atividadeService);
        this.objectMapper = Objects.requireNonNull(objectMapper);
    }

    /* ============================================================
       GET /api/atividade  (listar + filtros + paginação manual)
       ============================================================ */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllLinks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(name = "excessao",      required = false) List<String> excessao,
            @RequestParam(name = "categoria",     required = false) List<String> categoria,
            @RequestParam(name = "tag",           required = false) List<String> tag,
            @RequestParam(name = "categoriaTerm", required = false) String categoriaTerm
    ) {
        // Normalizações de entrada
        final var excNorm             = toNormalizedSet(excessao);             // categorias a excluir (normalizadas)
        final var categoriasRaw       = cleanList(categoria);                  // categorias de filtro (texto original)
        final var categoriasFiltroSet = categoriasToNormalizedSet(categoriasRaw);
        final var tagsFiltroSet       = normalizeTagParam(tag).stream()
                .map(AtividadeController::normalize).collect(Collectors.toUnmodifiableSet());
        final var term                = normalize(categoriaTerm);              // termo de busca (cat/sub)

        log.debug("GET /api/atividade page={} limit={} excNorm={} catFiltro={} tagsFiltro={} term={}",
                page, limit, excNorm, categoriasFiltroSet, tagsFiltroSet, term);

        // 1) Filtra ANTES de mapear
        var filtrado = atividadeService.getAll().stream()
                // excluir por categoria (lista de exceções)
                .filter(a -> excNorm.isEmpty() || !excNorm.contains(normalize(a.getCategoria())))
                // incluir por categoria (se informado)
                .filter(a -> categoriasFiltroSet.isEmpty() ||
                        categoriasFiltroSet.contains(normalize(a.getCategoria())))
                // incluir por tags (se informado) — requer interseção não-vazia
                .filter(a -> {
                    if (tagsFiltroSet.isEmpty()) return true;
                    var atvTags = extractTags(a.getTag(), objectMapper).stream()
                            .map(AtividadeController::normalize)
                            .collect(Collectors.toUnmodifiableSet());
                    return !Collections.disjoint(atvTags, tagsFiltroSet);
                })
                // termo em categoria/subCategoria
                .filter(a -> term.isBlank()
                        || normalize(a.getCategoria()).contains(term)
                        || normalize(a.getSubCategoria()).contains(term))
                .map(this::toRecord)
                .toList();

        // Paginação manual (mantém compatibilidade com o service atual)
        final int safePage  = Math.max(0, page);
        final int safeLimit = Math.max(1, limit);
        int total = filtrado.size();
        int from  = Math.min(safePage * safeLimit, total);
        int to    = Math.min(from + safeLimit, total);
        var pageContent = filtrado.subList(from, to);

        var body = Map.<String, Object>of(
                "atividades", pageContent,
                "total", total,
                "page", safePage,
                "limit", safeLimit
        );
        return ResponseEntity.ok(body);
    }

    /* ============================================================
       GET /api/atividade/categorias  (exclui por categoria)
       ============================================================ */
    @GetMapping("/categorias")
    public ResponseEntity<List<AtividadeRecord>> getCategorias(
            @RequestParam(name = "excessao", required = false) List<String> excessao
    ) {
        final var excNorm = toNormalizedSet(excessao);

        var filtered = atividadeService.getAll().stream()
                .filter(a -> excNorm.isEmpty() || !excNorm.contains(normalize(a.getCategoria())))
                .map(this::toRecord)
                .toList();

        return ResponseEntity.ok(filtered);
    }

    /* ============================================================
       GET /api/atividade/tags  (exclui por tags)
       - Exclui a atividade se QUALQUER tag ∈ excessao (normalizado)
       ============================================================ */
    @GetMapping("/tags")
    public ResponseEntity<List<AtividadeRecord>> getTags(
            @RequestParam(name = "excessao", required = false) List<String> excessao
    ) {
        final var excNorm = toNormalizedSet(excessao);

        var filtered = atividadeService.getAll().stream()
                .filter(a -> {
                    if (excNorm.isEmpty()) return true;
                    var atvTags = extractTags(a.getTag(), objectMapper).stream()
                            .map(AtividadeController::normalize)
                            .collect(Collectors.toUnmodifiableSet());
                    // mantém só quem NÃO intersecta com a lista de exclusão
                    return Collections.disjoint(atvTags, excNorm);
                })
                .map(this::toRecord)
                .toList();

        return ResponseEntity.ok(filtered);
    }

    /* ============================================================
       CRUD
       ============================================================ */
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
        var command = new UpdateAtividadeCommand(
                id,
                body.name(),
                body.uri(),
                body.categoria(),
                body.subCategoria(),
                body.descricao(),
                body.tag(),
                body.fileID(),
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

    /* ============================================================
       Mapeadores / Helpers
       ============================================================ */

    private AtividadeRecord toRecord(Atividade a) {
        return new AtividadeRecord(
                a.getId(),
                a.getName(),
                a.getUri(),
                a.getCategoria(),
                a.getSubCategoria(),
                a.getDescricao(),
                a.getTag(),
                a.getFileID(),
                a.getDataEntradaManha(),
                a.getDataSaidaManha(),
                a.getDataEntradaTarde(),
                a.getDataSaidaTarde(),
                a.getDataEntradaNoite(),
                a.getDataSaidaNoite()
        );
    }

    private static boolean hasText(String s) { return s != null && !s.strip().isEmpty(); }

    private static String safeTrim(String s) { return s == null ? "" : s.strip(); }

    private static List<String> cleanList(List<String> in) {
        if (CollectionUtils.isEmpty(in)) return List.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(Predicate.not(String::isBlank))
                .toList(); // imutável
    }

    /** normaliza: strip + lower + remove acentos; null -> "" */
    private static String normalize(String s) {
        if (s == null) return "";
        String noAccents = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return noAccents.strip().toLowerCase(Locale.ROOT);
    }

    private static Set<String> toNormalizedSet(List<String> items) {
        if (items == null) return Set.of();
        return items.stream()
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(Predicate.not(String::isBlank))
                .map(AtividadeController::normalize)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static Set<String> categoriasToNormalizedSet(List<String> categoriasRaw) {
        if (categoriasRaw == null || categoriasRaw.isEmpty()) return Set.of();
        // se vier ["todos"], zera o filtro
        if (categoriasRaw.size() == 1 && "todos".equalsIgnoreCase(categoriasRaw.get(0))) return Set.of();
        return categoriasRaw.stream()
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(Predicate.not(String::isBlank))
                .map(AtividadeController::normalize)
                .collect(Collectors.toUnmodifiableSet());
    }

    /** normaliza parâmetro 'tag' (querystring) -> strip, lower, sem vazios, sem "todos", sem duplicatas  */
    private static List<String> normalizeTagParam(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(Predicate.not(String::isBlank))
                .filter(s -> !"todos".equalsIgnoreCase(s))
                .map(String::toLowerCase)
                .distinct()
                .toList();
    }

    /**
     * Extrai lista de tags do campo 'tag' da entidade (coluna JSON no MySQL).
     * Aceita:
     *  - Collection<?>             -> lista direta
     *  - Map<?,?> com chave "tags" -> {"tags":[...]}
     *  - String JSON (objeto/array) ou CSV
     */
    @SuppressWarnings("unchecked")
    private static List<String> extractTags(Object raw, ObjectMapper mapper) {
        if (raw == null) return List.of();

        if (raw instanceof Collection<?> col) {
            return col.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .map(String::strip)
                    .filter(Predicate.not(String::isBlank))
                    .toList();
        }
        if (raw instanceof Map<?, ?> map) {
            Object t = map.get("tags");
            if (t instanceof Collection<?> list) {
                return list.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString)
                        .map(String::strip)
                        .filter(Predicate.not(String::isBlank))
                        .toList();
            }
        }

        var s = raw.toString().strip();
        if (s.isEmpty()) return List.of();

        try {
            if (s.startsWith("[")) {
                // JSON array (itens podem não ser String)
                var type = mapper.getTypeFactory().constructCollectionType(List.class, Object.class);
                List<?> arr = mapper.readValue(s, type);
                return arr.stream()
                        .filter(Objects::nonNull)
                        .map(Object::toString) // garante String
                        .map(String::strip)
                        .filter(t -> !t.isBlank())
                        .toList();
            }
            if (s.startsWith("{")) {
                JsonNode node = mapper.readTree(s).get("tags");
                if (node != null && node.isArray()) {
                    List<String> out = new ArrayList<>();
                    node.forEach(n -> out.add(n.isTextual() ? n.asText() : n.toString()));
                    return out.stream()
                            .filter(Objects::nonNull)
                            .map(String::strip)
                            .filter(Predicate.not(String::isBlank))
                            .toList();
                }
            }
        } catch (Exception ignore) {
            // cai para CSV
        }
        // CSV
        return Arrays.stream(s.split(","))
                .filter(Objects::nonNull)
                .map(String::strip)
                .filter(Predicate.not(String::isBlank))
                .toList();
    }

    /* Mapper auxiliar (se necessário em algum outro lugar) */
    AtividadeDTO toLinkDTO(Atividade a) {
        var dto = new AtividadeDTO();
        dto.setName(a.getName());
        dto.setUri(a.getUri());
        dto.setCategoria(a.getCategoria());
        dto.setSubCategoria(a.getSubCategoria());
        dto.setDescricao(a.getDescricao());
        dto.setTag(a.getTag());
        dto.setFileID(a.getFileID());
        dto.setDataEntradaManha(a.getDataEntradaManha());
        dto.setDataSaidaManha(a.getDataSaidaManha());
        dto.setDataEntradaTarde(a.getDataEntradaTarde());
        dto.setDataSaidaTarde(a.getDataSaidaTarde());
        dto.setDataEntradaNoite(a.getDataEntradaNoite());
        dto.setDataSaidaNoite(a.getDataSaidaNoite());
        return dto;
    }
}
