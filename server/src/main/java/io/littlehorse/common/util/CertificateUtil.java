package io.littlehorse.common.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

public class CertificateUtil {
    public static String getCommonNameFromCertificate(File certificate)
            throws CertificateException, InvalidNameException, IOException {
        FileInputStream fileInputStream;

        try {
            fileInputStream = new FileInputStream(certificate);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        try (BufferedInputStream bis = new BufferedInputStream(fileInputStream)) {
            CertificateFactory cf;
            cf = CertificateFactory.getInstance("X.509");
            while (bis.available() > 0) {
                X509Certificate cert = (X509Certificate) cf.generateCertificate(bis);
                LdapName ln = new LdapName(cert.getSubjectX500Principal().getName());

                for (Rdn rdn : ln.getRdns()) {
                    if (rdn.getType().equalsIgnoreCase("CN")) {
                        return rdn.getValue().toString();
                    }
                }
            }
        }

        return null;
    }
}
