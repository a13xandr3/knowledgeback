package br.com.teixeiraesteves.atividades.commands;

public sealed interface AtividadeCommand permits
        CreateAtividadeCommand,
        DeleteAtividadeCommand,
        UpdateAtividadeCommand {}