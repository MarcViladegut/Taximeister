package eps.udl.cat.meistertaxi;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Calendar;

public class Reservation implements Serializable {

    private static final double CURRENCY_DOLLAR = 1.12;
    private static final double CURRENCY_POUND = 0.85;

    private static final double TAX_INITIAL = 2.36;
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
    private long dateTimeLong;

    public Reservation(){
        idReservation = Reservation.id++;
        distance = 0;
        dateTime = Calendar.getInstance();
        this.paid = false;
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int reservation){
        this.idReservation = reservation;
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

    @Exclude
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

    @Exclude
    public double getCostToDollars(){
        return getCost() * CURRENCY_DOLLAR;
    }

    @Exclude
    public double getCostToPounds(){
        return getCost() * CURRENCY_POUND;
    }

    public boolean isPaid(){
        return paid;
    }

    public void setPaid(boolean paid){
        this.paid = paid;
    }

    public long getDateTimeLong(){
        return dateTimeLong;
    }

    public void setDateTimeLong(Calendar calendar){
        Timestamp timestamp = new Timestamp(calendar.getTimeInMillis());
        dateTimeLong = timestamp.getTime();
    }

    public long dateToTimeStamp(){
        Timestamp timestamp = new Timestamp(dateTime.getTimeInMillis());
        return timestamp.getTime();
    }

    @Exclude
    public Calendar timeStampToDate(Timestamp timestamp){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp.getTime());
        return calendar;
    }
}
