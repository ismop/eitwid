package pl.ismop.web.client.dap;

import java.util.List;

import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;

import pl.ismop.web.client.dap.levee.Levee;
import pl.ismop.web.client.dap.levee.LeveeResponse;
import pl.ismop.web.client.dap.levee.LeveeService;
import pl.ismop.web.client.dap.levee.LeveesResponse;
import pl.ismop.web.client.dap.levee.ModeChange;
import pl.ismop.web.client.dap.levee.ModeChangeRequest;

import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DapController {
	private LeveeService leveeService;
	
	public interface ErrorCallback {
		void onError(int code, String message);
	}
	
	public interface LeveesCallback extends ErrorCallback {
		void processLevees(List<Levee> levees);
	}
	
	public interface LeveeModeChangedCallback extends ErrorCallback {
		void processLevee(Levee levee);
	}

	@Inject
	public DapController(LeveeService leveeService) {
		this.leveeService = leveeService;
	}
	
	public void getLevees(final LeveesCallback callback) {	
		leveeService.getLevees(new MethodCallback<LeveesResponse>() {
			@Override
			public void onSuccess(Method method, LeveesResponse response) {
				callback.processLevees(response.getLevees());
			}
			
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}
		});
	}

	public void changeLeveeMode(String leveeId, String mode, final LeveeModeChangedCallback callback) {
		ModeChange modeChange = new ModeChange();
		modeChange.setMode(mode);
		
		ModeChangeRequest request = new ModeChangeRequest();
		request.setModeChange(modeChange);
		leveeService.changeMode(leveeId, request, new MethodCallback<LeveeResponse>() {
			@Override
			public void onFailure(Method method, Throwable exception) {
				callback.onError(0, exception.getMessage());
			}

			@Override
			public void onSuccess(Method method, LeveeResponse response) {
				callback.processLevee(response.getLevee());
			}});
	}
}