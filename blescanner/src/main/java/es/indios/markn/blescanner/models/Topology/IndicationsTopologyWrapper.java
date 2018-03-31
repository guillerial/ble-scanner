package es.indios.markn.blescanner.models.Topology;

import java.util.List;

public class IndicationsTopologyWrapper {

    public List<Indication> mIndications;
    public List<Route> mRoutes;

    public IndicationsTopologyWrapper(List<Indication> mIndications, List<Route> mRoutes) {
        this.mIndications = mIndications;
        this.mRoutes = mRoutes;
    }


}
