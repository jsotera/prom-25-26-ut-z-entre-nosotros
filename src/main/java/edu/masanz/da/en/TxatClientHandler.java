package edu.masanz.da.en;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TxatClientHandler extends Thread {

    private final Socket socket;
    private final Map<String, PrintWriter> mapaClientsWriters;
    private PrintWriter out;
    private String clientName;

    public TxatClientHandler(Socket socket, Map<String, PrintWriter> mapaClientsWriters) {
        this.socket = socket;
        this.mapaClientsWriters = mapaClientsWriters;
    }

    @Override
    public void run() {
        System.out.println("Nuevo cliente conectado: " + socket.getInetAddress());
        try (Scanner in = new Scanner(socket.getInputStream())) {
            out = new PrintWriter(socket.getOutputStream(), true);

            registerClient(in);

            while (in.hasNextLine()) {
                String message = in.nextLine();
                if (message.startsWith("/")){
                    processCommand(message);
                    continue;
                }
                String formattedMessage = clientName + ": " + message;
                System.out.println("Mensaje recibido: " + formattedMessage);
                broadcast(formattedMessage);
            }
        } catch (IOException e) {
            System.out.println("Error en la conexion con un cliente.");
        } finally {
            closeConnection();
        }
    }

    private void processCommand(String message) {
        //   /KILL javi
        //   /MOVE cocina
        //   /MAPA
        try {
            String cmd = message.split("\\s+")[0].substring(1);
            String par1 = "";
            if(message.split("\\s+").length > 1){
                par1 = message.split("\\s+")[1];
            }
            System.out.println("Comando: " + cmd);
            switch (cmd) {
                case "KILL":
                    kill(par1);
                    break;
                case "MOVE":
                    move(par1);
                    break;
                case "MAPA":
                    sendMapa();
                    break;
                case "PWD":
                    whereAmI();
                    break;
                case "ALERT":
                    alert();
                    break;
                case "VOTE":
                    vote(par1);
                    break;
            }
        } catch (Exception e) {
            out.println("MENSAJE MAL PROCESADO");
        }
    }

    private void vote(String nombreSospechoso) {
        Jugador muerto = GameManager.getInstance().vote(clientName, nombreSospechoso);
    }

    private void alert() {
        boolean exito = GameManager.getInstance().alert(clientName);
        if(exito) {
            broadcast(" NIII NOOO NIII NOOO - ALERTA - REUNION - NIII NOOO NIII NOOO ");
        } else {
            out.println(" YA ESTAS EN ALERTA ");
        }
    }

    private void kill(String nombreObjetivo) {
        if(nombreObjetivo==null || nombreObjetivo.isEmpty()){
            out.println("OBJETIVO INCORRECTO");
            return;
        }
        if(nombreObjetivo.equalsIgnoreCase(clientName)){
            out.println("NO TE PUEDES MARCAR COMO OBJETIVO");
            return;
        }

        boolean exito = GameManager.getInstance().kill(clientName, nombreObjetivo);

        if(exito){
            out.println("HAS MATADO A ["+nombreObjetivo+"]");
            PrintWriter objetivoOut = mapaClientsWriters.get(nombreObjetivo);
            objetivoOut.println("HAS SIDO ASESINADO.");
        } else {
            out.println("NO SE HA PODIDO REALIZAR LA ACCION DESEADA.");
        }
    }

    private void whereAmI() {
        //clientName
        Sala sala = GameManager.getInstance().whereIs(clientName);
        StringBuilder sb = new StringBuilder();
        out.println("*".repeat(30));
        out.printf("* %8s%-10s%8s *\n"," ", sala.getNombre(), " ");
        out.println("* "+"-".repeat(26)+" *");
        List<Jugador> jugadores = GameManager.getInstance().getMapSalasListaJugadores().get(sala);
        for (Jugador jugador : jugadores) {
            out.printf("*    %-14s: %5s   *\n", jugador.getNombre(), ""+jugador.isVivo());
        }
        out.println("*".repeat(30));
    }

    private void registerClient(Scanner in) {
        while (clientName == null && in.hasNextLine()) {
            String requestedName = in.nextLine().trim();
            synchronized (mapaClientsWriters) {
                if (!requestedName.isEmpty() && !mapaClientsWriters.containsKey(requestedName)) {
                    clientName = requestedName;
                    mapaClientsWriters.put(clientName, out);
                    System.out.println(clientName + " se ha registrado.");
                    out.println("OK");
                    broadcast(clientName + " se ha conectado.");

                    Jugador yo = GameManager.getInstance().addJugador(clientName);
                    out.println("ERES UN ["+(yo.isImpostor() ? "IMPOSTOR" : "TRIPULANTE")+"]");
                } else {
                    out.println("NO");
                    // clientName seguirá siendo null
                }
            }
        }
    }

    private void move(String nombreSalaDestino) {
        if (GameManager.getInstance().cambiaSala(clientName,nombreSalaDestino)) {
            out.println("Estás en " + nombreSalaDestino);
        }else{
            out.println("No puedes ir a " + nombreSalaDestino);
        }
    }

    private void sendMapa() {
        String mapa = GameManager.getInstance().getMapaTextual();
//        String mapa = "Este es el MAPA";
        out.println(mapa);
    }

    private void sendConnectedClients() {
        synchronized (mapaClientsWriters) {
            out.println("Clientes conectados: " + String.join(", ", mapaClientsWriters.keySet()));
        }
    }

    private void broadcast(String message) {
        synchronized (mapaClientsWriters) {
            for (PrintWriter writer : mapaClientsWriters.values()) {
                writer.println(message);
            }
        }
    }

    private void closeConnection() {
        if (clientName != null) {
            synchronized (mapaClientsWriters) {
                mapaClientsWriters.remove(clientName);
            }
            System.out.println(clientName + " se ha desconectado.");
            broadcast(clientName + " se ha desconectado.");
        }
        try {
            socket.close();
        } catch (IOException e) {
            // No hay nada mas que liberar si falla el cierre del socket.
        }
    }

}
