package br.com.athletix.config.demo;

import static br.com.athletix.config.demo.DemoData.APPLICATION_PDF;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.springframework.web.multipart.MultipartFile;

/** Upload PDF sintético válido, submetido às mesmas verificações dos arquivos enviados pela API. */
record ArquivoSintetico(String nomeOriginal, byte[] conteudo) implements MultipartFile {
  static MultipartFile pdf(String nome, String texto) {
    String textoSeguro =
        Normalizer.normalize(texto, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "")
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)");
    String comandos = "BT /F1 16 Tf 50 760 Td (" + textoSeguro + ") Tj ET\n";
    List<String> objetos =
        List.of(
            "<< /Type /Catalog /Pages 2 0 R >>",
            "<< /Type /Pages /Kids [3 0 R] /Count 1 >>",
            "<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] "
                + "/Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>",
            "<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>",
            "<< /Length " + comandos.length() + " >>\nstream\n" + comandos + "endstream");
    StringBuilder documento = new StringBuilder("%PDF-1.4\n");
    List<Integer> deslocamentos = new ArrayList<>();
    for (int indice = 0; indice < objetos.size(); indice++) {
      deslocamentos.add(documento.length());
      documento
          .append(indice + 1)
          .append(" 0 obj\n")
          .append(objetos.get(indice))
          .append("\nendobj\n");
    }
    int inicioReferencias = documento.length();
    documento.append("xref\n0 ").append(objetos.size() + 1).append("\n0000000000 65535 f \n");
    for (Integer deslocamento : deslocamentos) {
      documento.append(String.format(Locale.ROOT, "%010d 00000 n \n", deslocamento));
    }
    documento
        .append("trailer\n<< /Size ")
        .append(objetos.size() + 1)
        .append(" /Root 1 0 R >>\nstartxref\n")
        .append(inicioReferencias)
        .append("\n%%EOF\n");
    return new ArquivoSintetico(nome, documento.toString().getBytes(StandardCharsets.US_ASCII));
  }

  @Override
  public String getName() {
    return "arquivo";
  }

  @Override
  public String getOriginalFilename() {
    return nomeOriginal;
  }

  @Override
  public String getContentType() {
    return APPLICATION_PDF;
  }

  @Override
  public boolean isEmpty() {
    return conteudo.length == 0;
  }

  @Override
  public long getSize() {
    return conteudo.length;
  }

  @Override
  public byte[] getBytes() {
    return conteudo.clone();
  }

  @Override
  public InputStream getInputStream() {
    return new ByteArrayInputStream(conteudo);
  }

  @Override
  public void transferTo(File destino) throws IOException {
    Files.write(destino.toPath(), conteudo);
  }
}
