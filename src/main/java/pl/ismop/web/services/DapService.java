package pl.ismop.web.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeServiceSync;
import pl.ismop.web.client.dap.levee.LeveesResponse;

@Service
public class DapService {
	private LeveeServiceSync leveeService;

	@Autowired
	public DapService(LeveeServiceSync leveeService) {
		this.leveeService = leveeService;
	}
	
	public List<Levee> getLevees() {
		LeveesResponse response = leveeService.getLevees();
		
		return response.getLevees();
	}
}