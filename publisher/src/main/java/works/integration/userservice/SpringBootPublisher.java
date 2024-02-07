package works.integration.userservice;

import jakarta.annotation.PostConstruct;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

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
	private void customizeJmsTemplate() throws JMSException {
		// Update the jmsTemplate's connection factory to cache the connection
		CachingConnectionFactory ccf = new CachingConnectionFactory();
		ccf.setTargetConnectionFactory(jmsTemplate.getConnectionFactory());
		jmsTemplate.setConnectionFactory(ccf);

		// By default Spring Integration uses Queues, but if you set this to true you
		// will send to a PubSub+ topic destination
		jmsTemplate.setPubSubDomain(true);
		// Create Solace topic
		createSolaceTopic();
	}

	private void createSolaceTopic() throws JMSException {
		Connection connection = null;
		Session session = null;
		try {
			connection = jmsTemplate.getConnectionFactory().createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Topic topic = session.createTopic(topicName);
			MessageProducer producer = session.createProducer(topic);
		} finally {
			if (session != null) {
				session.close();
			}
			if (connection != null) {
				connection.close();
			}
		}
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
