/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import DBA.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Francisco Díaz Moreno, Alicia Rodriguez
 */
public abstract class AbstractDrone extends SuperAgent {

    // Rol del agente
    String rolname;
    
    // Clave de sesión
    String key;
    
    // Conversation ID
    String convID;
    
    // Variables para mensajes
    ACLMessage outbox;
    ACLMessage inbox;
    
    /**
       *
       * Crea un nuevo Agente
       * @param aid ID del agente
       * @throws Exception
       * 
       * @Author Alicia Rodríguez
       * 
       */
    public AbstractDrone( AgentID aid ) throws Exception {
        super( aid );
        rolname = this.getAid().name;
    }
    
    @Override
    public void init() {
        System.out.println( "\n\nInicializando el drone -> " + this.getName() );
    }
    
    /**
       *
       *  Función principal del drone
       *  @Author Juan Francisco Díaz Moreno
       * 
       */
    public void execute() {
        
        try {
            inbox = receiveACLMessage();
        } catch( InterruptedException ex ) {
            Logger.getLogger( AbstractDrone.class.getName() ).log( Level.SEVERE, null, ex );
            System.out.println( "No se puede recibir el mensaje" );
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            System.out.println( "\nRegistro realizado con éxito para: " + this.rolname );
            JsonObject objeto = Json.parse( this.inbox.getContent() ).asObject();
            key = objeto.get( "key" ).asString();
            System.out.println( "key -> " + key );
            
            enviarOK();
        }

    }
    
    /**
       *
       * @Author Alicia Rodríguez
       * 
       */
    public void enviarOK() {
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add( "result", "ok" );
        
        String mensaje = objetoJSON.toString();
        
        outbox = new ACLMessage();
        outbox.setSender( this.getAid() );
        outbox.setReceiver( new AgentID( "InterlocutorGL" ) );
        outbox.setPerformative( ACLMessage.INFORM );
        outbox.setContent( mensaje );
        this.send( outbox );
    }
    
    /**
       *
       *  Bucle con la actuación de los drones
       *  @Author Juan Francisco Díaz Morneo
       * 
       */
    public abstract void actuacion();
    
    @Override
    public void finalize() {
        super.finalize();
    }
    
}
