/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import DBA.SuperAgent;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Francisco Díaz Moreno, Alberto Rodríguez
 */
public class Interlocutor extends SuperAgent {
    
    // Mapa que vamos a usar
    String mapa;
    
    // Clave de sesión
    String key;
    
    // Variables para mensajes
    ACLMessage outbox = null;
    ACLMessage inbox    = null;
    
    // Conversation ID
    String convId = "";
    
    /**
     * 
     *  Constructor de la clase Interlocutor
     *  @author Juan Francisco Díaz Moreno, Alberto Rodríguez
     *  @param aid Identificador del agente
     *  @param map Mapa que se va a trabajar
     *  @throws Exception
     * 
     */
    public Interlocutor( AgentID aid, String mapa ) throws Exception {
        super( aid );
        this.mapa = mapa;
    }
    
    /**
     * 
     * @author Alberto Rodríguez
     * 
     */
    @Override
    public void init() {
        System.out.println( "\n\nInicializando el agente -> " + this.getName() );
    }
    
    /**
     *  
     *  @author Alberto Rodríguez
     *  
     */
    @Override
    public void execute() {
        
        // Enviar mensaje de login
        login();
        
        try {
            inbox = receiveACLMessage();
        } catch( InterruptedException ex ) {
            Logger.getLogger( Interlocutor.class.getName() ).log( Level.SEVERE, null, ex );
            System.out.println( "No se puede recibir el mensaje." );
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.AGREE )
            System.out.println( "\nSe ha cerrado sesión." );
        else
            System.out.println( "\nNo se ha cerrado sesión." );
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            
            System.out.println( "\nLogin realizado con éxito." );
            
            key = inbox.getConversationId().toString();
            System.out.println( "Recibida key: " + key );
            
            enviarKey();
            
            try {
                inbox = receiveACLMessage();
            } catch( InterruptedException ex ) {
                Logger.getLogger( Interlocutor.class.getName() ).log( Level.SEVERE, null, ex );
                System.out.println( "No se puede recibir el mensaje." );
            }
            
            if( inbox.getPerformativeInt() == ACLMessage.INFORM )
                System.out.println( "Se ha recibido la clave." );
            else
                System.out.println( "No se ha recibido la clave." );
            
        } else {
            System.out.println( "CLAVE NO RECIBIDA." );
        }
        
    }
    
    /**
     * 
     *  Envío del mensaje para hacer login
     *  @author Alberto Rodríguez
     * 
     */
    public void login() {
        
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add( "map", mapa );
        objetoJSON.add( "user", "Lackey" );
        objetoJSON.add( "password", "iVwGdxOa");
        
        String mensaje = objetoJSON.toString();
        
        /* envío */
        outbox = new ACLMessage();
        outbox.setSender( this.getAid() );
        outbox.setReceiver( new AgentID( "Lesath" ) );
        outbox.setPerformative( ACLMessage.SUBSCRIBE );
        outbox.setContent( mensaje );
        this.send( outbox );
        
    }
    
    /**
       *
       *  Envío de la clave a los drones
       *  @Author Alicia Rodríguez, Juan Francisco Díaz Moreno
       * 
       */
    public void enviarKey() {
        
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add( "key", key );
        String mensaje = objetoJSON.toString();
        
        outbox = new ACLMessage();
        outbox.setSender( this.getAid() );
        outbox.setPerformative( ACLMessage.INFORM );
        outbox.setContent( mensaje );
        
        outbox.setReceiver( new AgentID( "FLY" ) );
        this.send( outbox );
        
        outbox.setReceiver( new AgentID( "SPARROW" ) );
        this.send( outbox );
        
        outbox.setReceiver( new AgentID( "HAWK" ) );
        this.send( outbox );
        
        outbox.setReceiver( new AgentID( "RESCUE" ) );
        this.send( outbox );
        
    }
    
    /**
     * 
     *  @Author Alberto Rodríguez
     * 
     */
    @Override
    public void finalize() {
        super.finalize();
    }
    
}
