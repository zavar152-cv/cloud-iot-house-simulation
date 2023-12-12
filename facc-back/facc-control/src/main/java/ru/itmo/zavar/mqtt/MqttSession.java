package ru.itmo.zavar.mqtt;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import ru.itmo.zavar.FaccControlApplication;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Objects;

@Slf4j
public final class MqttSession implements MqttCallback {

    private static final String TRUSTED_ROOT = """
            -----BEGIN CERTIFICATE-----
            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
            LpuQKbSbIERsmR+QqQ==
            -----END CERTIFICATE-----""";

    private final MqttClient client;
    private final String clientId;
    private final MqttConnectOptions connOpts;
    private Runnable onDoneHandler;
    private QoS messageQos = QoS.AT_LEAST_ONCE;

    public MqttSession(String broker, String clientId, String objectId) throws Exception {
        File file = new File(Objects.requireNonNull(getClass().getResource("/ssl/" + objectId)).getFile());
        String certsDir = file.getAbsolutePath();
        this.clientId = clientId;
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        connOpts = new MqttConnectOptions();
        connOpts.setSocketFactory(getSocketFactoryWithCerts(certsDir));
        connOpts.setCleanSession(true);
        connOpts.setKeepAliveInterval(60);
        connOpts.setConnectionTimeout(60);
    }

    public MqttSession(String broker, String clientId, String login, String password) throws Exception {
        this.clientId = clientId;
        client = new MqttClient(broker, clientId);
        client.setCallback(this);
        connOpts = new MqttConnectOptions();
        connOpts.setSocketFactory(getSocketFactory());
        connOpts.setCleanSession(true);
        connOpts.setKeepAliveInterval(60);
        connOpts.setConnectionTimeout(60);
        connOpts.setUserName(login.trim());
        connOpts.setPassword(password.trim().toCharArray());
    }

    public void setOnDoneHandler(Runnable onDone) {
        onDoneHandler = onDone;
    }

    public void setQoS(QoS value) {
        messageQos = value;
    }

    @Override
    public void connectionLost(Throwable cause) {
        log.error("Connection lost for " + clientId, cause);
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        log.info("Message arrived for " + clientId + "> " + topic + ": " + message.toString().replace("\n", ""));
        if(onDoneHandler != null)
            onDoneHandler.run();
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        log.info("Delivery complete for " + clientId);
    }

    public void publish(String topic, String payload) throws MqttException {
        MqttMessage msg = new MqttMessage(payload.getBytes());
        msg.setQos(messageQos.getValue());
        client.publish(topic, msg);
    }

    public void subscribe(String topic) throws MqttException {
        client.subscribe(topic, messageQos.getValue());
    }

    public void stop() throws MqttException {
        client.disconnect();
        client.close();
        log.info("Disconnected for " + clientId);
    }

    public void start() throws Exception {
        client.connect(connOpts);
        log.info("Connected for " + clientId);
    }

    private SSLSocketFactory getSocketFactory()
            throws Exception {
        InputStream is = new ByteArrayInputStream(TRUSTED_ROOT.getBytes(StandardCharsets.UTF_8));
        CertificateFactory cFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cFactory.generateCertificate(
                is);

        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
        tks.load(null); // You don't need the KeyStore instance to come from a file.
        tks.setCertificateEntry("caCert", caCert);
        tmf.init(tks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(null, tmf.getTrustManagers(), null);
        return ctx.getSocketFactory();
    }

    private SSLSocketFactory getSocketFactoryWithCerts(String certsDir)
            throws Exception {
        // Client key/cert:
        final char[] empty = "".toCharArray();
        KeyStore ks = KeyStore.getInstance("PKCS12");
        // To obtain |.p12| from |.pem|:
        // openssl pkcs12 -export -in cert.pem -inkey key.pem -out keystore.p12
        ks.load(new FileInputStream(
                        Paths.get(certsDir, "keystore.p12").toString()),
                empty);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, empty);

        InputStream is = new ByteArrayInputStream(TRUSTED_ROOT.getBytes(StandardCharsets.UTF_8));
        CertificateFactory cFactory = CertificateFactory.getInstance("X.509");
        X509Certificate caCert = (X509Certificate) cFactory.generateCertificate(
                is);

        TrustManagerFactory tmf = TrustManagerFactory
                .getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore tks = KeyStore.getInstance(KeyStore.getDefaultType());
        tks.load(null); // You don't need the KeyStore instance to come from a file.
        tks.setCertificateEntry("caCert", caCert);
        tmf.init(tks);

        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
        return ctx.getSocketFactory();
    }

    @Getter
    @RequiredArgsConstructor
    public enum QoS {
        AT_MOST_ONCE(0),
        AT_LEAST_ONCE(1);

        private final int value;
    }
}
