package pl.ismop.web.client.dap.scenario;

import org.fusesource.restygwt.client.Options;
import pl.ismop.web.client.dap.DapDispatcher;

import java.util.List;

public class ScenariosResponse {
    private List<Scenario> scenarios;

    public List<Scenario> getScenarios() {
        return scenarios;
    }

    public void setScenarios(List<Scenario> scenarios) {
        this.scenarios = scenarios;
    }
}
