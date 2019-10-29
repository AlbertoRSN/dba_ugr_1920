package pruebap2;


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
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author Alberto Rodriguez, Juan Francisco Díaz Moreno, Ana Rodriguez
 */
public class MiAgente extends SuperAgent {
    
    private EstadosDrone estadoActual;
    
    private static final String USER = "Lackey";
    private static final String PASS = "iVwGdxOa";
    
    //nivel maximo de bateria 100%
    private int batery=100;
    
    // Posiciones vectores de percepción
    static final int POSNW = 48;
    static final int POSN = 49;
    static final int POSNE = 50;
    static final int POSW = 59;
    static final int POSACTUAL = 60;
    static final int POSE = 61;
    static final int POSSW = 70;
    static final int POSS = 71;
    static final int POSSE = 72;
    
    // Variables para contemplar el refuel
    static final int MINFUEL = 10;
    static final double GASTOMOV = 0.5;
    
    public MiAgente(AgentID aid) throws Exception {
        super(aid);
    }
    
    @Override
    public void init(){
         System.out.println("\n\n Inicializando el agente -> " + this.getName());
    }
    
    /**
     * Crear imagen a partir de la traza recibida
     * @author Alberto Rodríguez
     * @param jsObjeto Nombre del objeto con la traza
     */
    public void crearTraza(JsonObject jsObjeto){
        
        try{
            System.out.println("\nRecibiendo traza");
            //JsonObject injson = Json.parse(this.recibirMensaje()).asObject();
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
            System.err.println("Error procesando traza");
        }
    }

    /**
     * Envia un mensaje a otro agente
     * @author Alberto Rodríguez
     * @param receiver Nombre del agente que recibira el mensaje
     * @param content Contenido del mensaje
     * 
     */
    public void enviarMensaje(AgentID receiver, String content) {
        ACLMessage outbox = new ACLMessage();	
        outbox.setSender(this.getAid());
        outbox.setReceiver(receiver);
        outbox.setContent(content);	
        this.send(outbox);
    }
    
    /**
    * Recibir mensaje de otro agente
    * @author Alberto Rodriguez
    * @return String con el contenido del mensaje
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
    * @return objeto con mensaje codificado
    * @param map mapa a seleccionar
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
    * Hacer comprobaciones de direccion segun el angulo
    * @author Alberto Rodriguez, Alicia Rodriguez
    * @return string con la accion a realizar
    * @param valorAngle Valor del angulo  donde se encuentra objetivo
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
    * Hacer comprobaciones de altura relativa para bajar
    * @author Juan Francisco Díaz
    * @return boolean (true si está al nivel del suelo, false si está por encima)
    * @param alturaRelativa Altura de la coordenada en la que se encuentra el agente
    */    
    public boolean mismaAltura( double alturaRelativa ) {
        return alturaRelativa == 0;
    }
    
    /**
    * Comprobaciones de alturas relativas
    * @author Juan Francisco Díaz
    * @return double altura relativa de la posicion a la que se quiere avanzar
    * @param movimiento siguiente direccion a la que se quiere ir
    * @param alturas alturas relativas de las casillas adyacentes
    */    
    public double getAlturaDestino( String movimiento, JsonArray alturas  ) {
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
    * Hacer comprobaciones de altura relativa para seguir
    * @author Juan Francisco Díaz
    * @return boolean (true si necesita subir, false si no)
    * @param movimiento Próximo movimiento seleccionado para realizar
    * @param alturas Vector con las alturas relativas que percibe
    */    
    public boolean  necesitaSubir( double siguienteAltura ) {
        /*boolean lonecesita = false;
        
        switch( movimiento ){
            case "moveN":
                if( alturas.get( POSN ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveNE":
                if( alturas.get( POSNE ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveE":
                if( alturas.get( POSE ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveSE":
                if( alturas.get( POSSE ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveS":
                if( alturas.get( POSS ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveSW":
                if( alturas.get( POSSW ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveW":
                if( alturas.get( POSW ).asDouble() < 0 )
                    lonecesita = true;
                break;
            case "moveNW":
                if( alturas.get( POSNW ).asDouble() < 0 )
                    lonecesita = true;
                break;
        }
        
        return lonecesita;*/
        return ( siguienteAltura < 0 );
    }
    
    /**
     * Funcion para repostar
     * @author Ana Rodriguez Duran
     * @param nameReceiver nombre del agente receptor
     * @param map mapa
     */
    private boolean necesitaRefuel(double alturaActual, double siguienteAltura, double fuelActual){
        boolean necesita = false;
        
        double necesario = GASTOMOV;
        double diferenciaAltura = alturaActual - siguienteAltura;
        
        if( diferenciaAltura < 0 )
            diferenciaAltura *= -1;
        
        necesario += diferenciaAltura * GASTOMOV;
        
        if( ( fuelActual - necesario ) < MINFUEL )
            necesita = true;
        
        return necesita;
    }
    
    /**
    *
    * @author Alberto Rodriguez, Juan Francisco Díaz
    * 
    */
    @Override
    public void execute(){
        
       System.out.println("\n " + this.getName()+" Comenzando la ejecución... \n");
        
        //Declaramos quien recibe el mensaje
        AgentID nameReceiver = new AgentID("Lesath");
       
        // ***************************************************
        //              ENVIO DE MENSAJE 
        // ***************************************************
        JsonObject objeto; //= new JsonObject();
        
        //String mapa = "playground";
        //String mapa = "case_study";+
        //String mapa = "minicase";
        String mapa = "map4";
        
        String login = this.mensajeLogIn(mapa);
        //Enviar Mensaje Login
        System.out.println("Mensaje de LogIn -> "+ login);
        this.enviarMensaje((nameReceiver), login);
        
        // 1. Parsear el String original y almacenarlo en un objeto 
        objeto = Json.parse(this.recibirMensaje()).asObject();
        
        // 2. Mostrar los valores asociados a cada campo
        System.out.println("\n\nRespuesta: " + objeto.toString());
        
        //Cogemos la clave para enviar segundo mensaje
        String clave = objeto.get("key").asString();
        System.out.println("Key -> " + clave);
        
        //UNA VEZ QUE GUARDO LA CLAVE, HACER FUNCION QUE POR PARAMETRO MANDE LA CLAVE SIEMPRE Y LA ACCION A REALIZAR
        
        //RECIBIR SEGUNDO MENSAJE CON LAS PERCEPCIONES DEL CONTROLADOR
        objeto = Json.parse(this.recibirMensaje()).asObject();
        System.out.println("\n\nPercepcion: " + objeto.get("perceptions").asObject().toString());
        
        //Coger valor angulo del GONIO -> objeto.get("perceptions").asObject().get("gonio").asObject().get("angle").asDouble());
        
        //System.out.println(objeto.get("goal").asBoolean());
        double valorAngle;
        valorAngle = objeto.get("perceptions").asObject().get("gonio").asObject().get("angle").asDouble();
        
        // Vector de alturas relativas
        //ArrayList<double> alturasRelativas = new ArrayList<double>();
        JsonArray alturasRelativas = new JsonArray();
        JsonArray alturas = new JsonArray();
        
        //System.out.println("Valor Angulo: " + valorAngle);
        String mov = null;
        estadoActual = EstadosDrone.ESTADO_INICIAL;
        //comprobar en el bucle el fuel?
        double fuelActual;
        double siguienteAltura;
        double alturaActual;
        
            // ------------------- POSIBLE BUCLE -------------------------
            while( objeto.get("perceptions").asObject().get("goal").asBoolean() == false &&
                       !estadoActual.equals( EstadosDrone.ESTRELLADO )){
                
                    // Obtenemos el vector de alturas relativas
                    alturasRelativas = objeto.get("perceptions").asObject().get("elevation").asArray();
                    alturas = objeto.get("perceptions").asObject().get("radar").asArray();
                    fuelActual = objeto.get("perceptions").asObject().get("fuel").asDouble();
                    alturaActual = alturasRelativas.get( POSACTUAL ).asDouble();
                    //for( int i = 0; i < 121; i++ )
                        //alturasRelativas.add( jArray.get(i).asDouble() );
                    
                    if( !this.mismaAltura( alturaActual ) && !estadoActual.equals(EstadosDrone.SUBIENDO) ) {
                        mov = "moveDW";
                        estadoActual = EstadosDrone.MOVIENDO;
                    } else {
                        mov = this.accionDireccion(valorAngle);
                        siguienteAltura = getAlturaDestino( mov, alturasRelativas );
                        System.out.println( "\nSiguiente movimiento: " + mov + ", Siguiente altura: " + "\n" );
                        if( this.necesitaRefuel( alturaActual, siguienteAltura, fuelActual ) )
                            mov = "refuel";
                        else if( this.necesitaSubir( siguienteAltura ) ) {
                            mov = "moveUP";
                            estadoActual = EstadosDrone.SUBIENDO;
                        } else
                            estadoActual = EstadosDrone.MOVIENDO;
                    }
                    
                    System.out.println( alturasRelativas.get( POSNW ).asDouble() + "\t" +
                                                     alturasRelativas.get( POSN ).asDouble() + "\t" +
                                                     alturasRelativas.get( POSNE ).asDouble() + "\n" +
                                                     alturasRelativas.get( POSW ).asDouble() + "\t" +
                                                     alturasRelativas.get( POSACTUAL ).asDouble() + "\t" +
                                                     alturasRelativas.get( POSE ).asDouble() + "\n" +
                                                     alturasRelativas.get( POSSW ).asDouble() + "\t" +
                                                     alturasRelativas.get( POSS ).asDouble() + "\t" +
                                                     alturasRelativas.get( POSSE ).asDouble() + "\n");
                    System.out.println( "\nAltura actual: " + alturaActual + "\n" );
                    
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
            //}
            //------------------------------------------------------------------------------*/

        System.out.println( "\n\nGoal: " + objeto.get("perceptions").asObject().get("goal").asBoolean() );
            
        // ---------------- CODIGO PARA HACER LOGOUT ------------------- ***Meter en una funcion en un futuro***
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
        // ---------------------------------------------------------------  
           
            
        //Crear imagen a partir de la traza recibida al hacer logout
        this.crearTraza(objeto);
     
        
    }
    
    @Override
    public void finalize(){
        super.finalize();
    }
    
    
    
    
    
    
}
