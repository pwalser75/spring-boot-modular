package ch.frostnova.app.boot.platform.service.impl;

import ch.frostnova.app.boot.platform.config.SigningConfig;
import ch.frostnova.app.boot.platform.service.SigningService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.Signature;

@Service
public class SigningServiceImpl implements SigningService {

    @Autowired
    private SigningConfig signingConfig;

    @Override
    public byte[] sign(byte[] data) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("data is required");
        }
        Signature signature = Signature.getInstance(signingConfig.requireKeyType().getSignatureAlgorithm().getJcaName());
        signature.initSign(signingConfig.requirePrivateKey());
        signature.update(data);
        return signature.sign();
    }

    @Override
    public boolean verify(byte[] data, byte[] signatureBytes) throws Exception {
        if (data == null) {
            throw new IllegalArgumentException("data is required");
        }
        if (signatureBytes == null) {
            throw new IllegalArgumentException("signatureBytes is required");
        }
        Signature signature = Signature.getInstance(signingConfig.requireKeyType().getSignatureAlgorithm().getJcaName());
        signature.initVerify(signingConfig.requirePublicKey());
        signature.update(data);
        return signature.verify(signatureBytes);
    }
}
