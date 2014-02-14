package pl.ismop.web.security;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import pl.ismop.web.domain.User;
import pl.ismop.web.repository.UserRepository;

@Service
public class DatabaseUserDetailsService implements UserDetailsService {
	@Autowired UserRepository userRepository;
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findOneByEmail(username);
		
		if(user != null) {
			return new org.springframework.security.core.userdetails.User(
					user.getEmail(), user.getPasswordHash(), Arrays.asList(new GrantedAuthority[] {
						new SimpleGrantedAuthority("USER")
					}));
		} else {
			throw new UsernameNotFoundException("User " + username + " could not be found");
		}
	}
}