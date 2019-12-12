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
 * Clase principal abstracta para los drones que usaremos
 * 
 * @author Juan Francisco Diaz Moreno, Alicia Rodriguez, Ana Rodriguez
 */
public abstract class AbstractDrone extends SuperAgent {

    //Rol del agente
    private String rolname;
    //ID del servidor
    private AgentID server = new AgentID("Lesath");
    //Clave de sesión
    private String key;
    //Conversation ID
    private String convID;
    //Reply
    private String reply;
    
    //Variables para mensajes
    private ACLMessage outbox;
    private ACLMessage inbox;
    
    // Sensores
    private JsonObject gps;
    private double fuel;
    
    /**
     * Constructor de la clase principal. Crea un nuevo Agente
     * @param aid ID del agente
     * @throws Exception
     * 
     * @author Alicia Rodriguez 
     */
    public AbstractDrone(AgentID aid) throws Exception {
        super(aid);
        rolname = this.getAid().name;
        inicializarSensores();
    }
    
    /**
     * Funcion que inicializa los objetos que almacenaran los datos de los sensores
     * 
     * @Author Juan Francisco Diaz Moreno 
     */
    public void inicializarSensores() {
        gps = new JsonObject();
        gps.add("x", 0);
        gps.add("y", 0);
        gps.add("z", 0);
    }
    
    @Override
    public void init() {
        System.out.println("\n\nInicializando el drone -> " + this.getName() );
    }
    
    /**
     *  Funcion principal del drone
     * 
     *  @author Juan Francisco Diaz Moreno
     */
    public void execute() {
        try {
            inbox = receiveACLMessage();
        } catch(InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se puede recibir el mensaje");
        }
        
        if(inbox.getPerformativeInt() == ACLMessage.INFORM) {
            System.out.println("\nRegistro realizado con éxito para: " + this.rolname);
            JsonObject objeto = Json.parse(this.inbox.getContent()).asObject();
            key = objeto.get("key").asString();
            System.out.println("key -> " + key);
            
            convID = "CONV-" + key;
            reply = "REPLY-" + key;
            
            enviarOK();
        }
    }
    
    /**
     * Funcion que
     * 
     * @author Alicia Rodriguez
     */
    public void enviarOK() {
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add("result", "ok");
        
        String mensaje = objetoJSON.toString();
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Interlocutor-GL"));
        outbox.setPerformative(ACLMessage.INFORM);
        outbox.setContent(mensaje);
        this.send(outbox);
    }
    
    /**
     *  Bucle con la actuacion de los drones
     * 
     *  @author Juan Francisco Diaz Morneo 
     */
    public abstract void actuacion();
    
    /**
     *  Funcion que actualiza la percepcion del drone
     * 
     *  @author Juan Francisco Diaz Moreno 
     */
    public void actualizarPercepcion() {
    
        //Envio de la solicitud
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.QUERY_REF);
        outbox.setSender(this.getAid());
        outbox.addReceiver(server);
        outbox.setConversationId(key);
        outbox.setInReplyTo(reply);
        
        this.send(outbox);
        
        //Recepción del mensaje
        do {
            try {
                inbox = this.receiveACLMessage();
            } catch (InterruptedException ex) {
                Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
            }
        } while(inbox.getPerformativeInt() != ACLMessage.INFORM);
        
        JsonObject percepcion = (Json.parse(inbox.getContent()).asObject());
        
        //GPS
        JsonObject coordenadas = percepcion.get("gps").asObject();
        gps.add("x", coordenadas.get("x").asInt());
        gps.add("y", coordenadas.get("y" ).asInt());
        gps.add("z", coordenadas.get("z").asInt());
        
        // FUEL
        fuel = percepcion.get("fuel").asDouble();
    }
    
    /**
     *  Funcion que calcula la posicion inicial de los drones
     * 
     *  @author Ana Rodriguez Duran
     */
    public void calcularPosicion(){
        ArrayList<Integer> posicion = null;
        //El halcon lo ponemos en el medio
        int x=50,y=50;
        
        //comprobar la altura maxima y si no la superamos
        posicion.add(x);
        posicion.add(y);
        
        
        
    }
    
    @Override
    public void finalize() {
        super.finalize();
    }
}