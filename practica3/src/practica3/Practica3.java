/*
 * Practica 3
 * Grupo L
 */
package practica3;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 * Clase principal de la practica 3
 * 
 * @author Alberto Rodriguez, Juan Francisco Diaz Moreno, Alicia Rodriguez
 */
public class Practica3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        String mapa = "playground";
        
        AgentsConnection.connect(
            "isg2.ugr.es",  //host
            6000,           //puerto
            "Practica3",
            "guest",        //username
            "guest",        //password
            false           //SSL
        );
        
        Interlocutor interlocutor;
        DroneBuscador fly;
        DroneBuscador sparrow;
        DroneBuscador hawk;
        DroneRescue rescue;
        
        try {
            interlocutor = new Interlocutor(new AgentID("Interlocutor-GLLL"), mapa);
            fly = new DroneBuscador(new AgentID("FLYii"), mapa );
            sparrow = new DroneBuscador(new AgentID("SPARROWii"), mapa );
            hawk = new DroneBuscador(new AgentID("HAWKii"), mapa );
            rescue = new DroneRescue(new AgentID("RESCUEii"), mapa );
        } catch(Exception e) {
            System.out.println("\n\nERROR. El agente ya existe en la plataforma.");
            return;
        }
        
        //Lanzamos los agentes
        interlocutor.start();
        fly.start();
        sparrow.start();
        hawk.start();
        rescue.start();
    }   
}