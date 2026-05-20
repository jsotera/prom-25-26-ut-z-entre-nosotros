package edu.masanz.da.en;

public class Jugador {

    private String nombre;
    private boolean impostor;
    private boolean vivo;
    private Sala sala;

    public Jugador(String nombre) {
        this.nombre = nombre;
        this.impostor = false;
        this.vivo = true;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isImpostor() {
        return impostor;
    }

    public void setImpostor(boolean impostor) {
        this.impostor = impostor;
    }

    public boolean isVivo() {
        return vivo;
    }

    public void setVivo(boolean vivo) {
        this.vivo = vivo;
    }

    public Sala getSala() {
        return sala;
    }

    public void setSala(Sala sala) {
        this.sala = sala;
    }
}
