package br.com.athletix.gestao.transicao.dto; import java.util.*;
public record PreviaTransicaoResponse(TransicaoResponse transicao,long acessosEntram,long acessosSaem,long responsaveisSubstituidos,Map<String,Long> pendencias){}
