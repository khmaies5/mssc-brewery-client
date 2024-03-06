package guru.springframework.msscbreweryclient.web.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.Timeout;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BlockingRestTemplateCustomizer implements RestTemplateCustomizer {

    private final Integer maxTotalConnections;
    private final Integer defaultMaxRoute;
    private final Integer connectionTimeOut;

    public BlockingRestTemplateCustomizer(@Value("${sfg.maxtotalconnections}") Integer maxTotalConnections,
                                          @Value("${sfg.defaultmaxroute}") Integer defaultMaxRoute,
                                          @Value("${sfg.connectiontimeout}") Integer connectionTimeOut) {
        this.maxTotalConnections = maxTotalConnections;
        this.defaultMaxRoute = defaultMaxRoute;
        this.connectionTimeOut = connectionTimeOut;
    }

    public ClientHttpRequestFactory clientHttpRequestFactory() {
        PoolingHttpClientConnectionManager clientConnectionManager = new PoolingHttpClientConnectionManager();
        clientConnectionManager.setMaxTotal(maxTotalConnections);
        clientConnectionManager.setDefaultMaxPerRoute(defaultMaxRoute);

        RequestConfig requestConfig = RequestConfig
                .custom()
                .setConnectionRequestTimeout(Timeout.ofSeconds(connectionTimeOut))
                .build();

        CloseableHttpClient httpClient = HttpClients
                .custom()
                .setConnectionManager(clientConnectionManager)
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                .setDefaultRequestConfig(requestConfig)
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Override
    public void customize(RestTemplate restTemplate) {
        restTemplate.setRequestFactory(this.clientHttpRequestFactory());
    }
}
