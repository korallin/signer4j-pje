package br.jus.cnj.pje.office.core;

import java.util.Optional;

import com.github.signer4j.IConfigPersister;
import com.github.signer4j.imp.function.Performable;

import br.jus.cnj.pje.office.signer4j.IPjeAuthStrategy;


public interface IPjeConfigPersister extends IConfigPersister {
  
  void loadServerAccess(Performable<IPjeServerAccess> add);

  Optional<String> authStrategy(); 

  void save(IPjeServerAccess... access);

  void save(IPjeAuthStrategy strategy);
  
  void overwrite(IPjeServerAccess... access);

  void delete(IPjeServerAccess access);
}