package br.com.athletix.arquivo.service;

import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
@ConfigurationProperties(prefix = "athletix.arquivos")
public class ArquivoArmazenamentoProperties {
    @Min(1)
    private long maxTamanhoBytes = 10L * 1024 * 1024;

    @Min(1)
    private long orcamentoPorAtleticaBytes = 100L * 1024 * 1024;

    public long getMaxTamanhoBytes() { return maxTamanhoBytes; }
    public void setMaxTamanhoBytes(long maxTamanhoBytes) { this.maxTamanhoBytes = maxTamanhoBytes; }
    public long getOrcamentoPorAtleticaBytes() { return orcamentoPorAtleticaBytes; }
    public void setOrcamentoPorAtleticaBytes(long orcamentoPorAtleticaBytes) {
        this.orcamentoPorAtleticaBytes = orcamentoPorAtleticaBytes;
    }
}
