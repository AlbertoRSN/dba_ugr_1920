/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebap2;

/**
 *
 * @author Alberto Rodriguez
 */
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

public class MiAgente extends SingleAgent {
    
    public MiAgente(AgentID aid) throws Exception {
        super(aid);
    }
    
     
    @Override
    public void init(){
         System.out.println("\n\n Inicializando el agente -> " + this.getName()+". ");
    }
    
    
    /**
    *
    * @author Alberto Rodriguez
    */
    @Override
    public void execute(){
        
       System.out.println("\n " + this.getName()+" Comenzando la ejecución... \n");
        
       
        // ***************************************************
        //              ENVIO DE MENSAJE 
        
        ACLMessage outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(new AgentID("Lesath"));
        
        //outbox.setReceiver(new AgentID("Lesath"));
        // outbox.addReceiver(new AgentID("YYY"));
        
        //Mensaje a enviar a Lesath 
        // {"sender":"Dragonfly10","receiver":"Bellatrix",
        //"content":{"command":"login","map":"case_study","radar":true,"elevation":true,"magnetic":true,"gps":true,"fuel":true,"gonio":true}}
 
        // ***************************************************
        //                  CODIFICACION JSON
        // ***************************************************
        // 1. Crear el objeto 
        JsonObject objeto = new JsonObject();

        String nameSen = this.getName();
        String nameRec = "Lesath";
        String command = "login";
        String map = "playground";
        Boolean radar = true, 
                elevation = true, 
                magnetic = true, 
                gps = true, 
                fuel = true, 
                gonio = true;
        String user = "Lackey";
        String pass = "iVwGdxOa";
                  
        
        // 2. Añadir pares <clave,valor>
        objeto.add("sender", nameSen);
        objeto.add("receiver",nameRec);
        objeto.add("content",new JsonObject().add("command", command)
                                             .add("map", map)
                                             .add("radar", radar)
                                             .add("elevation", elevation)
                                             .add("magnetic", magnetic)
                                             .add("gps", gps)
                                             .add("fuel", fuel)
                                             .add("gonio", gonio)
                                             .add("user", user)
                                             .add("password", pass));
        
        
        // 3. Serializar el objeto en un String
        String mensajeOutbox = objeto.toString();
        // 4. Manejar el String
        System.out.println("Mensaje serializado -> "+ mensajeOutbox);
        
        outbox.setContent(mensajeOutbox);
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
        
        // {"sender":"Bellatrix","receiver":"Dragonfly10","content":{"result":"ok","in-reply-to":"login","key":"bp4glvkb","dimx":51,"dimy":51,"min":5,"max":240}}
       

        String fuente = inbox.getContent();
        
        // 1. Parsear el String original y almacenarlo en un objeto 
        objeto = Json.parse(fuente).asObject();
        
        // 2. Extraer los valores asociados a cada clave
        
        System.out.println("result: " + objeto.get("content").asObject().get("result").asString());
        
        //System.out.println("result: " + objeto.get("result").asString());
        //System.out.println("In Reply to: " + objeto.get("in-reply-to").asString());
     /* System.out.println("content:{ ");
        System.out.println("result: " + objeto.get("content").asObject().get("result").asString());
        c
        //System.out.println("Anchura= " + objeto.get("content").asObject().get("in-reply-to").asString());
    */
        //System.out.println("}.");
        
        
  
    }
    
    @Override
    public void finalize(){
        super.finalize();
    }
    
}
