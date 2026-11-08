package br.com.athletix.auth.service;

import br.com.athletix.cargo.entity.PerfilAcesso;
import org.springframework.stereotype.Service;

import java.util.EnumSet;
import java.util.List;

@Service
public class CapacidadeService {

    public List<Capacidade> para(PerfilAcesso perfil) {
        EnumSet<Capacidade> capacidades = switch (perfil) {
            case ADMINISTRADOR -> EnumSet.allOf(Capacidade.class);
            case FINANCEIRO -> EnumSet.of(
                    Capacidade.CONSULTAR_MEMBROS, Capacidade.CONSULTAR_DEPARTAMENTOS,
                    Capacidade.ATUALIZAR_PROPRIA_TAREFA, Capacidade.CONSULTAR_RESUMOS_FINANCEIROS,
                    Capacidade.GERENCIAR_FINANCEIRO, Capacidade.CONSULTAR_DOCUMENTOS_MEMBROS,
                    Capacidade.CONSULTAR_DOCUMENTOS_DIRETORIA
            );
            case GESTOR -> EnumSet.of(
                    Capacidade.CONSULTAR_MEMBROS, Capacidade.CONSULTAR_DEPARTAMENTOS,
                    Capacidade.GERENCIAR_EVENTOS_DO_PROPRIO_ESCOPO, Capacidade.ATUALIZAR_PROPRIA_TAREFA,
                    Capacidade.CONSULTAR_RESUMOS_FINANCEIROS,
                    Capacidade.GERENCIAR_PATRIMONIO_DO_PROPRIO_DEPARTAMENTO,
                    Capacidade.CONSULTAR_DOCUMENTOS_MEMBROS, Capacidade.CONSULTAR_DOCUMENTOS_DIRETORIA
            );
            case MEMBRO -> EnumSet.of(
                    Capacidade.CONSULTAR_MEMBROS_DADOS_MINIMOS, Capacidade.ATUALIZAR_PROPRIA_TAREFA,
                    Capacidade.CONSULTAR_RESUMOS_FINANCEIROS, Capacidade.CONSULTAR_DOCUMENTOS_MEMBROS
            );
        };
        return capacidades.stream().sorted().toList();
    }
}
