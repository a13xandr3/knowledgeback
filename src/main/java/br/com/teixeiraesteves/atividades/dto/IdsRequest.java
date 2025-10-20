package br.com.teixeiraesteves.atividades.dto;

import java.util.List;

public record IdsRequest(
        List<Long> ids,
        Boolean includeBase64 // opcional; default=false
) {}
