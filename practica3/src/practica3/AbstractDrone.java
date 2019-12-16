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
    private String session = "";
    
    //Variables para controlar repostaje
    static final int MINFUEL = 10;
    private double gastoFuel; //Diferente segun que tipo de drone es
    
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
        //inicializarCaracteristicas();
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
     * rol (visibilidad, rango, altura maxima, gasto de fuel)
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran, Alberto Rodriguez
     */
    private void inicializarCaracteristicas() {
        switch(rolname) {
            case "FLY":
                alturaMax = 255;
                visibilidad = 20;
                rango = 5;
                gastoFuel = 0.1;
                break;
            case "SPARROW":
                alturaMax = 240;
                visibilidad = 50;
                rango = 11;
                gastoFuel = 0.5;
                break;
            case "HAWK":
                alturaMax = 230;
                visibilidad = 100;
                rango = 41;
                gastoFuel = 2;
                break;
            case "RESCUE":
                alturaMax = 255;
                visibilidad = 1;
                rango = 1;
                gastoFuel = 0.5;
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
            session = objeto.get("session").asString();
            //System.out.println("Session ---------------> " + session);
            System.out.println("key -> " + key);
            
            //convID = "CONV-" + key;
            //reply = "REPLY-" + key;
            
            convID = key ;
            reply = inbox.getReplyWith();
            
            this.enviarOK();
        }
        checkin();
        actuacion(); //Cada drone tiene su metodo Actuacion sobreescrito!

        //Si es necesario respostar, reposta.
        //if(necesitoRepostar())
        //repostar();
            
    }
    
    /**
     * Funcion para realizar el checkin, es decir, situar los drones en el mapa
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran, Alicia Rodriguez
     */
    private void checkin(){
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
        //objeto.add("key", key);
        
        String content = objeto.toString();
        outbox.setContent(content);
        
        this.send(outbox);
        
        System.out.println(rolname + " enviando " + content);
        
        try {
            inbox = this.receiveACLMessage();
            reply = inbox.getReplyWith();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(inbox.getPerformativeInt() == ACLMessage.INFORM){
            reply = inbox.getReplyWith();
            System.out.println("CHECKIN BIEN!!!!!!! UEEEE -> REPLY: " + reply);
            JsonObject recibido;
            recibido = Json.parse( this.inbox.getContent()).asObject();
            alturaMax = recibido.get("maxlevel").asInt();
            visibilidad = recibido.get("visibility").asInt();
            rango = recibido.get("range").asInt();
            gastoFuel =  recibido.get("fuelrate").asDouble();
        }
        else{
            System.out.println("MAL CHECKIN!");
        }
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
        outbox.setReceiver(new AgentID("Interlocutor-GLA"));
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
        
        try {
            inbox = this.receiveACLMessage();
            reply = inbox.getReplyWith();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            JsonObject percepcion = contenido.get( "result" ).asObject();

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
        } else {
            System.out.println( rolname + " no ha recibido bien la percepción." );
        }
    }
    
    /**
     * Funcion que calcula la posicion inicial de los drones
     * 
     * @author Ana Rodriguez Duran, Alberto Rodriguez, Juan Francisco Diaz Moreno
     */
    public void inicializarPosicion(){  
        
        int guia = ( rango - 1 ) / 2;
        
        //Segun rolname sacamos el drone en una pos u otra
        switch(rolname) {
            //En la esquina superior izquierda
            case "FLY":
                posx = guia;
                posy = guia;
                break;
            //En la esquina inferior derecha
            case "SPARROW":
                posx = map.getWidth() - guia;
                posy = map.getHeight() - guia;
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
     * Funcion que envia el mensaje para repostar
     * 
     * @author Alberto Rodriguez
     */
    public void enviarRefuel(){
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
            reply = inbox.getReplyWith();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        //Si recibo inform, repostar
        if(inbox.getPerformativeInt() == ACLMessage.INFORM){
            //Fuel global, restarle lo que hemos añadido al agente
            //FuelTotal -= this.fuel;
            energy -= fuel;
            
            //Actualizo el fuel del drone.
            this.fuel = 100;
            System.out.println("HE REPOSTADO:)!!!!!");
        }
        else{
            JsonObject objeto = Json.parse(this.inbox.getContent()).asObject();
            System.out.println(objeto.get("result").asString());
        }
    }
    
    /**
      * 
      * Funcion para repostar (baja hasta el nivel del suelo y hace el refuel)
      * 
      * @Author Juan Francisco Diaz Moreno, Alberto Rodriguez
      * 
      */
    public void repostar() {
        
        //Si la energia global que queda es suficiente para repostar, entonces me muevo, si no no gasto energia en repostar.
        if(energy >= 100-fuel){
            bajarSuelo();
            enviarRefuel();
        }
        else{
            System.out.println("No queda suficiente energia para repostar...");
        }
        
        
    }
    
    /**
      * 
      * Funcion que envia el mensaje para moverse
      * 
      * @param move Direccion a la que se movera
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void enviarMove( String move ) {
        
        outbox = new ACLMessage();
        outbox.setPerformative( ACLMessage.REQUEST );
        outbox.setSender( this.getAid() );
        outbox.setReceiver( server );
        outbox.setConversationId( convID );
        outbox.setInReplyTo( reply );
        
        JsonObject command = new JsonObject();
        command.add( "command", move );
        outbox.setContent( command.toString() );
        
        this.send( outbox );
        
        try {
            inbox = this.receiveACLMessage();
            reply = inbox.getReplyWith();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            System.out.println( "Drone " + rolname + " se ha movido: " + move );
            fuel -= gastoFuel;
            actualizarPercepcion();
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + rolname + " ERROR ENVIARMOVE: " + inbox.getPerformative() + " - result: " + result );
        }
        
    }
    
    /**
      *
      * Funcion que envia el mensaje de rescue y recibe su respuesta
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void enviarRescue() {
        
        outbox = new ACLMessage();
        outbox.setPerformative( ACLMessage.REQUEST );
        outbox.setSender( this.getAid() );
        outbox.setReceiver( server );
        outbox.setConversationId( convID );
        outbox.setInReplyTo( reply );
        
        JsonObject command = new JsonObject();
        command.add( "command", "rescue" );
        outbox.setContent( command.toString() );
        
        this.send( outbox );
        
        try {
            inbox = this.receiveACLMessage();
            reply = inbox.getReplyWith();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            actualizarPercepcion();
            System.out.println( "¡El drone rescue ha rescatado un alemán! Solo faltan: " + torescue );
        } else {
            JsonObject contenido = ( Json.parse( inbox.getContent() ).asObject() );
            String result = contenido.get( "result" ).asString();
            System.out.println( "ERROR RESCUE: " + inbox.getPerformative() + " - result: " + result );
        }
        
    }
    
    /**
      *
      * Funcion que calcula la altura del punto adyacente al drone que se le
      * indica
      * 
      * @param move Siguiente movimiento que quiere realizarse
      * @return Altura del punto al que se quiere desplazar
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private double calcularAlturaMove( String move ) {
        
        switch( move ) {
            case "moveN":
                return map.getLevel( posx, posy + 1 );
            case "moveNE":
                return map.getLevel( posx + 1, posy + 1 );
            case "moveE":
                return map.getLevel( posx + 1, posy );
            case "moveSE":
                return map.getLevel( posx + 1, posy - 1 );
            case "moveS":
                return map.getLevel( posx, posy - 1 );
            case "moveSW":
                return map.getLevel( posx - 1, posy - 1 );
            case "moveW":
                return map.getLevel( posx - 1, posy );
            default:
                return map.getLevel( posx - 1, posy + 1 );
        }
        
    }
    
    /**
      * 
      * Funcion que comprueba si se puede realizar un movimiento o se necesita
      * repostar antes.
      * 
      * @param move Siguiente movimiento que quiere realizarse
      * @return Devuelve true si necesita repostar antes de moverse y false en
      * caso contrario
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public boolean necesitoRepostar( String move ) {
        
        boolean necesita = false;
        
        double siguienteAltura = calcularAlturaMove( move );
        double diferenciaAltura = ( posz - siguienteAltura) / 5;
        
        //Convierte el valor a positivo para calcular el gasto total de los movimientos
        if(diferenciaAltura < 0 )
            diferenciaAltura *= -1;
        
        double necesario = gastoFuel + diferenciaAltura * gastoFuel;
        
        //Comprueba el nivel minimo de bateria
        if( ( fuel - necesario ) < MINFUEL )
            necesita = true;
        
        return necesita;
        
    }
    
    /**
     * 
     * Funcion que comprueba si un drone puede realizar el movimiento que
     * quiere en funcion de su altura actual
     * 
     * @param move Siguiente movimiento que quiere realizarse
     * @return Devuelve true si el drone puede moverse a esa casilla (la altura
     * a la que vuela es mayor que la altura de la casilla)
     * @Author Juan Francisco Diaz Moreno
     * 
     */
    public boolean puedoAcceder( String move ) {
        
        return posz > calcularAlturaMove( move );
        
    }
    
    /**
      *
      * Funcion para subir hasta la altura maxima del drone
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void subirMaxima() {
        
        while( posz < alturaMax )
            enviarMove( "moveUP" );
        
    }
    
    /**
      *
      * Funcion para bajar hasta el nivel del suelo
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void bajarSuelo() {
        
        
        while( posz > map.getLevel( posx, posy ) )
            enviarMove( "moveDW" );
        
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
      *
      * Funcion que comprueba si el drone ha llegado a las coordenadas x e y de
      * su objetivo
      * 
      * @return true si ha llegado, false en otro caso
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public boolean estoyEnObjetivo() {
        
        return ( ( posx == objetivox ) && ( posy == objetivoy ) );
        
    }
    
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
     * Getter del fuel restante de cada drone
     * @return necesito si necesita o no
     * 
     * @author Alberto Rodriguez 
     */
    public boolean necesitoRepostar(){
        boolean necesito = false;
        if(fuel < 10)
            necesito = true;
        
        return necesito;
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
    
    /**
      *
      * Getter del radio del rango
      * 
      * @return Radio del rango del drone
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public int getRadioRango() {
        return ( rango - 1 ) / 2;
    }
    
    /**
      *
      * Getter de si esta en un objetivo o no
      * 
      * @return está encima de objetivo o no
      * @Author Alberto Rodriguez
      * 
      */
    public boolean getGoal() {
        return goal;
    }
    
        /**
      *
      * Getter del rango
      * 
      * @return devuelve rango del drone
      * @Author Juanfran
      * 
      */
    public int getRango() {
        return rango;
    }
    
        
    /**
      *
      * Getter del rango
      * 
      * @return devuelve rango del drone
      * @Author Juanfran
      * 
      */
    public JsonArray getInfrared() {
        return infrared;
    }
    
    @Override
    public void finalize() {
        super.finalize();
    }
}
