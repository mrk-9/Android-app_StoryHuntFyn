package dk.kultur.historiejagtenfyn.data.entities;

/**
 * Created by Lina on 2015.02.25
 */
public class POIConnection {

    private POIEntity source;
    private POIEntity destination;

    public POIConnection(POIEntity source, POIEntity destination) {
        this.source = source;
        this.destination = destination;
    }

    public POIEntity getSource() {
        return source;
    }

    public void setSource(POIEntity source) {
        this.source = source;
    }

    public POIEntity getDestination() {
        return destination;
    }

    public void setDestination(POIEntity destination) {
        this.destination = destination;
    }
}
