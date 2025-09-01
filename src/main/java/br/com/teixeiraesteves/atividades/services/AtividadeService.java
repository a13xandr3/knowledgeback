package br.com.teixeiraesteves.atividades.services;

import br.com.teixeiraesteves.atividades.commands.AtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.CreateAtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.DeleteAtividadeCommand;
import br.com.teixeiraesteves.atividades.commands.UpdateAtividadeCommand;
import br.com.teixeiraesteves.atividades.entities.Atividade;
import br.com.teixeiraesteves.atividades.repositories.AtividadeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executors;

@Service
public class AtividadeService {

    @Autowired
    private AtividadeRepository atividadeRepository;

    public List<Atividade> getAll() {
        return atividadeRepository.findAll();
    }

    public Atividade getById(Long id) {
        return atividadeRepository.findById(id).orElseThrow();
    }

    public void processCommand(AtividadeCommand command) {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.submit(() -> {
                var now = LocalDateTime.now();
                switch (command) {
                    case CreateAtividadeCommand c -> {
                        Atividade atividade = new Atividade();
                        atividade.setName(c.name());
                        atividade.setUrl(c.url());
                        atividade.setUri(c.uri());
                        atividade.setCategoria(c.categoria());
                        atividade.setSubCategoria(c.subCategoria());
                        atividade.setDescricao(c.descricao());
                        atividade.setTag(c.tag());
                        atividade.setDataEntradaManha(c.dataEntradaManha());
                        atividade.setDataSaidaManha(c.dataSaidaManha());
                        atividade.setDataEntradaTarde(c.dataEntradaTarde());
                        atividade.setDataSaidaTarde(c.dataSaidaTarde());
                        atividade.setDataEntradaNoite(c.dataEntradaNoite());
                        atividade.setDataSaidaNoite(c.dataSaidaNoite());
                        atividade.setCreatedAt(now);
                        atividadeRepository.save(atividade);
                        logAsJson("link criado", c.name(), c.url());
                    }
                    case UpdateAtividadeCommand u -> {
                        Atividade exist = atividadeRepository.findById(u.id()).orElseThrow();
                        exist.setId(u.id());
                        exist.setName(u.name());
                        exist.setUrl(u.url());
                        exist.setUri(u.uri());
                        exist.setCategoria(u.categoria());
                        exist.setSubCategoria(u.subCategoria());
                        exist.setDescricao(u.descricao());
                        exist.setTag(u.tag());
                        exist.setDataEntradaManha(u.dataEntradaManha());
                        exist.setDataSaidaManha(u.dataSaidaManha());
                        exist.setDataEntradaTarde(u.dataEntradaTarde());
                        exist.setDataSaidaTarde(u.dataSaidaTarde());
                        exist.setDataEntradaNoite(u.dataEntradaNoite());
                        exist.setDataSaidaNoite(u.dataSaidaNoite());
                        exist.setUpdatedAt(now);
                        atividadeRepository.save(exist);
                        logAsJson("Link atualizado", u.name(), u.url());
                    }
                    case DeleteAtividadeCommand d -> {
                        atividadeRepository.deleteById(d.id());
                        logAsJson("Link deletado", "ID: " + d.id(), "");
                    }
                    default -> throw new IllegalArgumentException("Comando desconhecido");
                }
            });
        }
    }

    private void logAsJson(String status, String campo1, String campo2) {
        String json = """
                {
                    "status": "%s",
                    "campo1": "%s",
                    "campo2": "%s"
                }
                """.formatted(status, campo1, campo2);
        System.out.println(json);
    }

}
