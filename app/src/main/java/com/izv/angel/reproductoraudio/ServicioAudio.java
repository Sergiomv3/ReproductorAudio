package com.izv.angel.reproductoraudio;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class ServicioAudio extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, AudioManager.OnAudioFocusChangeListener{

    // aleatoria
    // bucle
    // seekbar
    // previous next

    private MediaPlayer mp;
    private enum Estados{
        idle,
        initialized,
        prepairing,
        prepared,
        started,
        paused,
        completed,
        sttoped,
        end,
        error
    };
    private Estados estado;
    public static final String PLAY="play";
    public static final String STOP="stop";
    public static final String ADD="add";
    public static final String PAUSE="pause";
    public static final String NEXT="next";
    public static final String BACK="back";
    public static final String UNA ="UNA";
    public static final String MOV ="MOV";
    private String rutaCancion=null;
    private ArrayList<String> canciones;
    private boolean reproducir;
    private int cont = 0;
    private int actual = 0;
    private int duracion = 0;
    private String PROGRESO = "PROGRESO";
    /* ******************************************************* */
    // METODOS SOBREESCRITOS //
    /* ****************************************************** */

    @Override
    public void onCreate() {
        super.onCreate();
        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        canciones = new ArrayList<String>();
        int r = am.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        if(r==AudioManager.AUDIOFOCUS_REQUEST_GRANTED){
            // normal
            mp = new MediaPlayer();
            mp.setOnPreparedListener(this);
            mp.setOnCompletionListener(this);
            mp.setWakeMode(this,PowerManager.PARTIAL_WAKE_LOCK);
            estado = Estados.idle;
        } else {
            stopSelf();
        }

    }

    @Override
    public void onDestroy() {
        //mp.reset();
        mp.release();
        mp = null;
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent.getAction()!= null) {
            String action = intent.getAction();
            String dato = intent.getStringExtra("cancion");
            if (action.equals(PLAY)) {
                play();
            } else if (action.equals(ADD)) {
                add(dato);
            } else if (action.equals(STOP)) {
                stop();
            } else if (action.equals(PAUSE)) {
                pause();
            } else if (action.equals(NEXT)){
                next();
            } else if (action.equals(BACK)){
                back();
            } else if(action.equals(UNA)){
                una(intent.getIntExtra("posicion",0));
            } else if(action.equals(MOV)){
                mp.seekTo(intent.getIntExtra("mov",0));
            }
        }
        return super.onStartCommand(intent, flags, startId);

    }

    /* ******************************************************* */
    // INTERFAZ PREPARED LISTENER //
    /* ****************************************************** */

    @Override
    public void onPrepared(MediaPlayer mp) {
        estado = Estados.prepared;
        if(reproducir){
            mp.start();
            estado = Estados.started;

        }
        new Progreso().execute();
    }

    /* ******************************************************* */
    // INTERFAZ COMPLETED LISTENER //
    /* ****************************************************** */

    @Override
    public void onCompletion(MediaPlayer mp) {
        System.out.println("termino");
        estado = Estados.completed;
        // pase a la siguiente cancion
        //mp.stop();
        //estado = Estados.sttoped;
       next();

    }

    /* ******************************************************* */
    // INTERFAZ AUDIO FOCUS CHANGED //
    /* ****************************************************** */

    @Override
    public void onAudioFocusChange(int focusChange) {
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                play();
                mp.setVolume(1.0f, 1.0f);
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mp.setVolume(0.1f, 0.1f);
                break;
        }
    }

    /* ******************************************************* */
    // METODOS DE AUDIO //
    /* ****************************************************** */

    private void play(){
        if(rutaCancion != null){
            if(estado == Estados.error){
                estado = Estados.idle;
            }
            if(estado == Estados.idle){
                reproducir = true;
                try {
                    mp.setDataSource(rutaCancion);

                    estado = Estados.initialized;
                } catch (IOException e) {
                    estado= Estados.error;
                }
            }
            if(estado == Estados.initialized ||
                    estado == Estados.sttoped){
                reproducir = true;
                mp.prepareAsync();
                estado = Estados.prepairing;
            } else if(estado == Estados.prepairing) {
                reproducir = true;
            }
            if(estado == Estados.prepared ||
                    estado == Estados.paused ||
                    estado == Estados.completed ||
                    estado == Estados.started) {
                mp.start();
                estado = Estados.started;
            }
        }
    }

    private void stop(){
        if(estado == Estados.prepared ||
                estado == Estados.started ||
                estado == Estados.paused ||
                estado == Estados.completed){
            mp.seekTo(0); // Para volver al principio sino comentar para pause
            mp.stop();
            estado = Estados.sttoped;
        }
        reproducir = false;
    }

    private void pause() {
        if(estado == Estados.paused) {
            mp.pause();
            estado = Estados.paused;
        }
    }

    private void add(String cancion){
        canciones.add(cancion);
        this.rutaCancion = canciones.get(0);
        Log.v("ADD",cancion);
    }
    private void next(){

        mp.reset();
        estado = Estados.idle;
        cont++;
        if(cont  < canciones.size()) {
            rutaCancion = canciones.get(cont);
            play();
        }else{
            Toast.makeText(this,"Empieza lista de nuevo",Toast.LENGTH_SHORT);
            cont = 0;
            rutaCancion = canciones.get(cont);
            play();
        }
    }
    private void back(){
        mp.reset();
        estado = Estados.idle;
        cont--;
        if(cont  < canciones.size() && cont > -1) {
            rutaCancion = canciones.get(cont);
            play();
        }else{
            //Toast.makeText(this,"Empieza lista de nuevo",Toast.LENGTH_SHORT);
            cont = canciones.size()-1;
            rutaCancion = canciones.get(cont);
            play();
        }
    }
    private void una(int position){
        mp.reset();
        estado = Estados.idle;
        cont = position;
        System.out.println("la posicion es "+position);
        rutaCancion = canciones.get(cont);
        play();
    }
    private class Progreso extends AsyncTask<Void, Integer, Integer> {

        @Override
        protected Integer doInBackground(Void... voids) {
            while(mp.getDuration() > mp.getCurrentPosition()){
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.v("voy por", mp.getCurrentPosition()+"");
                progreso(mp.getCurrentPosition());
                duracion = mp.getDuration();

            }

            return mp.getDuration();
        }
    }
    private void progreso(int p){
        Intent intent = new Intent(PROGRESO);
        intent.putExtra("posicionActual", p);
        intent.putExtra("duracion", duracion);
        sendBroadcast(intent);
    }
}