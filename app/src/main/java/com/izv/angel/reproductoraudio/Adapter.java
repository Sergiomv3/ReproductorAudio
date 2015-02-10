package com.izv.angel.reproductoraudio;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Sergio on 10/02/2015.
 */
public class Adapter extends ArrayAdapter{
    ArrayList <Cancion>datos = new ArrayList<Cancion>();
    public Adapter(Context context, ArrayList<Cancion> canciones) {
        super(context, 0, canciones);
        this.datos = canciones;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.detallelista, parent, false);
        }
        // Lookup view for data population
        TextView num = (TextView) convertView.findViewById(R.id.textView);
        TextView ruta = (TextView) convertView.findViewById(R.id.textView2);
        // Populate the data into the template view using the data object
        num.setText(String.valueOf(datos.get(position).getNum())+" - ");
        ruta.setText(datos.get(position).getRuta());
        // Return the completed view to render on screen
        return convertView;
    }
}
