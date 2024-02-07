package works.integration.userservice.repository;

import org.springframework.data.repository.CrudRepository;

import works.integration.userservice.entity.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
