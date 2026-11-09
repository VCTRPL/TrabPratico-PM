package br.pucminas.hospital.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErroResponse(Instant timestamp, int status, String mensagem, Map<String, String> campos) {

    public static ErroResponse de(int status, String mensagem) {
        return new ErroResponse(Instant.now(), status, mensagem, null);
    }

    public static ErroResponse de(int status, String mensagem, Map<String, String> campos) {
        return new ErroResponse(Instant.now(), status, mensagem, campos);
    }
}
