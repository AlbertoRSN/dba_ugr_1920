package pruebap2;

/**
 *
 * @author Alberto Rodriguez
 */
import DBA.SuperAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.logging.Level;
import java.util.logging.Logger;

//Librerias para Json
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class MiAgente extends SuperAgent {
    
    private EstadosDrone estadoActual;
    
    public MiAgente(AgentID aid) throws Exception {
        super(aid);
    }
    
     
    @Override
    public void init(){
         System.out.println("\n\n Inicializando el agente -> " + this.getName()+". ");
    }
    
    
    
    public void enviarMensaje(AgentID receiver, String content) {
		
        ACLMessage outbox = new ACLMessage();
		
        outbox.setSender(this.getAid());
        outbox.setReceiver(receiver);
        outbox.setContent(content);
		
        this.send(outbox);
    }
    
    /**
    *
    * @author Alberto Rodriguez
    * 
    */
    @Override
    public void execute(){
        
       System.out.println("\n " + this.getName()+" Comenzando la ejecución... \n");
        
       
        // ***************************************************
        //              ENVIO DE MENSAJE 
        
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Lesath"));

        
        // ***************************************************
        //                  CODIFICACION JSON
        // ***************************************************
        // 1. Crear el objeto 
        JsonObject objeto = new JsonObject();

        String nameSen = this.getName();
        String nameRec = "Lesath";
        String command = "login";
        String map = "map1";
        
        Boolean radar = false, 
                elevation = false, 
                magnetic = false, 
                gps = false, 
                fuel = false, 
                gonio = true;
        
        String user = "Lackey";
        String pass = "iVwGdxOa";
                  
        
        // 2. Añadir pares <clave,valor>      
        objeto = new JsonObject().add("command", command)
                                             .add("map", map)
                                             .add("radar", radar)
                                             .add("elevation", elevation)
                                             .add("magnetic", magnetic)
                                             .add("gps", gps)
                                             .add("fuel", fuel)
                                             .add("gonio", gonio)
                                             .add("user", user)
                                             .add("password", pass);
        
        
        // 3. Serializar el objeto en un String
        //String mensajeOutbox = objeto.toString();
        // 4. Manejar el String
        System.out.println("Mensaje serializado a enviar -> "+ objeto.toString());
        
        outbox.setContent(objeto.toString());
        this.send(outbox);
        
        //System.out.println("Mensaje enviado: " + outbox);
       // ***************************************************
       // ***************************************************
       
     
       
        //Recibir mensaje codificado del agente controlador (Lesath) ubicado en el servidor
        ACLMessage inbox = new ACLMessage();
        try {
            inbox=this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(MiAgente.class.getName()).log(Level.SEVERE, null, ex);
        }
        
      

        String fuente = inbox.getContent();
        
        // 1. Parsear el String original y almacenarlo en un objeto 
        objeto = Json.parse(fuente).asObject();
        
        // 2. Extraer los valores asociados a cada clave
        
        System.out.println("\n\nRespuesta: " + objeto.toString());//objeto.get("result").asString() );
        
        //Cogemos la clave para enviar segundo mensaje
        String clave = objeto.get("key").asString();
        System.out.println("Key ----> " + clave);
        
        
        //RECIBIR SEGUNDO MENSAJE CON LAS PERCEPCIONES DEL CONTROLADOR
          try {
            inbox=this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(MiAgente.class.getName()).log(Level.SEVERE, null, ex);
        }
        objeto = Json.parse(inbox.getContent()).asObject();
        System.out.println("\n\nPercepcion: " + objeto.get("perceptions").asObject().toString());
        
        
        
        //ENVIAR SEGUNDO MENSAJE PARA MOVER HORIZONTAL
        command = "moveSE";
        objeto.add("command", command).add("key", clave);
        
        outbox.setContent(objeto.toString());
        this.send(outbox);
        
        
        
        
        try {
            inbox=this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(MiAgente.class.getName()).log(Level.SEVERE, null, ex);
        }
        objeto = Json.parse(inbox.getContent()).asObject();
        System.out.println("\n\nRespuesta: " + objeto.toString());
        
         try {
            inbox=this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(MiAgente.class.getName()).log(Level.SEVERE, null, ex);
        }
        objeto = Json.parse(inbox.getContent()).asObject();
        System.out.println("\n\nPercepcion: " + objeto.get("perceptions").asObject().toString());
        
        
        
        
        
    }
    
    @Override
    public void finalize(){
        super.finalize();
    }
    
}
