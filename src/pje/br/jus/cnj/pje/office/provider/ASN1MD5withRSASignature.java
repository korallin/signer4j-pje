package br.jus.cnj.pje.office.provider;

import java.io.IOException;
import java.lang.reflect.Field;
import java.security.InvalidKeyException;
import java.security.InvalidParameterException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;

import org.bouncycastle.asn1.DERNull;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.DigestInfo;

public class ASN1MD5withRSASignature extends Signature implements Cloneable {
  
  private static final String PROVIDER_SUN_MSCAPI = "SunMSCAPI";
  private static final String PROVIDER_BC = "BC";
  private String provider;
  private Signature delegateSignature;

  public ASN1MD5withRSASignature() throws NoSuchAlgorithmException {
    super("ASN1MD5withRSA");
    this.provider = "";
  }

  public void engineInitVerify(final PublicKey publicKey) throws InvalidKeyException {
    throw new IllegalArgumentException("Para verificar assinatura, usar o MD5withRSA.");
  }

  public void engineInitSign(final PrivateKey privateKey) throws InvalidKeyException {
    try {
      if (privateKey.getClass().getCanonicalName().equals("sun.security.mscapi.RSAPrivateKey")) {
        final Signature signatureMSCAPI = Signature.getInstance("MD5withRSA", PROVIDER_SUN_MSCAPI);
        final MessageDigest digestNullMd5 = MessageDigest.getInstance("nullMD5");
        digestNullMd5.reset();
        final Field f = Class.forName("java.security.Signature$Delegate").getDeclaredField("sigSpi");
        f.setAccessible(true);
        final Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(f, f.getModifiers() & 0xFFFFFFEF);
        final SignatureSpi signatureSpi = (SignatureSpi)f.get(signatureMSCAPI);
        final Field fMessageDigest = Class.forName("sun.security.mscapi.RSASignature").getDeclaredField("messageDigest");
        fMessageDigest.setAccessible(true);
        final Field modifiersFieldMessageDigest = Field.class.getDeclaredField("modifiers");
        modifiersFieldMessageDigest.setAccessible(true);
        modifiersFieldMessageDigest.setInt(f, f.getModifiers() & 0xFFFFFFEF);
        fMessageDigest.set(signatureSpi, digestNullMd5);
        this.delegateSignature = signatureMSCAPI;
        this.provider = "SunMSCAPI";
      }
      else {
        this.delegateSignature = Signature.getInstance("nonewithRSA");
        this.provider = PROVIDER_BC;
      }
      this.delegateSignature.initSign(privateKey);
    }
    catch (Exception e) {
      throw new IllegalArgumentException("N\u00e3o foi poss\u00edvel assinar, utilizar fallback para garantir assinatura.");
    }
  }

  public void engineUpdate(final byte b) throws SignatureException {
    try {
      if (PROVIDER_BC.equals(this.provider)) {
        throw new IllegalArgumentException("Opera\ufffd\ufffdo n\ufffdo dispon\ufffdvel, utilizar fallback para garantir assinatura.");
      }
      this.delegateSignature.update(b);
    }
    catch (NullPointerException npe) {
      throw new SignatureException("No SHA digest found");
    }
  }

  public void engineUpdate(final byte[] b, final int offset, final int length) throws SignatureException {
    try {
      if (PROVIDER_BC.equals(this.provider)) {
        final DigestInfo dInfo = new DigestInfo(new AlgorithmIdentifier(PKCSObjectIdentifiers.md5, DERNull.INSTANCE), b);
        try {
          this.delegateSignature.update(dInfo.getEncoded("DER"));
        }
        catch (IOException e) {
          throw new IllegalArgumentException("Erro ao gerar ASN.1 do provider 'BC', utilizar fallback para garantir assinatura.");
        }
      }
      else {
        this.delegateSignature.update(b, offset, length);
      }
    }
    catch (NullPointerException npe) {
      throw new SignatureException("No SHA digest found");
    }
  }

  public byte[] engineSign() throws SignatureException {
    try {
      return this.delegateSignature.sign();
    }
    catch (NullPointerException npe) {
      throw new SignatureException("No SHA digest found");
    }
  }

  public boolean engineVerify(final byte[] sigBytes) throws SignatureException {
    throw new IllegalArgumentException("Para verificar assinatura, usar o MD5withRSA.");
  }

  public void engineSetParameter(final String param, final Object value) {
    throw new InvalidParameterException("No parameters");
  }

  public void engineSetParameter(final AlgorithmParameterSpec aps) {
    throw new InvalidParameterException("No parameters");
  }

  public Object engineGetParameter(final String param) {
    throw new InvalidParameterException("No parameters");
  }

  public void engineReset() {
  }
}
