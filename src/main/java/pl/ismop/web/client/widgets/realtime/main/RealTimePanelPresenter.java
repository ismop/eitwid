package pl.ismop.web.client.widgets.realtime.main;

import static com.google.common.util.concurrent.Futures.addCallback;
import static com.google.common.util.concurrent.Futures.transform;
import static com.google.common.util.concurrent.Futures.transformAsync;

import java.util.List;

import javax.inject.Inject;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.mvp4g.client.annotation.Presenter;
import com.mvp4g.client.presenter.BasePresenter;

import pl.ismop.web.client.MainEventBus;
import pl.ismop.web.client.dap.DapController;
import pl.ismop.web.client.dap.device.Device;
import pl.ismop.web.client.dap.parameter.Parameter;
import pl.ismop.web.client.widgets.realtime.main.IRealTimePanelView.IRealTimePanelPresenter;

@Presenter(view = RealTimePanelView.class, multiple = true)
public class RealTimePanelPresenter extends BasePresenter<IRealTimePanelView, MainEventBus>
		implements IRealTimePanelPresenter {
	private DapController dapController;

	@Inject
	public RealTimePanelPresenter(DapController dapController) {
		this.dapController = dapController;
	}
	
	public void init() {
		// TODO Auto-generated method stub
		
	}
	
	public void onRefreshRealTimePanel() {
		ListenableFuture<List<String>> deviceIds = transform(
				dapController.getDevicesForType("weather_station"),
				new Function<List<Device>, List<String>>() {
					@Override
					public List<String> apply(List<Device> input) {
						return Lists.transform(input, new Function<Device, String>() {
							@Override
							public String apply(Device device) {
								return device.getId();
							}
						});
					}
				});
		ListenableFuture<List<Parameter>> parameters = transformAsync(deviceIds,
				new AsyncFunction<List<String>, List<Parameter>>() {
					@Override
					public ListenableFuture<List<Parameter>> apply(List<String> deviceIds) throws Exception {
						return dapController.getParameters(deviceIds);
					}
		});
		addCallback(parameters, new FutureCallback<List<Parameter>>() {
			@Override
			public void onSuccess(List<Parameter> result) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFailure(Throwable t) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}