package br.com.athletix.gestao.transicao.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.atletica.repository.AtleticaRepository;
import br.com.athletix.auditoria.service.AtividadeUsuarioService;
import br.com.athletix.auth.service.InvalidacaoSessaoUsuarioService;
import br.com.athletix.cargo.entity.Cargo;
import br.com.athletix.cargo.entity.PerfilAcesso;
import br.com.athletix.cargo.repository.CargoRepository;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.departamento.repository.DepartamentoRepository;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.gestao.entity.StatusGestao;
import br.com.athletix.gestao.entity.VinculoGestao;
import br.com.athletix.gestao.repository.GestaoRepository;
import br.com.athletix.gestao.repository.VinculoGestaoRepository;
import br.com.athletix.gestao.transicao.dto.ItemTransicaoRequest;
import br.com.athletix.gestao.transicao.dto.TransicaoUpdateRequest;
import br.com.athletix.gestao.transicao.entity.ItemTransicaoGestao;
import br.com.athletix.gestao.transicao.entity.StatusTransicaoGestao;
import br.com.athletix.gestao.transicao.entity.TransicaoGestao;
import br.com.athletix.gestao.transicao.repository.ItemTransicaoGestaoRepository;
import br.com.athletix.gestao.transicao.repository.TransicaoGestaoRepository;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.tenancy.AtleticaResourceResolver;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TransicaoGestaoServiceTest {

  private static final Long ATLETICA_ID = 1L;
  private static final Long TRANSICAO_ID = 10L;
  private static final Long MEMBRO_ID = 20L;
  private static final Long CARGO_ID = 30L;
  private static final Long DEPARTAMENTO_ID = 40L;

  @Test
  void atualizaItemExistenteMantendoAsValidacoesDaTransicao() {
    Dependencias dependencias = new Dependencias();
    TransicaoGestao transicao = transicaoRascunho();
    ItemTransicaoGestao itemTransicao = mock(ItemTransicaoGestao.class);
    Membro membro = membro();
    Cargo cargo = cargoAdministrador();
    Departamento departamento = departamento();
    when(dependencias.resolver.atleticaIdAtual()).thenReturn(ATLETICA_ID);
    when(dependencias.transicoes.findByIdAndAtletica_IdForUpdate(TRANSICAO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(transicao));
    when(dependencias.membros.findByIdAndAtletica_Id(MEMBRO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(membro));
    when(dependencias.cargos.findByIdAndAtletica_Id(CARGO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(cargo));
    when(dependencias.departamentos.findByIdAndAtletica_Id(DEPARTAMENTO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(departamento));
    when(dependencias.itens.findByTransicao_IdAndMembroId(TRANSICAO_ID, MEMBRO_ID))
        .thenReturn(Optional.of(itemTransicao));
    when(dependencias.itens.findAllByTransicao_IdOrderByMembroIdAsc(TRANSICAO_ID))
        .thenReturn(List.of(itemTransicao));

    dependencias.service.atualizar(
        TRANSICAO_ID,
        new TransicaoUpdateRequest(
            1L,
            List.of(new ItemTransicaoRequest(MEMBRO_ID, CARGO_ID, DEPARTAMENTO_ID, true, null))),
        membro);

    verify(itemTransicao).atualizar(CARGO_ID, DEPARTAMENTO_ID, true, null);
    verify(dependencias.auditoria).registrar(any(), any(), any(), any());
  }

  @Test
  void concluiTransicaoComAdministradorAtivoERegistraNovosVinculos() {
    Dependencias dependencias = new Dependencias();
    TransicaoGestao transicao = transicaoRascunho();
    Gestao origem = mock(Gestao.class);
    Gestao destino = mock(Gestao.class);
    ItemTransicaoGestao itemTransicao = mock(ItemTransicaoGestao.class);
    Membro membro = membro();
    Cargo cargo = cargoAdministrador();
    Departamento departamento = departamento();
    VinculoGestao vinculoAnterior = mock(VinculoGestao.class);
    when(dependencias.resolver.atleticaIdAtual()).thenReturn(ATLETICA_ID);
    when(dependencias.atleticas.findByIdForUpdate(ATLETICA_ID))
        .thenReturn(Optional.of(mock(Atletica.class)));
    when(dependencias.transicoes.findByIdAndAtletica_IdForUpdate(TRANSICAO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(transicao));
    when(dependencias.gestoes.findByIdAndAtletica_Id(2L, ATLETICA_ID))
        .thenReturn(Optional.of(origem));
    when(dependencias.gestoes.findByIdAndAtletica_Id(3L, ATLETICA_ID))
        .thenReturn(Optional.of(destino));
    when(origem.getId()).thenReturn(2L);
    when(origem.getStatus()).thenReturn(StatusGestao.ATIVA);
    when(destino.getStatus()).thenReturn(StatusGestao.PLANEJADA);
    when(itemTransicao.isContinuaAtivo()).thenReturn(true);
    when(itemTransicao.getCargoDestinoId()).thenReturn(CARGO_ID);
    when(itemTransicao.getDepartamentoDestinoId()).thenReturn(DEPARTAMENTO_ID);
    when(itemTransicao.getMembroId()).thenReturn(MEMBRO_ID);
    when(dependencias.itens.findAllByTransicao_IdOrderByMembroIdAsc(TRANSICAO_ID))
        .thenReturn(List.of(itemTransicao));
    when(dependencias.cargos.findByIdAndAtletica_Id(CARGO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(cargo));
    when(dependencias.membros.findByIdAndAtletica_Id(MEMBRO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(membro));
    when(dependencias.departamentos.findByIdAndAtletica_Id(DEPARTAMENTO_ID, ATLETICA_ID))
        .thenReturn(Optional.of(departamento));
    when(dependencias.vinculos.findAllByGestao_IdAndFimVigenciaIsNull(2L))
        .thenReturn(List.of(vinculoAnterior));

    dependencias.service.concluir(TRANSICAO_ID, 1L, membro);

    verify(vinculoAnterior).encerrar(any());
    verify(membro).atualizarVinculo(any(), any(), any(), any());
    verify(dependencias.vinculos).save(any(VinculoGestao.class));
    verify(origem).encerrar(any());
    verify(destino).ativar();
    verify(transicao).concluir(MEMBRO_ID);
    verify(dependencias.auditoria).registrar(any(), any(), any(), any());
  }

  private TransicaoGestao transicaoRascunho() {
    TransicaoGestao transicao = mock(TransicaoGestao.class);
    when(transicao.getVersion()).thenReturn(1L);
    when(transicao.getStatus()).thenReturn(StatusTransicaoGestao.RASCUNHO);
    when(transicao.getGestaoOrigemId()).thenReturn(2L);
    when(transicao.getGestaoDestinoId()).thenReturn(3L);
    return transicao;
  }

  private Membro membro() {
    Membro membro = mock(Membro.class);
    when(membro.getId()).thenReturn(MEMBRO_ID);
    when(membro.getCargoId()).thenReturn(CARGO_ID);
    when(membro.getDepartamentoId()).thenReturn(DEPARTAMENTO_ID);
    return membro;
  }

  private Cargo cargoAdministrador() {
    Cargo cargo = mock(Cargo.class);
    when(cargo.getId()).thenReturn(CARGO_ID);
    when(cargo.getPerfilAcesso()).thenReturn(PerfilAcesso.ADMINISTRADOR);
    when(cargo.isAtivo()).thenReturn(true);
    return cargo;
  }

  private Departamento departamento() {
    Departamento departamento = mock(Departamento.class);
    when(departamento.getId()).thenReturn(DEPARTAMENTO_ID);
    return departamento;
  }

  private static final class Dependencias {
    private final TransicaoGestaoRepository transicoes = mock(TransicaoGestaoRepository.class);
    private final ItemTransicaoGestaoRepository itens = mock(ItemTransicaoGestaoRepository.class);
    private final GestaoRepository gestoes = mock(GestaoRepository.class);
    private final VinculoGestaoRepository vinculos = mock(VinculoGestaoRepository.class);
    private final MembroRepository membros = mock(MembroRepository.class);
    private final CargoRepository cargos = mock(CargoRepository.class);
    private final DepartamentoRepository departamentos = mock(DepartamentoRepository.class);
    private final AtleticaRepository atleticas = mock(AtleticaRepository.class);
    private final AtleticaResourceResolver resolver = mock(AtleticaResourceResolver.class);
    private final InvalidacaoSessaoUsuarioService sessoes =
        mock(InvalidacaoSessaoUsuarioService.class);
    private final AtividadeUsuarioService auditoria = mock(AtividadeUsuarioService.class);
    private final TransicaoGestaoService service =
        new TransicaoGestaoService(
            transicoes,
            itens,
            gestoes,
            vinculos,
            membros,
            cargos,
            departamentos,
            atleticas,
            resolver,
            sessoes,
            auditoria);
  }
}
