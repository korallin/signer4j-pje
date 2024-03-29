package br.jus.cnj.pje.office.core;

import java.util.List;
import java.util.Optional;

import com.github.signer4j.ISignatureAlgorithm;
import com.github.signer4j.ISignatureType;

public interface ITarefaAssinador {
  
  List<IArquivo> getArquivos();

  Optional<String> getEnviarPara();

  Optional<IPjeSignMode> getModo();

  Optional<ISignatureType> getTipoAssinatura();

  boolean isDeslogarKeyStore();

  Optional<ISignatureAlgorithm> getAlgoritmoHash();

  Optional<IAssinaturaPadrao> getPadraoAssinatura();
}