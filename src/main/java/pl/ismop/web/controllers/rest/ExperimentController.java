package pl.ismop.web.controllers.rest;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PUT;

import java.util.stream.Collectors;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import pl.ismop.web.client.hypgen.Experiment;
import pl.ismop.web.client.model.UserExperiments;
import pl.ismop.web.domain.User;
import pl.ismop.web.repository.ExperimentRepository;
import pl.ismop.web.repository.UserRepository;

public class ExperimentController {
	@Autowired private UserRepository userRepository;
	@Autowired private ExperimentRepository experimentRepository;
	
	@RequestMapping(value = "experiments", method = PUT)
	public void addUserExperiment(@RequestBody Experiment experiment, HttpServletResponse response) {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findOneByEmail(email);
		
		if(user != null) {
			pl.ismop.web.domain.Experiment dbExperiment = new pl.ismop.web.domain.Experiment();
			dbExperiment.setNativeId(experiment.getId());
			experimentRepository.save(dbExperiment);
			user.getExperiments().add(dbExperiment);
			userRepository.save(user);
			response.setStatus(HttpServletResponse.SC_OK);
		} else {
			response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@RequestMapping(value = "experiments", method = GET)
	public ResponseEntity<UserExperiments> getUserExperiments() {
		String email = SecurityContextHolder.getContext().getAuthentication().getName();
		User user = userRepository.findOneByEmail(email);
		
		if(user != null) {
			UserExperiments result = new UserExperiments();
			result.setUserLogin(email);
			result.setExperimentIds(user.getExperiments()
					.stream()
					.map(e -> {
						return e.getNativeId();
					})
					.collect(Collectors.toList()));
			
			return new ResponseEntity<UserExperiments>(result, HttpStatus.OK);
		} else {
			return new ResponseEntity<UserExperiments>(HttpStatus.NOT_FOUND);
		}
	}
}