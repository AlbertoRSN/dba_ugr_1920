package pruebap2;

import DBA.SuperAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.logging.Level;
import java.util.logging.Logger;

//Librerias Json
import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Clase principal MiAgente, hereda de SuperAgent
 * 
 * @author Alberto Rodriguez, Juan Francisco Diaz Moreno, Ana Rodriguez
 */
public class MiAgente extends SuperAgent {
    
    
    //----------- VARIABLE PARA ELEGIR MAPA -----------------
    private String mapa = "map5";
    
    //Referencia al estado de DragonFly
    private EstadosDrone estadoActual;
    
    //Datos para establecer conexion con el servidor
    private static final String USER = "Lackey";
    private static final String PASS = "iVwGdxOa";
    
    //Altura maxima del mapa que se este ejecutando
    private double alturaMaxima;
    
    //Posiciones vectores de percepcion
    static final int POSNW = 48;
    static final int POSN = 49;
    static final int POSNE = 50;
    static final int POSW = 59;
    static final int POSACTUAL = 60;
    static final int POSE = 61;
    static final int POSSW = 70;
    static final int POSS = 71;
    static final int POSSE = 72;
    
    //Variables para contemplar el refuel
    //Asumimos repostar con un 10 por ciento
    static final int MINFUEL = 10;
    static final double GASTOMOV = 0.5;
    
    /** Constructor de la clase MiAgente
     * @author Ana Rodriguez
     * @param aid id de nuestro agente en el servidor
     * @throws java.lang.Exception excepcion
     */
    public MiAgente(AgentID aid) throws Exception {
        super(aid);
    }
    
    @Override
    public void init(){
        System.out.println("\n\nInicializando el agente -> " + this.getName());
    }
    
    /**
     * Crea una imagen a partir de la traza recibida
     * @author Alberto Rodriguez
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
            try (FileOutputStream fos = new FileOutputStream("mitraza.png")) {
                fos.write(data);
            }
            System.out.println("¡Bravo! Traza guardada :)");
        }catch (IOException ex){
            System.err.println("Error al procesar la traza");
        }
    }

    /**
     * Envia un mensaje a otro agente
     * @author Alberto Rodriguez
     * @param receiver Nombre del agente que recibira el mensaje
     * @param content Contenido del mensaje
     */
    public void enviarMensaje(AgentID receiver, String content) {
        ACLMessage outbox = new ACLMessage();	
        outbox.setSender(this.getAid());
        outbox.setReceiver(receiver);
        outbox.setContent(content);	
        this.send(outbox);
    }
    
    /**
    * Recibe un mensaje de otro agente
    * @author Alberto Rodriguez
    * @return string con el contenido del mensaje, null en caso contrario
    */
    public String recibirMensaje() {
        try {
            ACLMessage inbox = this.receiveACLMessage();
            return inbox.getContent();
        } catch (InterruptedException ex) {
            Logger.getLogger(MiAgente.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    /**
     * Crear el objeto Json Para hacer el login
     * @author Alberto Rodriguez
     * @param map mapa a seleccionar
     * @return objeto con mensaje codificado
     */
    public String mensajeLogIn(String map){
        JsonObject objetoLogin = new JsonObject();
        
        objetoLogin.add("command", "login")
                   .add("map", map)
                   .add("radar", true)
                   .add("elevation", true)
                   .add("magnetic", false)
                   .add("gps", false)
                   .add("fuel", true)
                   .add("gonio", true)
                   .add("user", USER)
                   .add("password", PASS);
        
        return objetoLogin.toString();
    }

    /**
     * Hace comprobaciones de direccion segun el angulo
     * @author Alberto Rodriguez
     * @param valorAngle valor del angulo  donde se encuentra objetivo
     * @return string con la accion/movimiento a realizar
     */
    public String accionDireccion(double valorAngle){
        String movimiento = null;
        
        //DIRECCIÓN N SI ESTÁ ENTRE 330-360 ó 0-30
        if((valorAngle >=  330 && valorAngle <= 360) || (valorAngle >= 0 && valorAngle < 30)){
            movimiento = "moveN";
        }
        //DIRECCION NW SI ESTA ENTRE [300-330)
        if(valorAngle >=  300 && valorAngle < 330 ){
            movimiento = "moveNW";
        }  
        //DIRECCION NE SI ESTA ENTRE [30-60]
        if(valorAngle >=  30 && valorAngle <= 60 ){
            movimiento = "moveNE";
        }           
        //DIRECCION E SI ESTA ENTRE (60-120]
        if(valorAngle >  60 && valorAngle <= 120 ){
            movimiento = "moveE";
        }      
        //DIRECCION SE SI ESTA ENTRE (120-150]
        if(valorAngle > 120 && valorAngle <= 150 ){
            movimiento = "moveSE";
        }   
        //DIRECCION S SI ESTA ENTRE (150-210)
        if(valorAngle > 150 && valorAngle < 210 ){
            movimiento = "moveS";
        }
        //DIRECCION SW SI ESTA ENTRE [210-240]
        if(valorAngle >= 210 && valorAngle <= 240 ){
            movimiento = "moveSW";
        }
        //DIRECCION W SI ESTA ENTRE (240-300)
        if(valorAngle > 240 && valorAngle < 300 ){
            movimiento = "moveW";
        }
        return movimiento;
    }
    
    /**
      * Vuelve a hacer comprobaciones de direccion cuando no se puede acceder a la primera
      * @author Juan Francisco Diaz
      * @param movimientoImposible movimiento que no se ha podido realizar antes
      * @param alturas alturas relativas de las casillas adyacentes
      * @return string con la accion/movimiento a realizar
      */
    public String seleccionOtraDireccion(String movimientoImposible, JsonArray alturas){
        String nuevoMovimiento = null;
        
        switch(movimientoImposible){
            case "moveN":
                if(puedeSubir(getAlturaDestino("moveNE", alturas)))
                    nuevoMovimiento = "moveNE";
                else if(puedeSubir(getAlturaDestino("moveNW", alturas)))
                    nuevoMovimiento = "moveNW";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveNE", alturas);
                break;
                
            case "moveNE":
                if(puedeSubir(getAlturaDestino("moveE", alturas)))
                    nuevoMovimiento = "moveE";
                else if(puedeSubir(getAlturaDestino("moveN", alturas)))
                    nuevoMovimiento = "moveN";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveE", alturas);
                break;
                
            case "moveE":
                if(puedeSubir(getAlturaDestino("moveSE", alturas)))
                    nuevoMovimiento = "moveSE";
                else if(puedeSubir(getAlturaDestino("moveNE", alturas)))
                    nuevoMovimiento = "moveNE";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveSE", alturas);
                break;
                
            case "moveSE":
                if(puedeSubir(getAlturaDestino("moveS", alturas)))
                    nuevoMovimiento = "moveS";
                else if(puedeSubir(getAlturaDestino("moveE", alturas)))
                    nuevoMovimiento = "moveE";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveS", alturas);
                break;
                
            case "moveS":
                if(puedeSubir(getAlturaDestino("moveSW", alturas)))
                    nuevoMovimiento = "moveSW";
                else if(puedeSubir(getAlturaDestino("moveSE", alturas)))
                    nuevoMovimiento = "moveSE";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveSW", alturas);
                break;
                
            case "moveSW":
                if(puedeSubir(getAlturaDestino("moveW", alturas)))
                    nuevoMovimiento = "moveW";
                else if(puedeSubir(getAlturaDestino("moveS", alturas)))
                    nuevoMovimiento = "moveS";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveW", alturas);
                break;
                
            case "moveW":
                if(puedeSubir(getAlturaDestino("moveNW", alturas)))
                    nuevoMovimiento = "moveNW";
                else if(puedeSubir(getAlturaDestino("moveSW", alturas)))
                    nuevoMovimiento = "moveSW";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveNW", alturas);
                break;
                
            case "moveNW":
                if(puedeSubir(getAlturaDestino("moveN", alturas)))
                    nuevoMovimiento = "moveN";
                else if(puedeSubir(getAlturaDestino("moveW", alturas)))
                    nuevoMovimiento = "moveW";
                else
                    nuevoMovimiento = seleccionOtraDireccion("moveN", alturas);
                break;
        }
        return nuevoMovimiento;
    }
    
    /**
     * Realiza comprobaciones de altura relativa para bajar
     * @author Juan Francisco Diaz
     * @param alturaRelativa altura de la coordenada en la que se encuentra el agente
     * @return boolean (true si está al nivel del suelo, false si esta por encima)
     */    
    public boolean mismaAltura(double alturaRelativa) {
        return alturaRelativa == 0;
    }
    
    /**
     * Comprobaciones de alturas relativas
     * @author Juan Francisco Díaz
     * @param movimiento siguiente direccion a la que se quiere ir
     * @param alturas alturas relativas de las casillas adyacentes
     * @return double altura relativa de la posicion a la que se quiere avanzar
     */    
    public double getAlturaDestino(String movimiento, JsonArray alturas) {
        double alturaDestino = 0;
        
        switch( movimiento ){
            case "moveN":
                alturaDestino = alturas.get( POSN ).asDouble();
                break;
            case "moveNE":
                alturaDestino = alturas.get( POSNE ).asDouble();
                break;
            case "moveE":
                alturaDestino = alturas.get( POSE ).asDouble();
                break;
            case "moveSE":
                alturaDestino = alturas.get( POSSE ).asDouble();
                break;
            case "moveS":
                alturaDestino = alturas.get( POSS ).asDouble();
                break;
            case "moveSW":
                alturaDestino = alturas.get( POSSW ).asDouble();
                break;
            case "moveW":
                alturaDestino = alturas.get( POSW ).asDouble();
                break;
            case "moveNW":
                alturaDestino = alturas.get( POSNW ).asDouble();
                break;
        }
        return alturaDestino;
    }
    
    /**
    * Realiza comprobaciones de altura relativa para seguir
    * @author Juan Francisco Diaz, Ana Rodriguez
    * @param siguienteAltura altura del punto al que quiere moverse
    * @return boolean (true si necesita subir, false si no)
    */    
    public boolean  necesitaSubir(double siguienteAltura) {
        return (siguienteAltura < 0);
    }
    
    /**
     * Funcion que comprueba si necesita repostar
     * @author Ana Rodriguez Duran, Juan Francisco Diaz Moreno
     * @param alturaActual altura relativa actual
     * @param siguienteAltura altura relativa del punto al que quiere moverse
     * @param fuelActual cantidad de combustible actual
     * @return necesita, true si necesita repostar, false en caso contrario
     */
    private boolean necesitaRefuel(double alturaActual, double siguienteAltura, double fuelActual){
        boolean necesita = false;
        double necesario = GASTOMOV;
        double diferenciaAltura = alturaActual - siguienteAltura;
        
        //Convierte el valor a positivo para calcular el gasto total de los movimientos
        if(diferenciaAltura < 0 )
            diferenciaAltura *= -1;
        
        necesario += diferenciaAltura * GASTOMOV;
        
        //Comprueba el nivel minimo de bateria
        if((fuelActual - necesario) < MINFUEL)
            necesita = true;
        
        return necesita;
    }
    
    /**
     *  Funcion que comprueba si la siguiente posicion a la que el agente quiere desplazarse
     *  tiene una altura menor o igual a la que el agente puede alcanzar
     *  @author Juan Francisco Diaz Moreno
     *  @param siguienteAltura altura de la posicion a la que quiere desplazarse
     *  @return devuelve true si la altura es alcanzable por el agente
     */
    private boolean puedeSubir(double siguienteAltura) {
        return (siguienteAltura <= alturaMaxima);
    }
    
    /**
     * @author Alberto Rodriguez, Juan Francisco Diaz, Ana Rodriguez, Alicia Rodriguez
     */
    @Override
    public void execute(){
        
        System.out.println("\n" + this.getName()+"Comenzando la ejecución...\n");
        //Declaramos quien recibe el mensaje
        AgentID nameReceiver = new AgentID("Lesath");
       
        //***************************************************
        //              ENVIO DE MENSAJE 
        //***************************************************
        JsonObject objeto;
        
        String login = this.mensajeLogIn(mapa);
        //Enviar Mensaje Login
        System.out.println("Mensaje de LogIn -> "+ login);
        this.enviarMensaje((nameReceiver), login);
        
        // 1. Parsear el String original y almacenarlo en un objeto 
        objeto = Json.parse(this.recibirMensaje()).asObject();
        alturaMaxima = objeto.get("max").asDouble();
        
        // 2. Mostrar los valores asociados a cada campo
        System.out.println("\n\nRespuesta: " + objeto.toString());
        
        //3. Cogemos la clave para enviar segundo mensaje
        String clave = objeto.get("key").asString();
        System.out.println("Key -> " + clave);
        
        //4. Recibo un segundo mensaje con las percepciones del controlador
        objeto = Json.parse(this.recibirMensaje()).asObject();
        System.out.println("\n\nPercepcion: " + objeto.get("perceptions").asObject().toString());
        
        double valorAngle;
        valorAngle = objeto.get("perceptions").asObject().get("gonio").asObject().get("angle").asDouble();
        
        //Vector de alturas relativas y absolutas
        JsonArray alturasRelativas = new JsonArray();
        JsonArray alturasAbsolutas = new JsonArray();

        double fuelActual;
        double siguienteAlturaRelativa;
        double siguienteAlturaAbsoluta;
        double alturaActual;
        
        //Comprobar que puede realizar un movimiento antes de hacerlo
        boolean movimientoValido = true;
        String mov = null;
        
        //Declaramos el estado actual del drone
        estadoActual = EstadosDrone.ESTADO_INICIAL;
        
        while(objeto.get("perceptions").asObject().get("goal").asBoolean() == false && !estadoActual.equals( EstadosDrone.ESTRELLADO)){ 
            //Obtenemos el vector de alturas relativas
            alturasRelativas = objeto.get("perceptions").asObject().get("elevation").asArray();
            alturasAbsolutas = objeto.get("perceptions").asObject().get("radar").asArray();
            fuelActual = objeto.get("perceptions").asObject().get("fuel").asDouble();
            alturaActual = alturasRelativas.get(POSACTUAL).asDouble();
                    
            if(!this.mismaAltura(alturaActual) && !estadoActual.equals(EstadosDrone.SUBIENDO)){
                //Para ponerse al nivel del suelo
                mov = "moveDW";
                estadoActual = EstadosDrone.MOVIENDO;
                System.out.println("Posandome en tierra...");
            } else {
                mov = this.accionDireccion( valorAngle );
                estadoActual = EstadosDrone.MOVIENDO;
                siguienteAlturaRelativa = getAlturaDestino( mov, alturasRelativas );
                siguienteAlturaAbsoluta = getAlturaDestino( mov, alturasAbsolutas );
                System.out.println("Quiero hacer " + mov + "...");
                
                do {
                    if(!puedeSubir( siguienteAlturaAbsoluta)){
                        movimientoValido = false;
                        mov = seleccionOtraDireccion( mov, alturasAbsolutas );
                        System.out.println("Estaba demasiado alto, ahora haré " + mov + "...");
                        
                        siguienteAlturaRelativa = getAlturaDestino(mov, alturasRelativas);
                        siguienteAlturaAbsoluta = getAlturaDestino(mov, alturasAbsolutas);
                    } else {
                        movimientoValido = true;
                            
                        if(necesitaRefuel(alturaActual, siguienteAlturaRelativa, fuelActual)) {
                            mov = "refuel";  
                            System.out.println("Voy a necesitar repostar...");
                        } else if(this.necesitaSubir(siguienteAlturaRelativa)) {
                            mov = "moveUP";
                            estadoActual = EstadosDrone.SUBIENDO;
                            System.out.println("Necesito subir para ir allí...");
                        }
                    }
                } while( !movimientoValido );
            }
                    
            System.out.println(alturasRelativas.get(POSNW).asDouble() + "\t" +
                               alturasRelativas.get(POSN).asDouble() + "\t" +
                               alturasRelativas.get(POSNE).asDouble() + "\n" +
                               alturasRelativas.get(POSW).asDouble() + "\t" +
                               alturasRelativas.get(POSACTUAL).asDouble() + "\t" +
                               alturasRelativas.get(POSE).asDouble() + "\n" +
                               alturasRelativas.get(POSSW).asDouble() + "\t" +
                               alturasRelativas.get(POSS).asDouble() + "\t" +
                               alturasRelativas.get(POSSE).asDouble() + "\n");
            
            System.out.println("\nAltura actual: " + alturaActual + "\n" );
                    
            objeto.add("command", mov).add("key", clave);
            this.enviarMensaje(nameReceiver, objeto.toString());
            System.out.println("Envio Movimiento -> " + mov);
                
            //Recibir respuesta 1
            objeto = Json.parse(this.recibirMensaje()).asObject();
            System.out.println("\n\nRespuesta: " + objeto.toString());

            if( objeto.get( "result" ).asString().equals( "CRASHED" ) ) {
                System.out.println( "\n\nDragonfly se ha estrellado..." );
                estadoActual = EstadosDrone.ESTRELLADO;
            }

            //Recibir respuesta 2
            objeto = Json.parse(this.recibirMensaje()).asObject();
            System.out.println("\n\nPercepcion: " + objeto.get("perceptions").asObject().toString());
            valorAngle = objeto.get("perceptions").asObject().get("gonio").asObject().get("angle").asDouble();     
        }

        System.out.println( "\n\nGoal: " + objeto.get("perceptions").asObject().get("goal").asBoolean() );
            
        // ---------------- CODIGO PARA HACER LOGOUT ------------------- funcion en el futuro?????
        objeto = new JsonObject();
        //Hacer logout para recibir traza
        objeto.add("command", "logout").add("key", clave);
        this.enviarMensaje(nameReceiver, objeto.toString());
            
        System.out.println("Envio peticion logout -> "+ objeto.toString());
            
        objeto = Json.parse(this.recibirMensaje()).asObject();
        System.out.println("\n\nRespuesta: " + objeto.toString());
            
        objeto = new JsonObject();//Limpiar objeto
        objeto = Json.parse(this.recibirMensaje()).asObject();
        System.out.println("\n\nTraza: " + objeto.toString());

        //Crear imagen a partir de la traza recibida al hacer logout
        this.crearTraza(objeto);
    }
    
    @Override
    public void finalize(){
        super.finalize();
    }  
}