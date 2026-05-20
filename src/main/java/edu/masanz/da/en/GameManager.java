package edu.masanz.da.en;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GameManager {

    private static final GameManager INSTANCE = new GameManager();

    private boolean alarma = false;

    private Map<String, Sala> mapSalas =  new TreeMap<>();
    private Map<String, Jugador> mapJugadores =  new TreeMap<>();
    private Map<Sala, List<Jugador>> mapSalasListaJugadores =  new TreeMap<>();
    // MAPA QUIEN VOTA A QUIEN PARA MATAR

    private String mapaTextual = """
[cocina] - [pasillo] - [dormitorio]
   |           |             |
   L-------[despensa]--------J
""";

    private GameManager() {
        initMapSalas();
        initMapSalasListaJugadores();
    }

    private void initMapSalas() {

        Sala cocina = new Sala("cocina");
        Sala pasillo = new Sala("pasillo");
        Sala dormitorio = new Sala("dormitorio");
        Sala despensa = new Sala("despensa");

        cocina.addSalaAdyacente(pasillo);
        cocina.addSalaAdyacente(despensa);

        pasillo.addSalaAdyacente(cocina);
        pasillo.addSalaAdyacente(dormitorio);
        pasillo.addSalaAdyacente(despensa);

        dormitorio.addSalaAdyacente(pasillo);
        dormitorio.addSalaAdyacente(despensa);

        despensa.addSalaAdyacente(cocina);
        despensa.addSalaAdyacente(pasillo);
        despensa.addSalaAdyacente(dormitorio);

        mapSalas.put("cocina", cocina);
        mapSalas.put("pasillo", pasillo);
        mapSalas.put("dormitorio", dormitorio);
        mapSalas.put("despensa", despensa);
    }

    private void initMapSalasListaJugadores() {
        mapSalasListaJugadores.put(mapSalas.get("cocina"), new ArrayList<>());
        mapSalasListaJugadores.put(mapSalas.get("pasillo"), new ArrayList<>());
        mapSalasListaJugadores.put(mapSalas.get("dormitorio"), new ArrayList<>());
        mapSalasListaJugadores.put(mapSalas.get("despensa"), new ArrayList<>());
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public String getMapaTextual() {
        return mapaTextual;
    }

    public Jugador addJugador(String clientName) {

        Jugador jugador = new Jugador(clientName);

        if (Math.random() < 0.3) {
            jugador.setImpostor(true);
        }

        // Meterlo en el mapa jugadores
        mapJugadores.put(clientName, jugador);

        // Elegir sala en la que aparece
        int i = (int) (Math.random() * mapSalas.size());
        Sala sala = (new ArrayList<>(mapSalas.values())).get(i);

        // Poner la sala en la que está al jugador
        jugador.setSala(sala);

        // Actualizar el mapa de salas y lista jugadores
        mapSalasListaJugadores.get(sala).add(jugador);

        return jugador;
    }

    public boolean puedeIr(String clientName, String nombreSalaDestino) {
        // TODO
        return true;
    }

    public boolean cambiaSala(String nombreJugador, String nombreSalaDestino) {
        Jugador jugador = mapJugadores.get(nombreJugador);
        // Si está muerto --> false
        if (jugador == null) { return false; }
        if (!jugador.isVivo()) { return false; }
        Sala salaDestino = mapSalas.get(nombreSalaDestino);
        // Si la sala no existe --> false
        if (salaDestino == null) { return false; }
        // Si la sala no es accesible --> false
        Sala salaJugador = jugador.getSala();
        if (!salaJugador.isAdyacente(salaDestino)) { return false; }
        // Actualizar sala del jugador
        jugador.setSala(salaDestino);
        // Actualizar mapSalasListaJugadores
        mapSalasListaJugadores.get(salaJugador).remove(jugador);
        mapSalasListaJugadores.get(salaDestino).add(jugador);
        return true;
    }

    public Sala whereIs(String nombreJugador) {
        if(mapJugadores.containsKey(nombreJugador)){
            return mapJugadores.get(nombreJugador).getSala();
        }
        return null;
    }

    public Map<Sala, List<Jugador>> getMapSalasListaJugadores() {
        return mapSalasListaJugadores;
    }

    public boolean kill(String nombreAsesino, String nombreObjetivo) {
        Jugador asesino = mapJugadores.get(nombreAsesino);
        Jugador objetivo = mapJugadores.get(nombreObjetivo);
        if(asesino == null || objetivo == null){
            return false;
        }
        if(!asesino.getSala().equals(objetivo.getSala())){
            return false;
        }
        if(!asesino.isImpostor() || objetivo.isImpostor()){
            return false;
        }
        if(!asesino.isVivo() || !objetivo.isVivo()){
            return false;
        }
        objetivo.setVivo(false);
        return true;
    }

    public boolean alert(String nombreJugador) {
        if(alarma){
            return false;
        }

        alarma = true;
        return alarma;
    }

    public Jugador vote(String nombreJugador, String nombreSospechoso) {
        // TODO
        return null;
    }
}
