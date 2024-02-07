package works.integration.userservice;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class SpringBootPublisher {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootPublisher.class, args);
	}

	@Autowired
	private JmsTemplate jmsTemplate;

	@PostConstruct
	private void customizeJmsTemplate() {
		// Update the jmsTemplate's connection factory to cache the connection
		CachingConnectionFactory ccf = new CachingConnectionFactory();
		ccf.setTargetConnectionFactory(jmsTemplate.getConnectionFactory());
		jmsTemplate.setConnectionFactory(ccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination
		jmsTemplate.setPubSubDomain(true);
	}

	@Value("${topic.name}")
	private String topicName;

	@Scheduled(fixedRate = 5000)
	public void sendEvent() throws Exception {
		Map<String, Object> user = new HashMap<>();
		user.put("firstName", "John");
		user.put("lastName", "Doe");
		user.put("age", 25);
		user.put("gender", "male");
		System.out.println("==========SENDING MESSAGE========== " + user);
		jmsTemplate.convertAndSend(topicName, user);
	}

}
