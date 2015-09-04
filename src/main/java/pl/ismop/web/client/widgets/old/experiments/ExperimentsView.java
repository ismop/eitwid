package pl.ismop.web.client.widgets.old.experiments;

import pl.ismop.web.client.widgets.old.experimentitem.IExperimentItemView;
import pl.ismop.web.client.widgets.old.experiments.IExperimentsView.IExperimentsPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.mvp4g.client.view.ReverseViewInterface;

public class ExperimentsView extends Composite implements IExperimentsView, ReverseViewInterface<IExperimentsPresenter> {
	private static ExperimentsViewUiBinder uiBinder = GWT.create(ExperimentsViewUiBinder.class);
	interface ExperimentsViewUiBinder extends UiBinder<Widget, ExperimentsView> {}

	private IExperimentsPresenter presenter;
	
	@UiField HTMLPanel experimentContainer;
	@UiField ExperimentsMessages messages;	

	public ExperimentsView() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@Override
	public void showNoExperimentsMessage() {
		experimentContainer.add(new Label(messages.noExperiments()));
	}

	@Override
	public void setPresenter(IExperimentsPresenter presenter) {
		this.presenter = presenter;
	}

	@Override
	public IExperimentsPresenter getPresenter() {
		return presenter;
	}

	@Override
	public void addExperiment(IExperimentItemView view) {
		experimentContainer.add(view);
	}

	@Override
	public void clear() {
		experimentContainer.clear();
	}
	
	@Override
	protected void onDetach() {
		super.onDetach();
		getPresenter().onWidgetDetached();
	}

	@Override
	public String popupTitle() {
		return messages.popupTitle();
	}
}