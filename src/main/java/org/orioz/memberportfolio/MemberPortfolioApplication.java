package org.orioz.memberportfolio;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@EnableMongoAuditing
@SpringBootApplication
public class MemberPortfolioApplication {
	public static void main(String[] args) {
		SpringApplication.run(MemberPortfolioApplication.class, args);
	}
}
