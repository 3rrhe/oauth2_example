package com.umg.sads.oauth2_example;

import java.util.Optional;

import org.apache.cxf.interceptor.security.AccessDeniedException;
import org.apache.http.HttpHeaders;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.github.malkomich.oauth2.token.client.AccessToken;
import com.github.malkomich.oauth2.token.client.OAuth2Client;
import com.github.malkomich.oauth2.token.client.OAuth2Config;
import com.github.malkomich.oauth2.token.client.exception.OAuth2ClientException;

import reactor.core.publisher.Mono;


class WebClientConfig {

    @Bean(name = "securedWebClient")
    WebClient fetchWebClient(@Value("${host}") String host,
                             OAuth2Config oAuth2Config) {
      OAuth2Client oAuth2Client = OAuth2Client.withConfig(oAuth2Config).build();
      return WebClient.builder()
                      .filter(new OAuth2ExchangeFilter(oAuth2Client))
                      .baseUrl(host)
                      .build();
    }
  
    @Bean
    @ConfigurationProperties(prefix = "security.oauth2.config")
    OAuth2Config oAuth2Config() {
      return new OAuth2Config();
    }
  
    class OAuth2ExchangeFilter implements ExchangeFilterFunction {
      
      public OAuth2ExchangeFilter(OAuth2Client oAuth2Client) {
        this.oAuth2Client = oAuth2Client;
      }

      OAuth2Client oAuth2Client;
  
      @Override
      public Mono<ClientResponse> filter(ClientRequest request,
                                         ExchangeFunction next) {
        String token;
        try {
          token = Optional.ofNullable(oAuth2Client.accessToken())
                                 .map(AccessToken::getAccessToken)
                                 .map("Bearer "::concat)
                                 .orElseThrow(() -> new AccessDeniedException("error"));
        } catch (AccessDeniedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        } catch (OAuth2ClientException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
  
        final ClientRequest newRequest = ClientRequest.from(request)
                                                .header(HttpHeaders.AUTHORIZATION, token)
                                                .build();
        return next.exchange(newRequest);
      }
    }
  }
