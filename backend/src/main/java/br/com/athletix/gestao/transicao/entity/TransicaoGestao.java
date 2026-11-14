package br.com.athletix.gestao.transicao.entity;
import br.com.athletix.atletica.entity.Atletica; import br.com.athletix.shared.persistence.AuditableEntity; import br.com.athletix.shared.tenancy.AtleticaScoped; import jakarta.persistence.*; import java.time.Instant;
@Entity @Table(name="transicoes_gestao") public class TransicaoGestao extends AuditableEntity implements AtleticaScoped {
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="atletica_id",nullable=false) private Atletica atletica;
 @Column(name="gestao_origem_id",nullable=false) private Long gestaoOrigemId; @Column(name="gestao_destino_id",nullable=false) private Long gestaoDestinoId;
 @Enumerated(EnumType.STRING) @Column(nullable=false,length=12) private StatusTransicaoGestao status=StatusTransicaoGestao.RASCUNHO;
 @Column(name="preparada_por_membro_id",nullable=false) private Long preparadaPorMembroId; @Column(name="concluida_por_membro_id") private Long concluidaPorMembroId; @Column(name="concluida_em") private Instant concluidaEm;
 protected TransicaoGestao(){} public TransicaoGestao(Atletica a,Long o,Long d,Long p){atletica=a;gestaoOrigemId=o;gestaoDestinoId=d;preparadaPorMembroId=p;}
 @Override public Long getAtleticaId(){return atletica.getId();} public Long getGestaoOrigemId(){return gestaoOrigemId;} public Long getGestaoDestinoId(){return gestaoDestinoId;} public StatusTransicaoGestao getStatus(){return status;} public Long getPreparadaPorMembroId(){return preparadaPorMembroId;} public Long getConcluidaPorMembroId(){return concluidaPorMembroId;} public Instant getConcluidaEm(){return concluidaEm;} public void concluir(Long membro){status=StatusTransicaoGestao.CONCLUIDA;concluidaPorMembroId=membro;concluidaEm=Instant.now();}
}
