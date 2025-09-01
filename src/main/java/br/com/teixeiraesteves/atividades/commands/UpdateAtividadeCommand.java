package br.com.teixeiraesteves.atividades.commands;

import java.time.LocalDateTime;
import java.util.Map;

public record UpdateAtividadeCommand(Long id,
                                     String name,
                                     String url,
                                     Map<String, Object> uri,
                                     String categoria,
                                     String subCategoria,
                                     String descricao,
                                     Map<String, Object> tag,
                                     LocalDateTime dataEntradaManha,
                                     LocalDateTime dataSaidaManha,
                                     LocalDateTime dataEntradaTarde,
                                     LocalDateTime dataSaidaTarde,
                                     LocalDateTime dataEntradaNoite,
                                     LocalDateTime dataSaidaNoite
) implements AtividadeCommand {}