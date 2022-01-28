package bio.terra.profile.service.crl;

import bio.terra.cloudres.common.ClientConfig;
import bio.terra.cloudres.google.billing.CloudBillingClientCow;
import bio.terra.profile.service.crl.exception.CrlInternalException;
import bio.terra.profile.service.crl.exception.CrlSecurityException;
import com.google.auth.oauth2.GoogleCredentials;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CrlService {
    /** The client name required by CRL. */
    private static final String CLIENT_NAME = "profile";

    private final ClientConfig clientConfig;
    private final CloudBillingClientCow crlBillingClientCow;

    @Autowired
    public CrlService() {
        var creds = getApplicationCredentials();
        this.clientConfig = buildClientConfig();
        try {
            this.crlBillingClientCow = new CloudBillingClientCow(clientConfig, creds);
        } catch (IOException e) {
            throw new CrlInternalException("Error creating resource manager wrapper", e);
        }
    }

    /** Returns the CRL {@link CloudBillingClientCow} which wraps Google Billing API. */
    public CloudBillingClientCow getCloudBillingClientCow() {
        return crlBillingClientCow;
    }

    private ClientConfig buildClientConfig() {
        // Billing profile manager does not create any cloud resources, so no need to use Janitor.
        return ClientConfig.Builder.newBuilder().setClient(CLIENT_NAME).build();
    }

    private GoogleCredentials getApplicationCredentials() {
        try {
            return GoogleCredentials.getApplicationDefault();
        } catch (IOException e) {
            throw new CrlSecurityException("Failed to get credentials", e);
        }
    }
}
