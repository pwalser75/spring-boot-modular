package ch.frostnova.app.boot.platform.service;

public interface SigningService {

    byte[] sign(byte[] data) throws Exception;

    boolean verify(byte[] data, byte[] signatureBytes) throws Exception;

}
