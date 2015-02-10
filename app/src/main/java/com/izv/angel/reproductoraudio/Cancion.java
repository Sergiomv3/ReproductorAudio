package com.izv.angel.reproductoraudio;

/**
 * Created by Sergio on 10/02/2015.
 */
public class Cancion {
    private int num;
    private String ruta;

    public Cancion(int num, String ruta) {
        this.num = num;
        this.ruta = ruta;
    }

    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String ruta) {
        this.ruta = ruta;
    }
}
