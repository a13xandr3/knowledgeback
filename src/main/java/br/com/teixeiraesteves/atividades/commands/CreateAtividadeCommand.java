package br.com.teixeiraesteves.atividades.commands;

import java.util.Map;

public record CreateAtividadeCommand(String name,
                                     String url,
                                     String categoria,
                                     String subCategoria,
                                     String descricao,
                                     Map<String, Object> tag
) implements AtividadeCommand {}