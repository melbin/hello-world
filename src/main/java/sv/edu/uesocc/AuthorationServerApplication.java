package sv.edu.uesocc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import io.jaegertracing.Configuration;
import io.opentracing.Tracer;


//import brave.sampler.Sampler;

@SpringBootApplication
public class AuthorationServerApplication {

	@Bean
	public RestTemplate restTemplate(RestTemplateBuilder restTemplateBuilder) {
		return restTemplateBuilder.build();
	}
	
// For Zipkin on Docker solution
//	@Bean
//	public Sampler defaultSampler() {
//		return Sampler.ALWAYS_SAMPLE;
//	}

	/*
	 * No se si se usa unicamente en Docker (Probarlo, sino borrarlo)
	 * Para Kubernates no sirve 
	 * 	
	@Bean
	public io.opentracing.Tracer initTracer() {
		SamplerConfiguration samplerConfig = new SamplerConfiguration().withType("const").withParam(1);
		ReporterConfiguration reporterConfig = ReporterConfiguration.fromEnv().withLogSpans(true);
		return Configuration.fromEnv("hello-service").withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
	}
	 */
	
// For Kubernates Only
	
	@Bean
	  public Tracer initTracer() {
	    Configuration.SamplerConfiguration samplerConfig = (new Configuration.SamplerConfiguration()).withType("const").withParam(Integer.valueOf(1));
	    Configuration.ReporterConfiguration reporterConfig = Configuration.ReporterConfiguration.fromEnv().withLogSpans(Boolean.valueOf(true));
	    return (Tracer)Configuration.fromEnv("hello-service").withSampler(samplerConfig).withReporter(reporterConfig).getTracer();
	  }

	public static void main(String[] args) {
		SpringApplication.run(AuthorationServerApplication.class, args);
	}

}
