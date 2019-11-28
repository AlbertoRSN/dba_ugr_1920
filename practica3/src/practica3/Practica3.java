/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author Juan Francisco Díaz Moreno
 */
public class Practica3 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        AgentsConnection.connect(
            "isg2.ugr.es",
             6000,
             "Practica3",
             "guest",
             "guest",
             false
        );
        
        Interlocutor interlocutor;
        
        try {
            interlocutor = new Interlocutor( new AgentID( "InterlocutorGL" ) );
        } catch( Exception e ) {
            System.out.println( "El agente ya existe en la plataforma." );
            return;
        }
        
        interlocutor.start();
        
    }
    
}
