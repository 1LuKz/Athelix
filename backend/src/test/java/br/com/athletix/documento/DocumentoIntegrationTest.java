package br.com.athletix.documento;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest @AutoConfigureMockMvc @ActiveProfiles("dev")
class DocumentoIntegrationTest {
    @Autowired MockMvc mockMvc; @Autowired ObjectMapper mapper;
    @Test void criaVersionaArquivaEServeVersoesSemSobrescreverArquivo() throws Exception {
        Sessao admin=login(); String nome="Estatuto " + Long.toUnsignedString(System.nanoTime());
        JsonNode criado=send(admin,multipart("/documentos").file(pdf("arquivo","estatuto-v1.pdf","v1")).param("titulo",nome).param("categoria","ESTATUTO").param("visibilidade","MEMBROS").param("observacao","Versão inicial"),201);
        assertEquals(1,criado.get("versaoAtual").get("numeroVersao").asInt()); long documentoId=criado.get("id").asLong(); long arquivoV1=criado.get("versaoAtual").get("arquivoId").asLong();
        JsonNode versao2=send(admin,multipart("/documentos/{id}/versoes",documentoId).file(pdf("arquivo","estatuto-v2.pdf","v2")).param("observacao","Atualização"),201);
        assertEquals(2,versao2.get("numeroVersao").asInt()); assertNotEquals(arquivoV1,versao2.get("arquivoId").asLong());
        JsonNode versoes=send(admin,get("/documentos/{id}/versoes",documentoId),200); assertEquals(2,versoes.size());
        MvcResult conteudo=perform(admin,get("/documentos/{id}/versoes/{versao}/conteudo",documentoId,versoes.get(1).get("id").asLong()),200);
        assertTrue(new String(conteudo.getResponse().getContentAsByteArray()).contains("v1"));
        JsonNode atualizado=send(admin,put("/documentos/{id}",documentoId),200,"""
                {"titulo":"%s","categoria":"ESTATUTO","visibilidade":"MEMBROS","arquivado":true,"version":%d}
                """.formatted(nome,criado.get("version").asLong()));
        assertTrue(atualizado.get("arquivado").asBoolean()); assertEquals(0,send(admin,get("/documentos").param("busca",nome),200).size());
        assertEquals(1,send(admin,get("/documentos").param("busca",nome).param("incluirArquivados","true"),200).size());
    }
    private MockMultipartFile pdf(String part,String filename,String marker){return new MockMultipartFile(part,filename,"application/pdf",("%PDF-1.4 "+marker).getBytes());}
    private Sessao login() throws Exception {MvcResult c=mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();JsonNode t=json(c);MvcResult l=mockMvc.perform(post("/auth/login").cookie(c.getResponse().getCookie("XSRF-TOKEN")).header(t.get("headerName").asText(),t.get("token").asText()).contentType(MediaType.APPLICATION_JSON).content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}")).andExpect(status().isOk()).andReturn();return new Sessao((MockHttpSession)l.getRequest().getSession(false),c.getResponse().getCookie("XSRF-TOKEN"),t.get("headerName").asText(),t.get("token").asText());}
    private JsonNode send(Sessao s,MockHttpServletRequestBuilder r,int status) throws Exception{return json(perform(s,r,status));}
    private JsonNode send(Sessao s,MockHttpServletRequestBuilder r,int status,String body) throws Exception {r.contentType(MediaType.APPLICATION_JSON).content(body);return send(s,r,status);}
    private MvcResult perform(Sessao s,MockHttpServletRequestBuilder r,int expected) throws Exception{return mockMvc.perform(r.session(s.session()).cookie(s.cookie()).header(s.header(),s.token())).andExpect(status().is(expected)).andReturn();}
    private JsonNode json(MvcResult r) throws Exception{return mapper.readTree(r.getResponse().getContentAsString());}
    private record Sessao(MockHttpSession session,jakarta.servlet.http.Cookie cookie,String header,String token){}
}
