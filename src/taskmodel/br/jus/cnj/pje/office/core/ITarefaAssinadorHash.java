package br.jus.cnj.pje.office.core;

import java.util.List;
import java.util.Optional;

public interface ITarefaAssinadorHash {

  boolean isModoTeste();

  boolean isDeslogarKeyStore();

  Optional<String> getAlgoritmoAssinatura();

  Optional<String> getUploadUrl();

  List<IAssinadorHashArquivo> getArquivos();
}