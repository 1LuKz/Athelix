package br.com.athletix.config.demo;

import br.com.athletix.atletica.entity.Atletica;
import br.com.athletix.departamento.entity.Departamento;
import br.com.athletix.gestao.entity.Gestao;
import br.com.athletix.membro.entity.Membro;
import java.time.Instant;
import java.time.LocalDate;

record CenarioDemo(
    Atletica atletica,
    Gestao gestao,
    Membro administrador,
    Membro tesoureiro,
    Membro gestora,
    Membro associado,
    Departamento esportes,
    LocalDate hoje,
    Instant agora) {}
