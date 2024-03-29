package br.jus.cnj.pje.office.core.imp;

import com.github.signer4j.imp.Params;
import com.github.signer4j.task.ITaskResponse;
import com.github.signer4j.task.exception.TaskException;

import br.jus.cnj.pje.office.core.IPjeClient;
import br.jus.cnj.pje.office.core.ITarefaCertChain;
import br.jus.cnj.pje.office.signer4j.IPjeToken;
import br.jus.cnj.pje.office.web.IPjeResponse;

public class PjeCertChainTask extends PjeAbstractTask<ITarefaCertChain> {

  private String uploadUrl;
  
  public PjeCertChainTask(Params params, ITarefaCertChain pojo) {
    super(params, pojo);
  }

  @Override
  protected void validateParams() throws TaskException {
    ITarefaCertChain pojo = getPojoParams();
    this.uploadUrl = PjeTaskChecker.checkIfPresent(pojo.getUploadUrl(),  "uploadUrl");
  }

  @Override
  protected ITaskResponse<IPjeResponse> doGet() throws TaskException {
    IPjeToken token = loginToken();
    try {
      String certificateChain64;
      try {
        certificateChain64 = token.createChooser().choose().getCertificate64();
      } catch (Exception e) {
        throw new TaskException("Escolha do certificado cancelada", e);
      }
      
      IPjeClient client = getPjeClient();
    
      try {
        client.send(
          getEndpointFor(uploadUrl), 
          getSession(),
          getUserAgent(), 
          certificateChain64
        );
      } catch (PJeClientException e) {
        throw new TaskException("Não foi possível enviar cadeia de certificados", e);
      }
      return PjeResponse.SUCCESS;
    }finally {
      token.logout();
    }
  }
}
