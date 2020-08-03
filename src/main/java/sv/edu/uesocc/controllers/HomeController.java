package sv.edu.uesocc.controllers;

import java.sql.SQLDataException;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import sv.edu.uesocc.dto.Person;

@RestController
@RequestMapping("/hello-world/v1.0.0")
public class HomeController {
	
	private static Logger logger = LogManager.getLogger(HomeController.class);
	
	//HTTP port
	@Value("${spring.profiles.active}")
	private String profile;
	
	@Value("${db.password}")
	private String password;
	
    @Autowired
    private RestTemplate restTemplate;
	
	@RequestMapping(value = "", method = RequestMethod.GET)
	public String hi() {
		logger.info("Default GET method, this will be printed on console");
		try {
			throw new SQLDataException();
		}catch(Exception e) {
			e.printStackTrace();
			logger.error("SQL Error: "+e.getMessage());
		}
		return "Welcome to the microservices world!!!! Profile: "+profile+" | pass: "+password;
	}
	
	@RequestMapping(value = "/test", method = RequestMethod.GET)
	public String test() {
		logger.info("Test Method was called");
		return "v1.2.12";
	}
	
	@RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json")
	@ResponseBody
	public String hi2(@RequestBody Person person) {
		logger.info("Defaut POST method!!!");
		logger.info("Person: "+person.toString());
		ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/hello-world/v1.0.0/chaining", String.class);
		return "Welcome "+person.getName();
	}
	
    @RequestMapping("/chaining")
    public String chaining() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:8080/hello-world/v1.0.0", String.class);
        return "Chaining + " + response.getBody();
    }
    
	@RequestMapping(value = "/error", method = RequestMethod.GET)
	public String generateError() throws SQLDataException {
		
		throw new SQLDataException();
		
	}
	
}
