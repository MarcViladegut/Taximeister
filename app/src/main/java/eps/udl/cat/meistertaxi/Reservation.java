package eps.udl.cat.meistertaxi;

import com.google.android.gms.maps.model.LatLng;

import java.io.Serializable;
import java.util.Calendar;

public class Reservation implements Serializable {

    public static final double CURRENCY_DOLLAR = 1.12;
    public static final double CURRENCY_POUND = 0.85;

    public static final double TAX_INITIAL = 2.36;
    private static final double PRICE_PER_KILOMETER_HIGH = 0.002;
    private static final double PRICE_PER_KILOMETER_MEDIUM = 0.0015;
    private static final double PRICE_PER_KILOMETER_LOW = 0.001;

    public static int id = 0;
    private int idReservation;
    private LatLng startingPoint;
    private LatLng destinationPoint;
    private Calendar dateTime;
    private int distance;
    private String duration;
    private boolean paid;

    public Reservation(){
        idReservation = Reservation.id++;
        distance = 0;
        dateTime = Calendar.getInstance();
        paid = false;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public LatLng getStartingPoint() {
        return startingPoint;
    }

    public void setStartingPoint(LatLng startingPoint) {
        this.startingPoint = startingPoint;
    }

    public LatLng getDestinationPoint() {
        return destinationPoint;
    }

    public void setDestinationPoint(LatLng destinationPoint) {
        this.destinationPoint = destinationPoint;
    }

    public Calendar getDateTime() {
        return dateTime;
    }

    public void setDateTime(Calendar dateTime) {
        this.dateTime = dateTime;
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

    public double getCost(){
        // tariff 1: Under 20 km
        if (distance <= 20000)
            return PRICE_PER_KILOMETER_HIGH * distance + TAX_INITIAL;
        // tariff 2: between 20 and 100 km
        else if (distance <= 100000)
            return PRICE_PER_KILOMETER_MEDIUM * distance + TAX_INITIAL;
        // tariff 3: More than 100 km
        else
            return PRICE_PER_KILOMETER_LOW * distance + TAX_INITIAL;
    }

    public double getCostToDollars(){
        return getCost() * CURRENCY_DOLLAR;
    }

    public double getCostToPounds(){
        return getCost() * CURRENCY_POUND;
    }

    public boolean isPaid(){
        return paid;
    }

    public void setPaid(boolean paid){
        this.paid = paid;
    }
}
