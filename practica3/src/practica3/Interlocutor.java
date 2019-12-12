/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import DBA.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal para elinterlocutor que se comunica con el servidor
 * 
 * @author Juan Francisco Diaz Moreno, Alberto Rodriguez, Ana Rodriguez
 */
public class Interlocutor extends SuperAgent {
    
    //Mapa que vamos a usar
    String mapa;
    //Clave de sesion
    String key;
    //Variables para mensajes
    ACLMessage outbox = null;
    ACLMessage inbox = null;
    
    //Conversation ID
    String convId = "";
    
    /**
     *  Constructor de la clase Interlocutor
     * 
     *  @param aid Identificador del agente
     *  @param mapa Mapa que se va a trabajar
     *  @throws Exception
     * 
     *  @author Juan Francisco Diaz Moreno, Alberto Rodriguez
     */
    public Interlocutor(AgentID aid, String mapa) throws Exception {
        super(aid);
        this.mapa = mapa;
    }
    
    @Override
    public void init() {
        System.out.println("\n\nInicializando el agente -> " + this.getName() );
    }
    
    /**
     *  @author Alberto Rodriguez
     */
    @Override
    public void execute() {
        
        //Enviar mensaje de login
        login();
        
        try {
            inbox = receiveACLMessage();
        } catch(InterruptedException ex) {
            Logger.getLogger(Interlocutor.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se puede recibir el mensaje.");
        }
        
        if(inbox.getPerformativeInt() == ACLMessage.AGREE)
            System.out.println("\nSe ha cerrado sesion.");
        else
            System.out.println("\nNo se ha cerrado sesion.");
        
        if(inbox.getPerformativeInt() == ACLMessage.INFORM) {
            System.out.println("\nLogin realizado con exito.");
            
            key = inbox.getConversationId();
            System.out.println("Recibida key: " + key);
            enviarKey();
            
            try {
                inbox = receiveACLMessage();
            } catch(InterruptedException ex) {
                Logger.getLogger(Interlocutor.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("No se puede recibir el mensaje.");
            }
            
            if(inbox.getPerformativeInt() == ACLMessage.INFORM)
                System.out.println("Se ha recibido la clave.");
            else
                System.out.println("No se ha recibido la clave.");
            
        } else {
            System.out.println("CLAVE NO RECIBIDA.");
        }
    }
    
    /**
     *  Envio del mensaje para hacer login
     * 
     *  @author Alberto Rodriguez
     */
    public void login() {
        
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add("map", mapa);
        objetoJSON.add("user", "Lackey");
        objetoJSON.add("password","iVwGdxOa");
        
        String mensaje = objetoJSON.toString();
        
        //Envio
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Lesath"));
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setContent(mensaje);
        this.send(outbox);
    }
    
    /**
     *  Envio de la clave a los drones
     * 
     *  @author Alicia Rodriguez, Juan Francisco Diaz Moreno
     */
    public void enviarKey() {
        
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add("key", key);
        String mensaje = objetoJSON.toString();
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setPerformative(ACLMessage.INFORM);
        outbox.setContent(mensaje);
        
        outbox.setReceiver(new AgentID("FLY"));
        this.send(outbox);
        
        outbox.setReceiver(new AgentID("SPARROW"));
        this.send(outbox);
        
        outbox.setReceiver(new AgentID("HAWK"));
        this.send(outbox);
        
        outbox.setReceiver(new AgentID("RESCUE"));
        this.send(outbox);
    }
    
     /**
     * Crea una imagen a partir de la traza recibida
     * @author Ana Rodriguez
     * @param jsObjeto nombre del objeto con la traza
     */
    public void crearTraza(JsonObject jsObjeto){
        try{
            System.out.println("\nRecibiendo traza");
            JsonArray ja = jsObjeto.get("trace").asArray();
            byte data[] = new byte[ja.size()];
            for(int i=0; i<data.length; i++){
                data[i] = (byte) ja.get(i).asInt();
            }
            try (FileOutputStream fos = new FileOutputStream(mapa+".png")) {
                fos.write(data);
            }
            System.out.println("¡Bravo! Traza guardada :)");
        }catch (IOException ex){
            System.err.println("Error al procesar la traza");
        }
    }
    
    @Override
    public void finalize() {
        super.finalize();
    }   
}