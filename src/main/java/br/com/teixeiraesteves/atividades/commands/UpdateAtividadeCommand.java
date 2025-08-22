package br.com.teixeiraesteves.atividades.commands;

import java.util.Map;

public record UpdateAtividadeCommand(Long id,
                                     String name,
                                     String url,
                                     String categoria,
                                     String subCategoria,
                                     String descricao,
                                     Map<String, Object> tag
) implements AtividadeCommand {}