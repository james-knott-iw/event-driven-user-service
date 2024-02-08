package works.integration.userservice;

import jakarta.annotation.PostConstruct;
import jakarta.jms.Connection;
import jakarta.jms.JMSException;
import jakarta.jms.MessageProducer;
import jakarta.jms.Session;
import jakarta.jms.Topic;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.jms.connection.CachingConnectionFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class SpringBootPublisher {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootPublisher.class, args);
	}

	@Autowired
	private ResourceLoader resourceLoader;
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

	@Scheduled(initialDelay = 5000)
	public void sendEvent() throws Exception {
		Resource resource = resourceLoader.getResource("classpath:MOCK_DATA.csv");
		try (CSVParser parser = CSVFormat.DEFAULT.withHeader("id", "age", "first_name", "last_name", "sex")
				.parse(new InputStreamReader(resource.getInputStream()))) {
			for (CSVRecord rec : parser) {
				// Skip the ID field
				int age = Integer.parseInt(rec.get("age"));
				String firstName = rec.get("first_name");
				String lastName = rec.get("last_name");
				String gender = rec.get("sex");

				Map<String, Object> user = new HashMap<>();
				user.put("first_name", firstName);
				user.put("last_name", lastName);
				user.put("age", age);
				user.put("sex", gender);
				System.out.println("==========SENDING MESSAGE========== " + user);
				jmsTemplate.convertAndSend(topicName, user);
			}
		}
	}

}
