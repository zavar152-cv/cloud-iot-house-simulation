package ru.itmo.zavar;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import ru.itmo.zavar.service.impl.CloudLoggingServiceImpl;
import yandex.cloud.sdk.ServiceFactory;
import yandex.cloud.sdk.auth.Auth;
import yandex.cloud.sdk.auth.provider.CredentialProvider;

import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

@SpringBootApplication
public class FaccControlApplication {

	@Value("${yandex.auth-key-file}")
	private String authKeyFile;

	public static void main(String[] args) {
		SpringApplication.run(FaccControlApplication.class, args);
	}

	@Bean
	public ServiceFactory yandexCloudServiceFactory() throws URISyntaxException {
		CredentialProvider credentialProvider =
				Auth.apiKeyBuilder().fromFile(Paths.get(Objects.requireNonNull(CloudLoggingServiceImpl.class
								.getResource(authKeyFile)).toURI()))
						.build();

		return ServiceFactory.builder()
				.credentialProvider(credentialProvider)
				.build();
	}

}
