package ssl;

import javax.net.ssl.X509TrustManager;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class ArmitageTrustManager implements X509TrustManager {
	protected ArmitageTrustListener checker;

	public ArmitageTrustManager(ArmitageTrustListener checker) {
		this.checker = checker;
	}

	public void checkClientTrusted(X509Certificate[] ax509certificate, String authType) {
		return;
	}

	public void checkServerTrusted(X509Certificate[] ax509certificate, String authType) throws CertificateException {
		try {
            for (X509Certificate x509Certificate : ax509certificate) {
                byte[] bytesOfMessage = x509Certificate.getEncoded();
                MessageDigest md = MessageDigest.getInstance("SHA1");
                byte[] thedigest = md.digest(bytesOfMessage);

                BigInteger bi = new BigInteger(1, thedigest);
                String fingerprint = bi.toString(16);

                if (checker != null && !checker.trust(fingerprint))
                    throw new CertificateException("Certificate Rejected. Press Cancel.");
            }

			return;
		}
		catch (CertificateException cex) {
			throw cex;
		}
		catch (Exception ex) {
			throw new CertificateException(ex.getMessage());
		}
	}

	public X509Certificate[] getAcceptedIssuers() {
		return new X509Certificate[0];
	}
}
