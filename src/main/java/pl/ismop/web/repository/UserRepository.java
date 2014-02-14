package pl.ismop.web.repository;

import org.springframework.data.repository.CrudRepository;

import pl.ismop.web.domain.User;

public interface UserRepository extends CrudRepository<User, Long> {
	User findOneByEmail(String email);
}