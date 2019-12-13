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
import java.io.IOException;
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
    //Visibilidad
    private int visibilidad;
    //Rango
    private int rango;
    //Altura maxima
    int alturaMax;
    //Mapa
    private String nombreMapa;
    private DBAMap map;
    
    //ID del servidor
    private AgentID server = new AgentID("Lesath");
    //Clave de sesión
    private String key;
    //Conversation ID
    private String convID;
    //Reply
    private String reply;
    //Session
    private String session;
    
    //Variables para mensajes
    private ACLMessage outbox;
    private ACLMessage inbox;
    
    //Sensores para la percepcion
    private int posx, posy, posz;
    private double fuel;
    private double gonioDistancia;
    private double gonioAngulo;
    private JsonArray infrared;
    private JsonArray awacs;
    private String status;
    private boolean goal;
    private int torescue;
    private int energy;
    
    //Numero de alemanes para tratar
    private int alemanesTotales;
    private int alemanesEncontrados;
    private int alemanesRescatados;
    
    //Coordenadas objetivo para el desplazamiento
    private int objetivox, objetivoy;
    
    /**
     * Constructor de la clase principal. Crea un nuevo Agente
     * @param aid ID del agente
     * @param mapa mapa en cuestion
     * @throws Exception
     * 
     * @author Alicia Rodriguez, Juan Francisco Diaz, Ana Rodriguez
     */
    public AbstractDrone(AgentID aid, String mapa) throws Exception {
        super(aid);
        rolname = this.getAid().name;
        nombreMapa = mapa;
        inicializarNumeroAlemanes();
        inicializarCaracteristicas();
        inicializarMapa();
        inicializarPosicion();
    }
    
    /**
     * Funcion que inicializa el numero de alemanes en funcion del mapa
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void inicializarNumeroAlemanes() {
        alemanesEncontrados = alemanesRescatados = 0;
        switch(nombreMapa) {
            case "playground":
                alemanesTotales = 4;
                break;
            case "map1":
                alemanesTotales = 5;
                break;
            case "map2":
                alemanesTotales = 5;
                break;
            case "map3":
                alemanesTotales = 6;
                break;
            case "map4":
                alemanesTotales = 8;
                break;
            case "map5":
                alemanesTotales = 10;
                break;
        }
    }
    
    /**
     * Funcion que inicializa las caracteristicas del drone en funcion de su
     * rol (visibilidad, rango y altura maxima)
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void inicializarCaracteristicas() {
        switch(rolname) {
            case "FLY":
                alturaMax = 255;
                visibilidad = 20;
                rango = 5;
                break;
            case "SPARROW":
                alturaMax = 240;
                visibilidad = 50;
                rango = 11;
                break;
            case "HAWK":
                alturaMax = 230;
                visibilidad = 100;
                rango = 41;
                break;
            case "RESCUE":
                alturaMax = 255;
                visibilidad = 1;
                rango = 1;
                break;
        }
    }
    
    /**
     * Funcion que carga el mapa
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void inicializarMapa() {
        map = new DBAMap();
        
        try {
            map.load( "./maps/"+ nombreMapa +"GL.png" );
        } catch (IOException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println( "\n\nERROR: no se pudo cargar el mapa.\n" );
        }
    }
    
    @Override
    public void init() {
        System.out.println("\n\nInicializando el drone -> " + this.getName() );
    }
    
    /**
     *  Funcion principal del drone
     * 
     *  @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public void execute() {
        
        //Comienza escuchando
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
            session = objeto.get( "session" ).asString();
            System.out.println("key -> " + key);
            
            convID = "CONV-" + key;
            reply = "REPLY-" + key;
            
            this.enviarOK();
        }
        checkin();
        actuacion();
    }
    
    /**
     * Funcion para realizar el checkin, es decir, situar los drones en el mapa
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void checkin() {
        System.out.println(rolname + " inicializando CHECKIN...");
        
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setConversationId(convID);
        outbox.setSender(this.getAid());
        outbox.setReceiver(server);
        
        JsonObject objeto = new JsonObject();
        objeto.add("command", "checkin");
        objeto.add("session", session);
        objeto.add("rol", rolname);
        objeto.add("x", posx);
        objeto.add("y", posy);
        
        String content = objeto.toString();
        outbox.setContent(content);
        
        this.send(outbox);
        
        System.out.println(rolname + " enviando " + content);
    }
    
    /**
     * Funcion para enviar el ok al interlocutor
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
     * Bucle con la actuacion de los drones
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran 
     */
    public abstract void actuacion();
    
    /**
     * Funcion que actualiza la percepcion del drone
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public void actualizarPercepcion() {
        
        //Envio de la solicitud
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.QUERY_REF);
        outbox.setSender(this.getAid());
        outbox.addReceiver(server);
        outbox.setConversationId(convID);
        outbox.setInReplyTo(reply);
        
        this.send(outbox);
        
        //Recepcion del mensaje
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
        posx = coordenadas.get("x").asInt();
        posy = coordenadas.get("y").asInt();
        posz = coordenadas.get("z").asInt();
        
        // FUEL
        fuel = percepcion.get("fuel").asDouble();
        
        // GONIO
        JsonObject gonio = percepcion.get("gonio").asObject();
        gonioDistancia = gonio.get("distance").asDouble();
        gonioAngulo = gonio.get("angle").asDouble();
        
        // INFRARED
        infrared = percepcion.get("infrared").asArray();
        
        // AWACS
        awacs = percepcion.get("awacs").asArray();
        
        // STATUS
        status = percepcion.get("status").asString();
        
        // GOAL
        goal = percepcion.get("goal").asBoolean();
        
        // TORESCUE
        torescue = percepcion.get("torescue").asInt();
        
        // ENERGY
        energy = percepcion.get("energy").asInt();
    }
    
    /**
     * Funcion que calcula la posicion inicial de los drones
     * 
     * @author Ana Rodriguez Duran, Alberto Rodriguez, Juan Francisco Diaz Moreno
     */
    public void inicializarPosicion(){    
        
        //Segun rolname sacamos el drone en una pos u otra
        switch(rolname) {
            //En la esquina superior izquierda
            case "FLY":
                posx = visibilidad;
                posy = map.getHeight() - visibilidad;
                break;
            //En la esquina inferior derecha
            case "SPARROW":
                posx = map.getWidth() - visibilidad;
                posy = visibilidad;
                break;
            //En el centro
            case "HAWK":
                posx = map.getWidth() / 2;
                posy = map.getHeight() / 2;
                break;
            //Casi al lado del hawk (para que no se choquen)
            case "RESCUE":
                posx = map.getWidth() / 2;
                posy = ( map.getHeight() / 2 ) + 1;
        }
    }
    
    /**
     * Funcion para repostar
     * 
     * @author Alberto Rodriguez
     */
    public void refuel(){
        JsonObject objetoJson = new JsonObject();
        objetoJson.add("command", "refuel");
        
        String mensaje = objetoJson.toString();
        //Envio de la solicitud de repostaje
        outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setSender(this.getAid());
        outbox.addReceiver(server);
        outbox.setContent(mensaje);
        outbox.setConversationId(convID);
        outbox.setInReplyTo(reply);
        
        this.send(outbox);
        
        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Si recibo inform, repostar
        if(inbox.getPerformativeInt() == ACLMessage.INFORM){
            //Fuel global, restarle lo que hemos añadido al agente
            //FuelTotal -= this.fuel;
            
            //Actualizo el fuel del drone.
            this.fuel = 100;
            System.out.println("HOLA HE REPOSTADO:)!!!!!");
        }
        else{
            JsonObject objeto = Json.parse(this.inbox.getContent()).asObject();
            System.out.println(objeto.get("result").asString());
        }
    }
    
    /**
     * Calcula a traves de la distancia del gonio si hay un aleman
     * @return true si visualiza el aleman, false en caso contrario
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public boolean alemanVisualizado() {
        return (gonioDistancia != -1);
    }
    
    /**
     * Calcula las coordenadas de un punto con la distancia y el angulo del gonio
     * @return coordenadas del punto
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public JsonObject calcularPosicion() {
        JsonObject coordenadas = new JsonObject();
        
        double difx = Math.cos( Math.toRadians( gonioAngulo ) ) * gonioDistancia;
        double dify = Math.sin( Math.toRadians( gonioAngulo ) ) * gonioDistancia;
        
        //if( gonioAngulo >= 0 && <=)
        return coordenadas;
    }
    
    /**
     * Funcion abstracta para calcular el siguiente movimiento segun un objetivo
     * @return siguiente movimiento
     * 
     * @author Juan Francisco Diaz Morneo 
     */
    public abstract String calcularSiguienteMovimiento();
    
    /**
     * Funcion para realizar el movimiento
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public void mover() {
        String movimiento = calcularSiguienteMovimiento();
        
        JsonObject command = new JsonObject();
        command.add("command", movimiento);
        
        outbox = new ACLMessage();
        outbox.setSender(this.getAid());
        outbox.setReceiver(server);
        outbox.setPerformative(ACLMessage.REQUEST);
        outbox.setContent(command.toString());
        outbox.setConversationId(convID);
        outbox.setInReplyTo(reply);
        
        this.send(outbox);
    }
    
    /**
     * Getter de rolname
     * @return rolname rolname
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public String getRolname() {
        return rolname;
    }
    
    /**
     * Getter de posx
     * @return posx posx
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public int getPosx() {
        return posx;
    }
    
    /**
     * Getter de posy
     * @return posy posy
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public int getPosy() {
        return posy;
    }
    
    /**
     * Getter de visibilidad
     * @return visibilidad visibilidad
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public int getVisibilidad() {
        return visibilidad;
    }
    
    /**
     * Getter de alemanesTotales
     * @return alemanesTotales alemanesTotales
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public int getAlemanesTotales() {
        return alemanesTotales;
    }
    
    /**
     * Getter de alemanesEncontrados
     * @return alemanesEncontrados alemanesEncontrados
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public int getAlemanesEncontrados() {
        return alemanesEncontrados;
    }
    
    /**
     * Getter de alemanesRescatados
     * @return alemanesRescatados alemanesRescatados
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public int getAlemanesRescatados() {
        return alemanesRescatados;
    }
    
    /**
     * Getter de map
     * @return map mapa
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public DBAMap getMap() {
        return map;
    }
    
    /**
     * Setter de objetivox
     * 
     * @param coordenada coordenada x
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public void setObjetivox(int coordenada) {
        objetivox = coordenada;
    }
    
    /**
     * Setter de objetivoy
     * 
     * @param coordenada coordenada y
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public void setObjetivoy( int coordenada ) {
        objetivoy = coordenada;
    }
    
    @Override
    public void finalize() {
        super.finalize();
    }
}