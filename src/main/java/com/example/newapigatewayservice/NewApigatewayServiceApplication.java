package com.example.newapigatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class NewApigatewayServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(NewApigatewayServiceApplication.class, args);
	}

	@Bean
	public HttpExchangeRepository httpTraceRepository(){
		return new InMemoryHttpExchangeRepository();
	}

}
