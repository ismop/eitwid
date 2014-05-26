package pl.ismop.web.controllers;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.logging.Logger;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.ClientRequestContext;
import javax.ws.rs.client.ClientRequestFilter;
import javax.ws.rs.client.WebTarget;

import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.glassfish.jersey.filter.LoggingFilter;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import pl.ismop.web.Application;
import pl.ismop.web.client.dap.levee.LeveeServiceSync;
import pl.ismop.web.client.dap.levee.LeveesResponse;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class RestClientTest {
	class AuthFilter implements ClientRequestFilter {
		@Override
		public void filter(ClientRequestContext requestContext) throws IOException {
			requestContext.getHeaders().add("PRIVATE-TOKEN", dapToken);
		}
	}
	
	@Value("${dap.token}")
	private String dapToken;
	@Value("${dap.endpoint}")
	private String dapEndpoint;

	@Test
	public void testGet() throws KeyManagementException, NoSuchAlgorithmException, KeyStoreException {
		SSLContext sslContext = SSLContext.getInstance("SSL");
		sslContext.init(null, new TrustManager[] { new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			public void checkClientTrusted(X509Certificate[] certs, String authType) {
			}

			public void checkServerTrusted(X509Certificate[] certs, String authType) {
			}
		} }, new SecureRandom());
		Client client = ClientBuilder.newBuilder()
				.sslContext(sslContext)
				.hostnameVerifier((String hostname, SSLSession session) -> {
					return true;
				})
				.build();
		client.register(new AuthFilter());
		client.register(JacksonFeature.class);
		WebTarget target = client.target(dapEndpoint);
		LeveeServiceSync leveeService = WebResourceFactory.newResource(LeveeServiceSync.class, target);
		LeveesResponse responseFromGet = leveeService.getLevees();
		System.out.println(responseFromGet);
	}
}