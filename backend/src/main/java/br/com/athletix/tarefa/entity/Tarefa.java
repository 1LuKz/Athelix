package br.com.athletix.tarefa.entity;
import br.com.athletix.atletica.entity.Atletica; import br.com.athletix.departamento.entity.Departamento; import br.com.athletix.evento.entity.Evento; import br.com.athletix.membro.entity.Membro; import br.com.athletix.shared.persistence.AuditableEntity; import br.com.athletix.shared.tenancy.AtleticaScoped; import jakarta.persistence.*; import java.time.*;
@Entity @Table(name="tarefas") public class Tarefa extends AuditableEntity implements AtleticaScoped {
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="atletica_id") private Atletica atletica;
 @Column(name="gestao_origem_id") private Long gestaoOrigemId;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="evento_id") private Evento evento;
 @Column(nullable=false,length=160) private String titulo; @Column(length=3000) private String descricao;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="responsavel_membro_id") private Membro responsavel;
 @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="departamento_id") private Departamento departamento;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=8) private PrioridadeTarefa prioridade;
 private LocalDate prazo; @Enumerated(EnumType.STRING) @Column(nullable=false,length=16) private StatusTarefa status;
 @Column(name="concluida_em") private Instant concluidaEm; @Column(name="criada_por_membro_id",nullable=false) private Long criadaPorMembroId;
 protected Tarefa(){} public Tarefa(Atletica a,Long g,Evento e,String t,String d,Membro r,Departamento dep,PrioridadeTarefa p,LocalDate prazo,Long criador){atletica=a;gestaoOrigemId=g;evento=e;titulo=t;descricao=d;responsavel=r;departamento=dep;prioridade=p;this.prazo=prazo;criadaPorMembroId=criador;status=StatusTarefa.A_FAZER;}
 public Long getAtleticaId(){return atletica.getId();} public Long getEventoId(){return evento.getId();} public Long getResponsavelMembroId(){return responsavel==null?null:responsavel.getId();} public Long getDepartamentoId(){return departamento==null?null:departamento.getId();} public String getTitulo(){return titulo;} public String getDescricao(){return descricao;} public PrioridadeTarefa getPrioridade(){return prioridade;} public LocalDate getPrazo(){return prazo;} public StatusTarefa getStatus(){return status;} public Instant getConcluidaEm(){return concluidaEm;} public Long getGestaoOrigemId(){return gestaoOrigemId;} public Long getCriadaPorMembroId(){return criadaPorMembroId;}
 public void atualizar(String t,String d,Membro r,Departamento dep,PrioridadeTarefa p,LocalDate prazo){titulo=t;descricao=d;responsavel=r;departamento=dep;prioridade=p;this.prazo=prazo;} public void status(StatusTarefa s){status=s;concluidaEm=s==StatusTarefa.CONCLUIDO?Instant.now():null;}
}
