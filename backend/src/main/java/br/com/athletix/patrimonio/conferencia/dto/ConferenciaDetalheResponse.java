package br.com.athletix.patrimonio.conferencia.dto; import java.util.List;
public record ConferenciaDetalheResponse(ConferenciaResponse conferencia,long unidadesEsperadas,long unidadesVerificadasValidas,Integer progresso,List<ItemConferenciaResponse> itens){}
