package eps.udl.cat.meistertaxi;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

public class Route {

    private LatLng origin;
    private LatLng destination;
    private int distance;
    private String duration;

    public Route(LatLng origin, LatLng destination, int distance, String duration){
        this.origin = origin;
        this.destination = destination;
        this.distance = distance;
        this.duration = duration;
    }

    public Route(){ }

    @Exclude
    public LatLng getOrigin() {
        return origin;
    }

    public void setOrigin(LatLng origin) {
        this.origin = origin;
    }

    @Exclude
    public LatLng getDestination() {
        return destination;
    }

    public void setDestination(LatLng destination) {
        this.destination = destination;
    }

    public int getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
