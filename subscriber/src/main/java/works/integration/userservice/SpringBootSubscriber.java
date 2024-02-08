package works.integration.userservice;

import java.text.SimpleDateFormat;
import java.util.Date;

import works.integration.userservice.entity.User;
import works.integration.userservice.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jms.annotation.JmsListener;

import java.util.Map;

@SpringBootApplication
public class SpringBootSubscriber {

	@Autowired
	UserService userService;

	public static void main(String[] args) {
		SpringApplication.run(SpringBootSubscriber.class, args);
	}

	@JmsListener(destination = "${topic.name}", containerFactory = "jmsTopicListenerContainerFactory")
	public void handle(Map<String, Object> userData) {
		Date receiveTime = new Date();

		try {
			System.out.println(
					"Message Received at " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(receiveTime)
							+ " with user data: " + userData);
			// Extract user attributes from the userData map and create a User object
			String firstName = (String) userData.get("first_name");
			String lastName = (String) userData.get("last_name");
			Integer age = (Integer) userData.get("age");
			String gender = (String) userData.get("sex");
			User user = new User(firstName, lastName, age, gender);

			// Save the received user data to the database
			userService.saveUser(user);
			System.out.println("UserData saved to the database.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
