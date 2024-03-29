package br.jus.cnj.pje.office.core.imp;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.github.signer4j.IByteProcessor;
import com.github.signer4j.ISignatureAlgorithm;
import com.github.signer4j.imp.Base64;
import com.github.signer4j.imp.Params;
import com.github.signer4j.imp.exception.Signer4JException;
import com.github.signer4j.imp.exception.Signer4JRuntimeException;
import com.github.signer4j.progress.IProgress;
import com.github.signer4j.progress.IStage;
import com.github.signer4j.task.ITaskResponse;
import com.github.signer4j.task.exception.TaskException;

import br.jus.cnj.pje.office.core.IAssinadorBase64Arquivo;
import br.jus.cnj.pje.office.core.IAssinadorBase64ArquivoAssinado;
import br.jus.cnj.pje.office.core.IPjeClient;
import br.jus.cnj.pje.office.core.ITarefaAssinadorBase64;
import br.jus.cnj.pje.office.core.imp.TarefaAssinadorBase64Reader.AssinadorBase64ArquivoAssinado;
import br.jus.cnj.pje.office.signer4j.IPjeToken;
import br.jus.cnj.pje.office.web.IPjeResponse;

public class PjeAssinadorBase64Task extends PjeAbstractTask<ITarefaAssinadorBase64> {

  private static enum Stage implements IStage {
    FILE_SIGNING("Assinatura de arquivos"),
    
    FILE_SENDING("Envio de arquivos");

    private final String message;

    Stage(String message) {
      this.message = message;
    }

    @Override
    public final String toString() {
      return message;
    }
  }
  
  public PjeAssinadorBase64Task(Params request, ITarefaAssinadorBase64 pojo) {
    super(request, pojo);
  }
  
  private ISignatureAlgorithm algoritmoAssinatura;

  private String uploadUrl;

  private List<IAssinadorBase64Arquivo> arquivos;

  @SuppressWarnings("unchecked")
  @Override
  protected void validateParams() throws TaskException {
   ITarefaAssinadorBase64 pojo = getPojoParams();
   this.algoritmoAssinatura = PjeTaskChecker.checkIfSupportedSig(pojo.getAlgoritmoAssinatura(), "algoritmoAssinatura");
   this.uploadUrl = PjeTaskChecker.checkIfPresent(pojo.getUploadUrl(), "uploadUrl");
   this.arquivos = PjeTaskChecker.checkIfNotEmpty(pojo.getArquivos(), "arquivos");
  }

  @Override
  protected ITaskResponse<IPjeResponse> doGet() throws TaskException {
    
    final IProgress progress = getProgress();
    
    IPjeToken token = loginToken();
    try {
      IByteProcessor processor = token.signerBuilder().usingAlgorigthm(algoritmoAssinatura).build();
          
      final int total;
      progress.begin(Stage.FILE_SIGNING, total = arquivos.size());
        
      List<IAssinadorBase64ArquivoAssinado> saida = new ArrayList<>(total);
      
      for(int i = 0; i < total; i++) {
        IAssinadorBase64Arquivo arquivo = arquivos.get(i);
        Optional<String> hashDoc = arquivo.getHashDoc();
        if (!hashDoc.isPresent()) {
          progress.step("Ignorada entrada %s", i);
          LOGGER.warn("'hashDoc' nao encontrado na lista vinda do servidor. Entrada ignorada");
          continue;
        }
        Optional<String> base64 = arquivo.getConteudoBase64();
        if (!base64.isPresent()) {
          progress.step("Ignorada entrada %s", i);
          LOGGER.warn("'conteudoBase64' não encontrado na lista vinda do servidor. Entrada ignorada");
          continue;
        }
        byte[] input = Base64.base64Decode(base64.get());
        progress.step("Assinando arquivo %s de tamanho %s", i, input.length);
        
        try {
          saida.add(new AssinadorBase64ArquivoAssinado(
            hashDoc.get(),
            processor.process64(input)
          ));
        } catch (Signer4JException e) {
          progress.abort(e);

          int remainder = total - i - 1;
          if (remainder > 0) {
            if (!token.isAuthenticated()) {
              try {
                token = loginToken();
              }catch(Signer4JRuntimeException ex) {
                progress.abort(e);
                ex.addSuppressed(e);
                throw new TaskException("Não foi possível recuperar autenticação do token.", ex);
              }
              processor = token.signerBuilder().build();
            }
            progress.begin(Stage.FILE_SIGNING, remainder);
          }
        }finally {
          input = null;
          arquivo.dispose();
        }
      }
      progress.end();
      
      if (saida.isEmpty()) {
        throw new TaskException("Nenhum documento pôde ser assinado de um total de " + total);
      }
      
      progress.begin(Stage.FILE_SENDING);
      final IPjeClient client = getPjeClient();
      try {
        client.send(getEndpointFor(uploadUrl), getSession(), getUserAgent(), saida);
      }catch(Exception e) {
        TaskException ex =  new TaskException("Não foi possível enviar os dados ao servidor", e);
        progress.abort(ex);
        throw ex;
      }finally {
        saida.forEach(IAssinadorBase64ArquivoAssinado::dispose);
        saida.clear();
        saida = null;
      }
      progress.end();
      return PjeResponse.SUCCESS;
    }finally {
      token.logout();
    }
  }

}
