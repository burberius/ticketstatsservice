package net.troja.trial.ticketstats;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import springfox.documentation.swagger2.annotations.EnableSwagger2WebMvc;

@SpringBootApplication
@EnableSwagger2WebMvc
public class TicketstatsApplication {
	public static void main(String[] args) {
		SpringApplication.run(TicketstatsApplication.class, args);
	}
}