package com.izv.angel.reproductoraudio;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Principal extends Activity {

    private final int GETMUSICA=1;
    private ListView lv;
    private ArrayList<Cancion> canciones = new ArrayList<Cancion>();
    private SeekBar sb;
    private String PROGRESO = "PROGRESO";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.actividad_principal);
        lv = (ListView) findViewById(R.id.listView);
        sb = (SeekBar)findViewById(R.id.seekBar);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                una(position);
            }
        });
        sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //seek(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }
    private BroadcastReceiver receptor= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Bundle bundle = intent.getExtras();
            int posicionActual = bundle.getInt("posicionActual");
            System.out.println("posicion actual "+ posicionActual);
            int duracion = bundle.getInt("duracion");
            System.out.println("" +duracion);
            sb.setProgress(posicionActual);
            sb.setMax(duracion);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receptor, new IntentFilter(PROGRESO));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(receptor);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void play(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.PLAY);
        startService(intent);
    }

    public void stop(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.STOP);
        startService(intent);
    }

    public void pause(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.PAUSE);
        startService(intent);
    }
    public void next(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.NEXT);
        startService(intent);
    }
    public void back(View v){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.BACK);
        startService(intent);
    }
    public void una(int position){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.UNA);
        intent.putExtra("posicion",position);
        startService(intent);
    }
    public void seek(int mov){
        Intent intent = new Intent(this, ServicioAudio.class);
        intent.setAction(ServicioAudio.MOV);
        intent.putExtra("mov",mov);
        startService(intent);
    }

    public void add(View v){
        /*Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        startActivityForResult(intent, GETMUSICA);*/

        Intent intent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Audio.
                        Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, GETMUSICA);
    }

    public void pararServicio(View v){
        stopService(new Intent(this, ServicioAudio.class));

    }

    public void onActivityResult(int requestCode,int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == GETMUSICA) {
                Uri uri = data.getData();

                String ruta = getPath(uri);
                Log.v("ruta",ruta);

                Cancion cancion = new Cancion(canciones.size(),ruta);
                canciones.add(cancion);
                Adapter ad = new Adapter(this,canciones);
                lv.setAdapter(ad);

                //TextView tv = (TextView) findViewById(R.id.tvArchivo);
                //tv.setText(ruta);
                Intent intent = new Intent(this, ServicioAudio.class);
                intent.putExtra("cancion",ruta);
                intent.setAction(ServicioAudio.ADD);
                startService(intent);
            }
            if(requestCode==2){
                Uri uri = data.getData();
                String sourceFilename= uri.getPath();
                String destinationFilename = android.os.Environment.getExternalStorageDirectory().getPath()+ File.separatorChar+"record.mp3";

                BufferedInputStream bis = null;
                BufferedOutputStream bos = null;

                try {
                    bis = new BufferedInputStream(new FileInputStream(sourceFilename));
                    bos = new BufferedOutputStream(new FileOutputStream(destinationFilename, false));
                    byte[] buf = new byte[1024];
                    bis.read(buf);
                    do {
                        bos.write(buf);
                    } while(bis.read(buf) != -1);
                } catch (IOException e) {

                } finally {
                    try {
                        if (bis != null) bis.close();
                        if (bos != null) bos.close();
                    } catch (IOException e) {

                    }
                }
            }
        }
    }

    public String getPath(Uri uri) {
        Cursor cur = getContentResolver().query(uri,null, null, null, null);
        cur.moveToFirst();
        String path = cur.getString(cur.getColumnIndex(MediaStore.Audio.Media.DATA));
        cur.close();
        return path;
    }
    public void grabar(View v){
        Intent intent = new Intent(
                MediaStore.Audio.Media.
                        RECORD_SOUND_ACTION);
        startActivityForResult(intent, 2);
    }
}
