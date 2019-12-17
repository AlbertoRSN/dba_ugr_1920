/*
 * Practica 3
 * Grupo L
 */
package practica3;

import DBA.SuperAgent;
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.io.File;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal para el interlocutor que se comunica con el servidor
 * 
 * @author Juan Francisco Diaz Moreno, Alberto Rodriguez, Ana Rodriguez, Alicia Rodriguez
 */
public class Interlocutor extends SuperAgent {
    //Mapa que vamos a usar
    String mapa;
    //Clave de sesion
    String key;
    String reply;
    String session = "";
    //Variables para mensajes
    ACLMessage outbox = null;
    ACLMessage inbox = null;
    
    //Variable para el nombre del mapa
    static String _filename="map100x100";
    
    DBAMap map = new DBAMap();

    //Conversation ID
    String convId = "";
    
    /**
     * Constructor de la clase Interlocutor
     * @param aid Identificador del agente
     * @param mapa Mapa que se va a trabajar
     * @throws Exception
     * 
     * @author Juan Francisco Diaz Moreno, Alberto Rodriguez
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
     * Funcion execute
     * 
     * @author Alberto Rodriguez
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
            
            JsonObject objeto = Json.parse(this.inbox.getContent()).asObject();
            
            //Tratamos el mapa
            tratarMapa(objeto);
            
            key = inbox.getConversationId();
            System.out.println("Recibida key: " + key);
            
//            JsonObject recibido = new JsonObject();
//            recibido = ( Json.parse(inbox.getContent()).asObject() );
            session = objeto.get("session").asString();
            System.out.println("Recibida clave de session --> " + session);
            System.out.println("Recibidas DIMENSIONES DEL MAPA del servidor ---> X: " + objeto.get( "dimx" ).asInt() + ", Y: " + objeto.get( "dimy" ).asInt());
            
            
            //Enviamos la clave a los 4 drones
            this.enviarKey();
            
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
     * Envio del mensaje para hacer login
     * 
     * @author Alberto Rodriguez
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
     *  @author Alicia Rodriguez, Juan Francisco Diaz Moreno, Alberto Rodriguez, Ana Rodriguez Duran
     */
    public void enviarKey() {
        JsonObject objetoJSON = new JsonObject();
        objetoJSON.add("key", key);
        objetoJSON.add("session", session );
        //System.out.println("Soy interlocutor y envio esta clave de sesion ---> " + session);
        String mensaje = objetoJSON.toString();
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setPerformative(ACLMessage.INFORM);
        outbox.setContent(mensaje);
        //outbox.setConversationId(key);
        
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
     * Funcion para tratar el mapa en forma de imagen
     * @param objeto json que le pasamos para convertir en jsonArray
     * 
     * @author Luis Castillo
     * @coauthor Alberto Rodriguez (Adaptacion del codigo)
     * 
     */
    public void tratarMapa(JsonObject objeto){
    System.out.println("\n\n-------- TRATANDO EL MAPA ----------\nReading json file "+"./json/"+_filename+".json");
        File file = new File("./json/"+_filename+".json");
        if (file != null)
            try {
                //String str= new Scanner(file).useDelimiter("\\Z").next();
                String str = objeto.toString();
                /// START
                /// 1) A partir del JSONArray que me devuelve el INFORM de respuesta a SUBSCRIBE
                JsonArray img = Json.parse(str).asObject().get("map").asArray();
                //JsonArray img = objeto.get("map").asArray();
                
                /// 2) Construir una matriz bidimensional para el mapa
                map.fromJson(img);
                System.out.println("IMAGE DATA:");
                /// 3) Cuyas dimensiones se pueden consultar
                System.out.println(map.getWidth()+" pixs width & "+map.getHeight()+" pixs height");
                /// 4) Y cuyos valores se pueden consultar en getLevel(X,Y)
                System.out.print("First row starts with: ");
                for (int i=0; i<10; i++) 
                    System.out.print(map.getLevel(i, 0)+"-");
                System.out.print("\nLast row ends with: ");
                for (int i=0; i<10; i++) 
                    System.out.print(map.getLevel(map.getWidth()-1-i, map.getHeight()-1)+"-");
                System.out.println();
                /// END
                /// Se guarda una copia de la imagen en PNG, aunque esto no hace falta, es sólo a
                /// título informativo
                System.out.println("Saving file ./maps/"+mapa+"GL.png\n\n");
                map.save("./maps/"+mapa+"GL.png");
                System.out.println( "INTERLOCUTORRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRRR ./maps/"+mapa+"GL.png" + map.getHeight());
            }  catch (Exception ex) {
                System.err.println("***ERROR "+ex.toString());
            }
    }
    
    @Override
    public void finalize() {
        super.finalize();
    }   
}