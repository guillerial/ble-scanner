package es.indios.markn.blescanner.models.Topology;

/**
 * Created by guille on 20/03/18.
 */

public class Route {

    private String route;
    private String next;

    public Route(String route, String next) {
        this.route = route;
        this.next = next;
    }

    public String getRoute() {
        return route;
    }

    public String getNext() {
        return next;
    }
}
