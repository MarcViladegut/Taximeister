package eps.udl.cat.meistertaxi.client;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.ReservationFromJSON;

import static eps.udl.cat.meistertaxi.client.CustomizeReservationActivity.BAR;
import static eps.udl.cat.meistertaxi.client.CustomizeReservationActivity.TWO_POINTS;
import static eps.udl.cat.meistertaxi.client.CustomizeReservationActivity.ZERO;

public class ReservationAdapter extends ArrayAdapter<ReservationFromJSON> {

    public ReservationAdapter(Context context, ArrayList<ReservationFromJSON> reservations) {
        super(context, 0, reservations);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        Calendar calendar = Calendar.getInstance();
        int day, month, year, hour, minute;

        // Get the data item for this position
        ReservationFromJSON reservation = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_reservation_row, parent, false);

        TextView tvId = (TextView) convertView.findViewById(R.id.idReservationValue);
        TextView tvOrigin = (TextView) convertView.findViewById(R.id.originPointReservation);
        TextView tvDestination = (TextView) convertView.findViewById(R.id.destinationPointReservation);
        TextView tvDate = (TextView) convertView.findViewById(R.id.dateReservationValue);
        TextView tvHour = (TextView) convertView.findViewById(R.id.HourReservationValue);

        tvId.setText(Integer.toString(reservation.getIdReservation()));
        tvOrigin.setText(reservation.getStartingPoint());
        tvDestination.setText(reservation.getDestinationPoint());

        Timestamp timestamp = new Timestamp(reservation.getDateTime());
        calendar.setTimeInMillis(timestamp.getTime());

        day = calendar.get(Calendar.DAY_OF_MONTH);
        month = calendar.get(Calendar.MONTH);
        year = calendar.get(Calendar.YEAR);
        minute = calendar.get(Calendar.MINUTE);
        hour = calendar.get(Calendar.HOUR_OF_DAY);

        tvDate.setText(((day > 9) ? day : ZERO + day) + BAR + (((month + 1) > 9) ? (month + 1) : ZERO + (month + 1))  + BAR + year);
        tvHour.setText(((hour < 10) ? String.valueOf(ZERO + hour) : String.valueOf(hour))
                + TWO_POINTS + ((minute < 10) ? String.valueOf(ZERO + minute) : String.valueOf(minute)));

        return convertView;
    }
}
