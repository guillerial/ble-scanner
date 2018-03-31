package es.indios.markn.blescanner.models.Topology;

/**
 * Created by guille on 20/03/18.
 */

public class Indication {

    private String route;
    private String indication;
    private String image_url;

    public Indication(String route, String indication, String image_url) {
        this.route = route;
        this.indication = indication;
        this.image_url = image_url;
    }

    public String getRoute() {
        return route;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getIndication() {
        return indication;
    }
}
