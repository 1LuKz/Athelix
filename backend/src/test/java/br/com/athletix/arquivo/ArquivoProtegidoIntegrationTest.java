package br.com.athletix.arquivo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import br.com.athletix.arquivo.dto.ArquivoMetadata;
import br.com.athletix.arquivo.entity.FinalidadeArquivo;
import br.com.athletix.arquivo.service.ArquivoAcessoAoConteudo;
import br.com.athletix.arquivo.service.ArquivoArmazenamentoService;
import br.com.athletix.membro.entity.Membro;
import br.com.athletix.membro.entity.StatusMembro;
import br.com.athletix.membro.repository.MembroRepository;
import br.com.athletix.shared.exception.BusinessRuleException;
import br.com.athletix.shared.tenancy.ContextoAtletica;
import br.com.athletix.usuario.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class ArquivoProtegidoIntegrationTest {
  @Autowired private ArquivoArmazenamentoService armazenamento;
  @Autowired private MembroRepository membros;
  @Autowired private UsuarioRepository usuarios;
  @Autowired private ContextoAtletica contextoAtletica;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;

  @MockBean(name = "anexoEventoArquivoAcessoAoConteudo")
  private ArquivoAcessoAoConteudo acessoAoConteudo;

  @Test
  void armazenaPorFinalidadeValidaAssinaturaELeSomenteComAcessoDoPai() throws Exception {
    Long usuarioAdminId = usuarios.findByEmail("admin@athletix.local").orElseThrow().getId();
    Membro admin =
        membros
            .findAllByUsuario_IdAndStatusOrderByAtletica_NomeAsc(usuarioAdminId, StatusMembro.ATIVO)
            .get(0);
    ArquivoMetadata metadata;
    try (ContextoAtletica.Escopo ignored = contextoAtletica.abrir(admin.getAtleticaId())) {
      metadata =
          armazenamento.armazenar(
              new MockMultipartFile(
                  "arquivo",
                  "../publicacao.pdf",
                  "application/pdf",
                  "%PDF-1.7\nconteudo protegido".getBytes(StandardCharsets.US_ASCII)),
              FinalidadeArquivo.DOCUMENTO,
              admin);
      assertEquals("publicacao.pdf", metadata.nomeOriginal());
      assertEquals(64, metadata.hashConteudo().length());
      assertThrows(
          BusinessRuleException.class,
          () ->
              armazenamento.armazenar(
                  new MockMultipartFile(
                      "arquivo",
                      "pagina.pdf",
                      "application/pdf",
                      "<html>não é PDF</html>".getBytes(StandardCharsets.UTF_8)),
                  FinalidadeArquivo.DOCUMENTO,
                  admin));
      assertThrows(
          BusinessRuleException.class,
          () ->
              armazenamento.armazenar(
                  new MockMultipartFile(
                      "arquivo",
                      "imagem.png",
                      "image/png",
                      new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
                  FinalidadeArquivo.DOCUMENTO,
                  admin));
    }

    Sessao sessao = autenticar();
    when(acessoAoConteudo.decidirLeitura(any(), any()))
        .thenReturn(ArquivoAcessoAoConteudo.DecisaoLeitura.NAO_SE_APLICA);
    mockMvc
        .perform(get("/arquivos/{id}/conteudo", metadata.id()).session(sessao.session()))
        .andExpect(status().isNotFound());

    when(acessoAoConteudo.decidirLeitura(any(), any()))
        .thenReturn(ArquivoAcessoAoConteudo.DecisaoLeitura.PERMITIR);
    MvcResult resultado =
        mockMvc
            .perform(get("/arquivos/{id}/conteudo", metadata.id()).session(sessao.session()))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Content-Type-Options", "nosniff"))
            .andExpect(
                header()
                    .string("Content-Disposition", org.hamcrest.Matchers.containsString("inline")))
            .andReturn();
    assertTrue(resultado.getResponse().getContentAsByteArray().length > 0);
  }

  private Sessao autenticar() throws Exception {
    MvcResult csrfResult =
        mockMvc.perform(get("/auth/csrf")).andExpect(status().isOk()).andReturn();
    JsonNode csrf = objectMapper.readTree(csrfResult.getResponse().getContentAsString());
    MvcResult login =
        mockMvc
            .perform(
                post("/auth/login")
                    .cookie(csrfResult.getResponse().getCookie("XSRF-TOKEN"))
                    .header(csrf.get("headerName").asText(), csrf.get("token").asText())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"email\":\"admin@athletix.local\",\"senha\":\"Athletix@2026\"}"))
            .andExpect(status().isOk())
            .andReturn();
    return new Sessao((MockHttpSession) login.getRequest().getSession(false));
  }

  private record Sessao(MockHttpSession session) {}
}
