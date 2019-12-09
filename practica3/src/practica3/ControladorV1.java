
package practica3;

import DBA.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que define al agente controlador que vamos a usar
 *
 * @author Alberto Rodríguez
 */
public class ControladorV1 extends SuperAgent {
    

    /**
     * Mapa que vamos a usar.
     */
    String mapa;
    
    /**
     * Clave de sesión.
     */
    String key;

    /**
     * Variables para mensajes.
     */
    ACLMessage outbox = null;
    ACLMessage inbox = null;
    
    /**
     * Conversation ID.
     */
    String convId = "";
   
    
    /**
     * Crea un nuevo Agente
     * 
     * @param aid ID del agente
     * @param map
     * @throws Exception
     * 
     * @author Alberto Rodríguez
     */
    public ControladorV1(AgentID aid, String map) throws Exception {
        super(aid);
        mapa = map;
    }
    

    @Override
    public void init() {
        System.out.println("\n\nInicializando el agente -> " + this.getName());
    }
    

    @Override
    public void execute() {        
 
        //Enviar mensaje de login
        login();
        
        try {
            inbox = receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(ControladorV1.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("No se puede recibir el mensaje");
        }
        
        if(inbox.getPerformativeInt() == ACLMessage.INFORM){
            System.out.println("\nLogin realizado con éxito");
            outbox = new ACLMessage();
            outbox.setSender(this.getAid());
            outbox.setReceiver(new AgentID("Lesath"));
            outbox.setPerformative(ACLMessage.CANCEL);
            outbox.setConversationId(convId);
            this.send(outbox);
            
            try {
                inbox = receiveACLMessage();
            } catch (InterruptedException ex) {
                Logger.getLogger(ControladorV1.class.getName()).log(Level.SEVERE, null, ex);
                System.out.println("No se puede recibir el mensaje");
            }
            
            if(inbox.getPerformativeInt() == ACLMessage.AGREE)
                System.out.println("\nSe ha cerrado sesión");
            else
                System.out.println("\nNo se ha cerrado sesión");
        }
        else{
            System.out.println("\nLOGIN NO REALIZADO");
        }
    }
    
    
    /**
     * Envío del mensaje para hacer login
     * 
     * 
     * @author Alberto Rodrgíuez
     */
    public void login() {
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add("map",mapa);
        objetoJSON.add("user", "Lackey");
        objetoJSON.add("password", "iVwGdxOa");
        
        String mensaje = objetoJSON.toString();
        
        /* envío */
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Lesath"));
        outbox.setPerformative(ACLMessage.SUBSCRIBE);
        outbox.setContent(mensaje);
        this.send(outbox);
    }

    @Override
    public void finalize() {
        super.finalize();
 
    }
    
        
    
    
}
