package br.jus.cnj.pje.office.core;

import java.util.Optional;

import com.github.signer4j.IDisposable;

public interface IAssinadorBase64Arquivo extends IDisposable {

  Optional<String> getHashDoc();

  Optional<String> getConteudoBase64();
}