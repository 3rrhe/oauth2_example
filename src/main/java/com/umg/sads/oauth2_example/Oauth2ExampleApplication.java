package com.umg.sads.oauth2_example;

import org.apache.el.stream.Optional;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.malkomich.oauth2.token.client.AccessToken;
import com.github.malkomich.oauth2.token.client.OAuth2Client;
import com.github.malkomich.oauth2.token.client.OAuth2Config;

@SpringBootApplication
@RestController
public class Oauth2ExampleApplication {

	OAuth2Config oAuth2Config;
	OAuth2Client oAuth2Client;

	public static void main(String[] args, OAuth2Config oAuth2Config) {
		SpringApplication.run(Oauth2ExampleApplication.class, args);
		oAuth2Client = OAuth2Client
			.withConfig(oAuth2Config) // Initialize OAuth2Config from your config files
			.build();
	}

	@GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {

		String token = Optional.ofNullable(oAuth2Client.accessToken())
			.map(AccessToken::getAccessToken)
			.map("Bearer "::concat)
			.orElseThrow(() -> new AccessDeniedException());

      return String.format("Hello %s!", name);
    }
}
