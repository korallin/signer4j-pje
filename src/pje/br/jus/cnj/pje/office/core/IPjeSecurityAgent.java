package br.jus.cnj.pje.office.core;

public interface IPjeSecurityAgent {
  
  String PARAM_NAME = IPjeSecurityAgent.class.getSimpleName() + ".instance";
  
  void refresh();
  
  boolean isPermitted(IMainParams params, StringBuilder whyNot);
}
