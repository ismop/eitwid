package pl.ismop.web.client.dap.experiment;

import pl.ismop.web.domain.*;

import java.util.List;

/**
 * Created by marek on 05.10.15.
 */
public class ExperimentsResponse {
    private List<Experiment> experiments;

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }
}
