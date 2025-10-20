package br.com.teixeiraesteves.atividades.records;

import java.time.LocalDateTime;
import java.util.Map;

public record AtividadeRecord(Long id,
                              String name,
                              Map<String, Object> uri,
                              String categoria,
                              String subCategoria,
                              String descricao,
                              Map<String, Object> tag,
                              Map<String, Object> fileID,
                              LocalDateTime dataEntradaManha,
                              LocalDateTime dataSaidaManha,
                              LocalDateTime dataEntradaTarde,
                              LocalDateTime dataSaidaTarde,
                              LocalDateTime dataEntradaNoite,
                              LocalDateTime dataSaidaNoite
                              ) {}