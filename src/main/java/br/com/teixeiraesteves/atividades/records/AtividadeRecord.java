package br.com.teixeiraesteves.atividades.records;

import java.util.Map;

public record AtividadeRecord(Long id,
                              String name,
                              String url,
                              String categoria,
                              String subCategoria,
                              String descricao,
                              Map<String, Object> tag
                              ) {}