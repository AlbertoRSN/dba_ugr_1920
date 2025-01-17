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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.*;
import javafx.util.Pair;


/**
 * Clase principal MiAgente, hereda de SuperAgent
 * 
 * @author Alberto Rodriguez, Juan Francisco Diaz Moreno, Ana Rodriguez, Alicia Rodriguez, Valentine
 */
public class MiAgente extends SuperAgent {

    //----------- VARIABLE PARA ELEGIR MAPA -------------
    private String mapa = "map5";
    
    //Referencia al estado de DragonFly
    private EstadosDrone estadoActual;
    
    
    private Interfaz interfaz;
    
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
    
    
    //ArrayList que controla las posiciones por las que hemos pasado(aqui)
    ArrayList<JsonObject> posRecorridasnArrayList = new ArrayList<JsonObject>();    
    String[] arrayMov1 = new String[]{"N","NE","E","SE","S","SW","W","NW"};
    KeyPosition key;
    HistoriaMov table = new HistoriaMov();
    //Hashtable<Key , Boolean> contenedor = new Hashtable<Key ,Boolean>();
    
    
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
                   .add("gps", true)
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
     * Almacena en un vector la direcciónes más prometedoras segun el angulo
     * @author Alicia Rodriguez
     * @param valorAngle valor del angulo  donde se encuentra objetivo
     * @return String[] con el vector de movimientos mas prometedores ordenados
     */
    public String[] masPrometedor(double valorAngle){
        String[] arrayMov = new String[]{"moveN","moveNE","moveE","moveSE","moveS","moveSW","moveW","moveNW"};
        
        //DIRECCIÓN N SI ESTÁ ENTRE 330-360 ó 0-30
        if((valorAngle >=  330 && valorAngle <= 360) || (valorAngle >= 0 && valorAngle < 30)){
            arrayMov[0]="moveN";
                    
            if(valorAngle >=  330 && valorAngle <= 360){
                arrayMov[1]="moveNW";
                arrayMov[2]="moveNE";
                arrayMov[3]="moveW";
                arrayMov[4]="moveE";
                arrayMov[5]="moveSW";
                arrayMov[6]="moveSE";
                arrayMov[7]="moveS";
            }
            if(valorAngle >= 0 && valorAngle < 30){
                arrayMov[1]="moveNE";
                arrayMov[2]="moveNW";
                arrayMov[3]="moveE";
                arrayMov[4]="moveW";
                arrayMov[5]="moveSE";
                arrayMov[6]="moveSW";
                arrayMov[7]="moveS";
            }    
        }
        
        //DIRECCION NW SI ESTA ENTRE [300-330)
        else if(valorAngle >=  300 && valorAngle < 330 ){
            arrayMov[0]="moveNW";
            if(valorAngle >= 315){
                arrayMov[1]="moveN";
                arrayMov[2]="moveW"; 
                arrayMov[3]="moveNE"; 
                arrayMov[4]="moveSW"; 
                arrayMov[5]="moveE"; 
                arrayMov[6]="moveS"; 
                arrayMov[7]="moveSE"; 
            }
            if(valorAngle < 315){
                arrayMov[1]="moveW";
                arrayMov[2]="moveN"; 
                arrayMov[3]="moveSW"; 
                arrayMov[4]="moveNE"; 
                arrayMov[5]="moveS"; 
                arrayMov[6]="moveE"; 
                arrayMov[7]="moveSE"; 
            }
        }  
        
        //DIRECCION NE SI ESTA ENTRE [30-60]
        if(valorAngle >=  30 && valorAngle <= 60 ){
            arrayMov[0]= "moveNE";
            if(valorAngle <= 45){
                arrayMov[1]="moveN";
                arrayMov[2]="moveE"; 
                arrayMov[3]="moveNW"; 
                arrayMov[4]="moveSE"; 
                arrayMov[5]="moveW"; 
                arrayMov[6]="moveS"; 
                arrayMov[7]="moveSW"; 
            }
            if(valorAngle > 45){
                arrayMov[1]="moveE";
                arrayMov[2]="moveN"; 
                arrayMov[3]="moveSE"; 
                arrayMov[4]="moveNW"; 
                arrayMov[5]="moveS"; 
                arrayMov[6]="moveW"; 
                arrayMov[7]="moveSW"; 
             }   
        }  
        
        //DIRECCION E SI ESTA ENTRE (60-120]
        if(valorAngle >  60 && valorAngle <= 120 ){
            arrayMov[0]= "moveE";
            if(valorAngle <= 90){
                arrayMov[1]="moveNE";
                arrayMov[2]="moveSE"; 
                arrayMov[3]="moveN"; 
                arrayMov[4]="moveS"; 
                arrayMov[5]="moveNW"; 
                arrayMov[6]="moveSW"; 
                arrayMov[7]="moveW"; 
                
            }
            if(valorAngle > 90){
                arrayMov[1]="moveSE";
                arrayMov[2]="moveNE"; 
                arrayMov[3]="moveS"; 
                arrayMov[4]="moveN"; 
                arrayMov[5]="moveSW"; 
                arrayMov[6]="moveNW"; 
                arrayMov[7]="moveW"; 
                
            }  
        }  
        
        //DIRECCION SE SI ESTA ENTRE (120-150]
        if(valorAngle > 120 && valorAngle <= 150 ){
            arrayMov[0] = "moveSE";
            if(valorAngle <= 135){
                arrayMov[1]="movemoE";
                arrayMov[2]="moveS"; 
                arrayMov[3]="moveNE"; 
                arrayMov[4]="moveSW"; 
                arrayMov[5]="moveN"; 
                arrayMov[6]="moveW"; 
                arrayMov[7]="moveNW"; 
            }
            if(valorAngle > 135){
                arrayMov[1]="moveS";
                arrayMov[2]="moveE"; 
                arrayMov[3]="moveSW"; 
                arrayMov[4]="moveNE"; 
                arrayMov[5]="moveW"; 
                arrayMov[6]="moveN"; 
                arrayMov[7]="moveNW"; 
            }
        } 
              
         if(valorAngle > 150 && valorAngle < 210 ){
            arrayMov[0] = "moveS";
            if(valorAngle <= 180){
                arrayMov[1]="moveSE";
                arrayMov[2]="moveSW"; 
                arrayMov[3]="moveE"; 
                arrayMov[4]="moveW"; 
                arrayMov[5]="moveNE"; 
                arrayMov[6]="moveNW"; 
                arrayMov[7]="moveN"; 
            }
            if(valorAngle > 180){
                arrayMov[1]="moveSW";
                arrayMov[2]="moveSE"; 
                arrayMov[3]="moveW"; 
                arrayMov[4]="moveE"; 
                arrayMov[5]="moveNW"; 
                arrayMov[6]="moveNE"; 
                arrayMov[7]="moveN"; 
            }
        }
        //DIRECCION SW SI ESTA ENTRE [210-240]
        if(valorAngle >= 210 && valorAngle <= 240 ){
            arrayMov[0] = "moveSW";
            if(valorAngle <= 225){
                arrayMov[1]="moveS";
                arrayMov[2]="moveW"; 
                arrayMov[3]="moveSE"; 
                arrayMov[4]="moveNW"; 
                arrayMov[5]="moveE"; 
                arrayMov[6]="moveN"; 
                arrayMov[7]="moveNE"; 
            }
            if(valorAngle > 225){
                arrayMov[1]="moveW";
                arrayMov[2]="moveS"; 
                arrayMov[3]="moveNW"; 
                arrayMov[4]="moveSE"; 
                arrayMov[5]="moveN"; 
                arrayMov[6]="moveE"; 
                arrayMov[7]="moveNE"; 
            }
        }
        //DIRECCION W SI ESTA ENTRE (240-300)
        if(valorAngle > 240 && valorAngle < 300 ){
            arrayMov[0] = "moveW";
            if(valorAngle <= 270){
                arrayMov[1]="moveSW";
                arrayMov[2]="moveNW"; 
                arrayMov[3]="moveS"; 
                arrayMov[4]="moveN"; 
                arrayMov[5]="moveSE"; 
                arrayMov[6]="moveNE"; 
                arrayMov[7]="moveE"; 
                
            }
            if(valorAngle > 270){
                arrayMov[1]="moveNW";
                arrayMov[2]="moveSW"; 
                arrayMov[3]="moveN"; 
                arrayMov[4]="moveS"; 
                arrayMov[5]="moveNE"; 
                arrayMov[6]="moveSE"; 
                arrayMov[7]="moveE"; 
                
            }
        }   
          
        return arrayMov;
    }
    
    
    
    public KeyPosition calculNewGpsPosicion(String nextPosition, int x, int y){
    
    KeyPosition coord;
    
           switch( nextPosition ){
            case "moveN":
                y = y - 1;
                break;
            case "moveNE":
                x = x + 1;
                y = y - 1;
                break;
            case "moveE":
                x = x + 1;
                break;
            case "moveSE":
                x = x + 1;
                y = y + 1;
                break;
            case "moveS":
                y = y + 1;
                break;
            case "moveSW":
                x = x - 1;
                y = y + 1;
                break;
            case "moveW":
                x = x - 1;
                break;
            case "moveNW":
                x = x - 1;
                y = y - 1;
                break;
        }
           
        coord = new KeyPosition(x,y);
        return coord;      
    }
    
    
    /**
     * Devuelve el movimiento que debe seguir el agente elegido po el vector la direcciones más prometedoras
     * @author Alicia Rodriguez y Valentine Seguineau
     * @param arrayMov1 array ordenado con las posiones donde avanzar
     * @param HistoriasCoord tabal hash con las coordenadas(x,y) como clave y valor booleano a true por donde ya ha pasado el agente
     * @param x coordenada x en la que está el agente
     * @param y coordenada y en la que está el agente
     * @param indexListMejorMov posicion del array
     * @return mov movimiento al que avanzar
     */
    public String sigMovimiento(String[] arrayMov1, HistoriaMov HistoriasCoord, int x, int y){
        
        String mov = " ";
        KeyPosition coordPrueba = new KeyPosition(x,y);
        int indexListMejorMov=0;
        boolean Esta = false;
        
       while(indexListMejorMov < arrayMov1.length)
        {
            coordPrueba = calculNewGpsPosicion(arrayMov1[indexListMejorMov], x, y);
            
            //Finaliza porque es false o porque ya ha recorrido todo el vector
            for (int i=0; i< HistoriasCoord.size(); i++){
                Esta = HistoriasCoord.get(i).equals(coordPrueba);
                //Esta = ((HistoriasCoord.get(i).getKeyX() == coordPrueba.X) && (HistoriasCoord.get(i).getKeyY() == coordPrueba.Y));
                if(Esta == true){
                    i = HistoriasCoord.size();
                    indexListMejorMov++;
                }
                System.out.println("HistoriasCoord: " + HistoriasCoord.get(i).getKeyX() + "," + HistoriasCoord.get(i).getKeyY()); 
                System.out.println("CoordPrueba: " + coordPrueba.X + "," + coordPrueba.Y); 
                System.out.println("¿Existe?" + Esta); 
            }
            if(Esta == false){
                mov = arrayMov1[indexListMejorMov];
                HistoriasCoord.add(coordPrueba);
                indexListMejorMov = arrayMov1.length;
            }
        }    
        if(mov == " "){
            mov = arrayMov1[0];
            coordPrueba = calculNewGpsPosicion(arrayMov1[0], x, y);
            HistoriasCoord.add(coordPrueba);
        }
        
        return mov;

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
        double diferenciaAltura = ( alturaActual - siguienteAltura) / 5;
        
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
     * @author Alberto Rodriguez, Juan Francisco Diaz, Ana Rodriguez, Alicia Rodriguez, Valentine
     */
    @Override
    public void execute(){
        
        System.out.println("\n" + this.getName()+"Comenzando la ejecución...\n");
        //Declaramos quien recibe el mensaje
        AgentID nameReceiver = new AgentID("Lesath");
       
        //***************************************************//
        //              ENVIO DE MENSAJE 
        //***************************************************//
        
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
        
        //Inicializamos el objeto interfaz con las primeras percepciones que recibe
        interfaz = new Interfaz(objeto.get("perceptions").asObject(), this, alturaMaxima );

        double fuelActual;
        double siguienteAlturaRelativa;
        double siguienteAlturaAbsoluta;
        double alturaActual;
        
        //Comprobar que puede realizar un movimiento antes de hacerlo
        boolean movimientoValido = true;
        String mov = null;
       
        
        
        int x;
        int y;

                
        //Declaramos el estado actual del drone
        estadoActual = EstadosDrone.ESTADO_INICIAL;
        
        while(objeto.get("perceptions").asObject().get("goal").asBoolean() == false && !estadoActual.equals( EstadosDrone.ESTRELLADO)){ 
            //Obtenemos el vector de alturas relativas
            alturasRelativas = objeto.get("perceptions").asObject().get("elevation").asArray();
            alturasAbsolutas = objeto.get("perceptions").asObject().get("radar").asArray();
            fuelActual = objeto.get("perceptions").asObject().get("fuel").asDouble();
            alturaActual = alturasRelativas.get(POSACTUAL).asDouble();
             
           
            //Justo al principio se añade la posición inicial, y a partir de ahí se van añadiendo las posiciones donde se situa(aqui)
            //posRecorridasnArrayList.add(objeto.get("perceptions").asObject().get("gps").asObject());
            //System.out.println(posRecorridasnArrayList.get(cont).asObject());
            //cont++;
            
            
            if(!this.mismaAltura(alturaActual) && !estadoActual.equals(EstadosDrone.SUBIENDO)){
                //Para ponerse al nivel del suelo
                mov = "moveDW";
                estadoActual = EstadosDrone.MOVIENDO;
                System.out.println("Posandome en tierra...");
            } else {
                                
                //aqui
                x = objeto.get("perceptions").asObject().get("gps").asObject().get("x").asInt();
                //System.out.println(objeto.get("perceptions").asObject().get("gps").asObject().get("x").asInt());
               
                y = objeto.get("perceptions").asObject().get("gps").asObject().get("y").asInt();
                //System.out.println(objeto.get("perceptions").asObject().get("gps").asObject().get("y").asInt());
                key = new KeyPosition(x,y);
                
                
                
                
                //contenedor.put(key , true);   //((120,200), true)
                System.out.println("Valor X: " + key.getKeyX() + "Valor Y: " + key.getKeyY());
 
                
                arrayMov1 = this.masPrometedor(valorAngle);
                for(int i=0;i<(arrayMov1.length);i++){
                    System.out.println(arrayMov1[i]);
                }
                  
                //nuevo
                mov = this.sigMovimiento(arrayMov1,table, key.getKeyX(), key.getKeyY());
                //table.add(key);
                
                //antiguo
                //mov = this.accionDireccion( valorAngle );
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
                break;
            }

           
            //Recibir respuesta 2
            objeto = Json.parse(this.recibirMensaje()).asObject();
            System.out.println("\n\nPercepcion: " + objeto.get("perceptions").asObject().toString());
            valorAngle = objeto.get("perceptions").asObject().get("gonio").asObject().get("angle").asDouble();
            interfaz.actualizar(objeto.get("perceptions").asObject(), mov );
           
           
           
           
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