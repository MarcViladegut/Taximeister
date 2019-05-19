package eps.udl.cat.meistertaxi.ClientApp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import eps.udl.cat.meistertaxi.R;
import eps.udl.cat.meistertaxi.Reservation;

public class ReservationAdapter extends ArrayAdapter<Reservation> {

    public ReservationAdapter(Context context, ArrayList<Reservation> reservations) {
        super(context, 0, reservations);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View getView(int position, View convertView, ViewGroup parent){

        // Get the data item for this position
        Reservation reservation = getItem(position);

        if (convertView == null)
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.activity_reservation_row, parent, false);

        TextView tvId = (TextView) convertView.findViewById(R.id.idReservationValue);
        TextView tvOrigin = (TextView) convertView.findViewById(R.id.originPointReservation);
        TextView tvDestination = (TextView) convertView.findViewById(R.id.destinationPointReservation);
        TextView tvDate = (TextView) convertView.findViewById(R.id.dateReservationValue);
        TextView tvHour = (TextView) convertView.findViewById(R.id.HourReservationValue);

        tvId.setText(Integer.toString(reservation.getIdReservation()));
        tvOrigin.setText(reservation.getOriginLocality());
        tvDestination.setText(reservation.getDestinationLocality());

        tvDate.setText(reservation.getDateToString());
        tvHour.setText(reservation.getTimeToString());

        return convertView;
    }
}
