package pl.ismop.web.client.widgets.experimentitem;

import com.google.gwt.i18n.client.Messages;
import com.google.gwt.safehtml.shared.SafeHtml;

public interface ExperimentItemMessages extends Messages {
	String emptyDate();
	SafeHtml startDate(String date);
	SafeHtml endDate(String date);
	String resultsLabel();
	String profileIdLabel();
	String similarityLabel();
	String noResultsMessage();
}