package eps.udl.cat.meistertaxi;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.Locale;

public class ReservationFromJSON {

    private int idReservation;
    private String startingPoint;
    private String destinationPoint;
    private long dateTime;

    public ReservationFromJSON(){ }

    public ReservationFromJSON(int idReservation, String startingPoint, String destinationPoint, long dateTime){
        this.idReservation = idReservation;
        this.startingPoint = startingPoint;
        this.destinationPoint = destinationPoint;
        this.dateTime = dateTime;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int idReservation) {
        this.idReservation = idReservation;
    }

    public String getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(String startingPoint) {
        this.startingPoint = startingPoint;
    }

    public String getDestinationPoint() {
        return destinationPoint;
    }

    public void setDestinationPoint(String destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTime(long dateTime) {
        this.dateTime = dateTime;
    }

    public ReservationFromJSON fromReservation(Context context, Reservation reservation){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address addressFrom = new Address(Locale.getDefault());
        Address addressTo = new Address(Locale.getDefault());
        try {
            addressFrom = geocoder.getFromLocation(reservation.getStartingPoint().latitude,
                    reservation.getStartingPoint().longitude, 1).get(0);

            addressTo = geocoder.getFromLocation(reservation.getDestinationPoint().latitude,
                    reservation.getDestinationPoint().longitude, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new ReservationFromJSON(reservation.getIdReservation(),
                addressFrom.getLocality(),
                addressTo.getLocality(),
                reservation.getDateTime().getTimeInMillis());
    }
}
