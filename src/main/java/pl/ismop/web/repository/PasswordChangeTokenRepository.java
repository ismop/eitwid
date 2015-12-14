package pl.ismop.web.repository;

import org.springframework.data.repository.CrudRepository;

import pl.ismop.web.domain.PasswordChangeToken;

public interface PasswordChangeTokenRepository extends CrudRepository<PasswordChangeToken, Long> {
	PasswordChangeToken findOneByToken(String token);
}
