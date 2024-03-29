package br.jus.cnj.pje.office.core;

import java.util.Optional;

public interface ITarefaAutenticador {

  Optional<String> getAlgoritmoAssinatura();

  Optional<String> getEnviarPara();

  Optional<String> getMensagem();

  Optional<String> getToken();
}