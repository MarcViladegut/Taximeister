package eps.udl.cat.meistertaxi;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.firebase.database.Exclude;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Locale;

import static eps.udl.cat.meistertaxi.Constants.BAR;
import static eps.udl.cat.meistertaxi.Constants.CURRENCY_DOLLAR;
import static eps.udl.cat.meistertaxi.Constants.CURRENCY_POUND;
import static eps.udl.cat.meistertaxi.Constants.PRICE_PER_KILOMETER_HIGH;
import static eps.udl.cat.meistertaxi.Constants.PRICE_PER_KILOMETER_LOW;
import static eps.udl.cat.meistertaxi.Constants.PRICE_PER_KILOMETER_MEDIUM;
import static eps.udl.cat.meistertaxi.Constants.TAX_INITIAL;
import static eps.udl.cat.meistertaxi.Constants.TWO_POINTS;
import static eps.udl.cat.meistertaxi.Constants.ZERO;

public class Reservation extends Route implements Serializable {

    private int idReservation;
    private String originLocality;
    private String destinationLocality;
    private long dateTime;
    private double cost;

    public Reservation(){ }

    public Reservation(Route route, long dataTime){
        super(route.getOrigin(), route.getDestination(), route.getDistance(), route.getDuration());
        this.dateTime = dataTime;
        this.setCost(route.getDistance());
    }

    public int getIdReservation() {
        return idReservation;
    }

    public void setIdReservation(int reservation){
        this.idReservation = reservation;
    }

    public String getOriginLocality() {
        return originLocality;
    }

    public void setOriginLocality(String originLocality) {
        this.originLocality = originLocality;
    }

    public String getDestinationLocality() {
        return destinationLocality;
    }

    public void setDestinationLocality(String destinationLocality) {
        this.destinationLocality = destinationLocality;
    }

    public void setCost(int distance){
        // tariff 1: Under 20 km
        if (this.getDistance() <= 20000)
            this.cost = PRICE_PER_KILOMETER_HIGH * distance + TAX_INITIAL;
            // tariff 2: between 20 and 100 km
        else if (this.getDistance() <= 100000)
            this.cost = PRICE_PER_KILOMETER_MEDIUM * distance + TAX_INITIAL;
            // tariff 3: More than 100 km
        else
            this.cost = PRICE_PER_KILOMETER_LOW * distance + TAX_INITIAL;
    }

    public double getCost(){
        return this.cost;
    }

    public double getCostToDollars(){
        return cost * CURRENCY_DOLLAR;
    }

    public double getCostToPounds(){
        return cost * CURRENCY_POUND;
    }

    public long getDateTime() {
        return dateTime;
    }

    public void setDateTimeFromCalendar(Calendar calendar){
        this.dateTime = calendar.getTimeInMillis();
    }

    @Exclude
    public Calendar getCalendarFromDateTime(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(this.dateTime);
        return calendar;
    }

    public String getDateToString(){
        Calendar calendar = this.getCalendarFromDateTime();
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH) + 1;
        int year = calendar.get(Calendar.YEAR);

        return ((day < 10) ? (ZERO + day) : day) + BAR +
                ((month < 10) ? (ZERO + month) : month) + BAR + year;
    }

    public String getTimeToString(){
        Calendar calendar = this.getCalendarFromDateTime();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return ((hour < 10) ? ZERO + hour : hour) + TWO_POINTS +
                ((minute < 10) ? ZERO + minute : minute);
    }

    public void setOriginToString(Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address addressFrom = new Address(Locale.getDefault());
        try {
            addressFrom = geocoder.getFromLocation(this.getOrigin().latitude,
                    this.getOrigin().longitude, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setOriginLocality(addressFrom.getLocality());
    }

    public void setDestinationToString(Context context){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        Address addressTo = new Address(Locale.getDefault());
        try {
            addressTo = geocoder.getFromLocation(this.getDestination().latitude,
                    this.getDestination().longitude, 1).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.setDestinationLocality(addressTo.getLocality());
    }
}
