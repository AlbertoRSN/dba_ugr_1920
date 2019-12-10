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
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juan Francisco Díaz Moreno, Alicia Rodriguez
 */
public abstract class AbstractDrone extends SuperAgent {

    // Rol del agente
    private String rolname;
   
    // ID del servidor
    private AgentID server = new AgentID( "Lesath" );
    
    // Clave de sesión
    private String key;
    
    // Conversation ID
    private String convID;
    
    // Reply
    private String reply;
    
    // Variables para mensajes
    private ACLMessage outbox;
    private ACLMessage inbox;
    
    // Sensores
    private JsonObject gps;
    private double fuel;
    
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
        inicializarSensores();
    }
    
    /**
       * 
       * Función que inicializa los objetos que almacenarán los datos de los sensores
       * @Author Juan Francisco Díaz Moreno 
       * 
       */
    public void inicializarSensores() {
        
        gps = new JsonObject();
        gps.add( "x", 0 );
        gps.add( "y", 0 );
        gps.add( "z", 0 );
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
            
            convID = "CONV-" + key;
            reply = "REPLY-" + key;
            
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
    
    /**
       *
       *  Función que actualiza la percepción del drone
       *  @Author Juan Francisco Díaz Moreno
       * 
       */
    public void actualizarPercepcion() {
        
        // Envío de la solicitud
        outbox = new ACLMessage();
        outbox.setPerformative( ACLMessage.QUERY_REF );
        outbox.setSender( this.getAid() );
        outbox.addReceiver( server );
        outbox.setConversationId(key);
        outbox.setInReplyTo( reply );
        
        this.send( outbox );
        
        // Recepción del mensaje
        do {
            try {
                inbox = this.receiveACLMessage();
            } catch (InterruptedException ex) {
                Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while( inbox.getPerformativeInt() != ACLMessage.INFORM );
        
        JsonObject percepcion = ( Json.parse( inbox.getContent() ).asObject() );
        
        // GPS
        JsonObject coordenadas = percepcion.get( "gps" ).asObject();
        gps.add( "x", coordenadas.get( "x" ).asInt() );
        gps.add( "y", coordenadas.get( "y" ).asInt() );
        gps.add( "z", coordenadas.get( "z" ).asInt() );
        
        // FUEL
        fuel = percepcion.get( "fuel" ).asDouble();
        
    }
    
    @Override
    public void finalize() {
        super.finalize();
    }
    
}
