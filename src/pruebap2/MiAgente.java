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
import java.util.Iterator;

/**
 * Clase principal MiAgente, hereda de SuperAgent
 * 
 * @author Alberto Rodriguez, Juan Francisco Diaz Moreno, Ana Rodriguez, Alicia Rodriguez, Valentine
 */
public class MiAgente extends SuperAgent {
    
    
    //----------- VARIABLE PARA ELEGIR MAPA -----------------
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
    
    
    //ArrayList que controla las posiciones por las que hemos pasado(aquí)
    ArrayList<JsonObject> posRecorridasnArrayList = new ArrayList<JsonObject>();
    
    String[] arrayMov1 = new String[]{"N","NE","E","SE","S","SW","W","NW"};
    
   
    
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
        String[] arrayMov = new String[]{"N","NE","E","SE","S","SW","W","NW"};
        
        //DIRECCIÓN N SI ESTÁ ENTRE 330-360 ó 0-30
        if((valorAngle >=  330 && valorAngle <= 360) || (valorAngle >= 0 && valorAngle < 30)){
            arrayMov[0]="N";
                    
            if(valorAngle >=  330 && valorAngle <= 360){
                arrayMov[1]="NW";
                arrayMov[2]="NE";
                arrayMov[3]="W";
                arrayMov[4]="E";
                arrayMov[5]="SW";
                arrayMov[6]="SE";
                arrayMov[7]="S";
            }
            if(valorAngle >= 0 && valorAngle < 30){
                arrayMov[1]="NE";
                arrayMov[2]="NW";
                arrayMov[3]="E";
                arrayMov[4]="W";
                arrayMov[5]="SE";
                arrayMov[6]="SW";
                arrayMov[7]="S";
            }    
        }
        
        //DIRECCION NW SI ESTA ENTRE [300-330)
        else if(valorAngle >=  300 && valorAngle < 330 ){
            arrayMov[0]="NW";
            if(valorAngle >= 315){
                arrayMov[1]="N";
                arrayMov[2]="W"; 
                arrayMov[3]="NE"; 
                arrayMov[4]="SW"; 
                arrayMov[5]="E"; 
                arrayMov[6]="S"; 
                arrayMov[7]="SE"; 
            }
            if(valorAngle < 315){
                arrayMov[1]="W";
                arrayMov[2]="N"; 
                arrayMov[3]="SW"; 
                arrayMov[4]="NE"; 
                arrayMov[5]="S"; 
                arrayMov[6]="E"; 
                arrayMov[7]="SE"; 
            }
        }  
        
        //DIRECCION NE SI ESTA ENTRE [30-60]
        if(valorAngle >=  30 && valorAngle <= 60 ){
            arrayMov[0]= "NE";
            if(valorAngle <= 45){
                arrayMov[1]="N";
                arrayMov[2]="E"; 
                arrayMov[3]="NW"; 
                arrayMov[4]="SE"; 
                arrayMov[5]="W"; 
                arrayMov[6]="S"; 
                arrayMov[7]="SW"; 
            }
            if(valorAngle > 45){
                arrayMov[1]="E";
                arrayMov[2]="N"; 
                arrayMov[3]="SE"; 
                arrayMov[4]="NW"; 
                arrayMov[5]="S"; 
                arrayMov[6]="W"; 
                arrayMov[7]="SW"; 
             }   
        }  
        
        //DIRECCION E SI ESTA ENTRE (60-120]
        if(valorAngle >  60 && valorAngle <= 120 ){
            arrayMov[0]= "E";
            if(valorAngle <= 90){
                arrayMov[1]="NE";
                arrayMov[2]="SE"; 
                arrayMov[3]="N"; 
                arrayMov[4]="S"; 
                arrayMov[5]="NW"; 
                arrayMov[6]="SW"; 
                arrayMov[7]="W"; 
                
            }
            if(valorAngle > 90){
                arrayMov[1]="SE";
                arrayMov[2]="NE"; 
                arrayMov[3]="S"; 
                arrayMov[4]="N"; 
                arrayMov[5]="SW"; 
                arrayMov[6]="NW"; 
                arrayMov[7]="W"; 
                
            }  
        }  
        
        //DIRECCION SE SI ESTA ENTRE (120-150]
        if(valorAngle > 120 && valorAngle <= 150 ){
            arrayMov[0] = "SE";
            if(valorAngle <= 135){
                arrayMov[1]="E";
                arrayMov[2]="S"; 
                arrayMov[3]="NE"; 
                arrayMov[4]="SW"; 
                arrayMov[5]="N"; 
                arrayMov[6]="W"; 
                arrayMov[7]="NW"; 
            }
            if(valorAngle > 135){
                arrayMov[1]="S";
                arrayMov[2]="E"; 
                arrayMov[3]="SW"; 
                arrayMov[4]="NE"; 
                arrayMov[5]="W"; 
                arrayMov[6]="N"; 
                arrayMov[7]="NW"; 
            }
        } 
              
         if(valorAngle > 150 && valorAngle < 210 ){
            arrayMov[0] = "S";
            if(valorAngle <= 180){
                arrayMov[1]="SE";
                arrayMov[2]="SW"; 
                arrayMov[3]="E"; 
                arrayMov[4]="W"; 
                arrayMov[5]="NE"; 
                arrayMov[6]="NW"; 
                arrayMov[7]="N"; 
            }
            if(valorAngle > 180){
                arrayMov[1]="SW";
                arrayMov[2]="SE"; 
                arrayMov[3]="W"; 
                arrayMov[4]="E"; 
                arrayMov[5]="NW"; 
                arrayMov[6]="NE"; 
                arrayMov[7]="N"; 
            }
        }
        //DIRECCION SW SI ESTA ENTRE [210-240]
        if(valorAngle >= 210 && valorAngle <= 240 ){
            arrayMov[0] = "SW";
            if(valorAngle <= 225){
                arrayMov[1]="S";
                arrayMov[2]="W"; 
                arrayMov[3]="SE"; 
                arrayMov[4]="NW"; 
                arrayMov[5]="E"; 
                arrayMov[6]="N"; 
                arrayMov[7]="NE"; 
            }
            if(valorAngle > 225){
                arrayMov[1]="W";
                arrayMov[2]="S"; 
                arrayMov[3]="NW"; 
                arrayMov[4]="SE"; 
                arrayMov[5]="N"; 
                arrayMov[6]="E"; 
                arrayMov[7]="NE"; 
            }
        }
        //DIRECCION W SI ESTA ENTRE (240-300)
        if(valorAngle > 240 && valorAngle < 300 ){
            arrayMov[0] = "W";
            if(valorAngle <= 270){
                arrayMov[1]="SW";
                arrayMov[2]="NW"; 
                arrayMov[3]="S"; 
                arrayMov[4]="N"; 
                arrayMov[5]="SE"; 
                arrayMov[6]="NE"; 
                arrayMov[7]="E"; 
                
            }
            if(valorAngle > 270){
                arrayMov[1]="NW";
                arrayMov[2]="SW"; 
                arrayMov[3]="N"; 
                arrayMov[4]="S"; 
                arrayMov[5]="NE"; 
                arrayMov[6]="SE"; 
                arrayMov[7]="E"; 
                
            }
        }   
          
        return arrayMov;
    }
    
    /**
     * Devuelve el movimiento que debe seguir el agente elegido po el vector la direcciones más prometedoras
     * @author Alicia Rodriguez
     * @param arrayMov1 array ordenado con las posiones donde avanzar
     * @param posRecorridasnArrayList arrayList con las posiciones por donde ya ha pasado el agente
     * @param x coordenada x en la que está el agente
     * @param y coordenada y en la que está el agente
     * @param i posicion del array
     * @return mov movimiento al que avanzar
     */
    public String sigMovimiento(String[] arrayMov1, ArrayList posRecorridasnArrayList, int x, int y, int i, int tam){
        
        String movimiento = null;
        Iterator<JsonObject> nombreIterator = posRecorridasnArrayList.iterator();
        JsonObject elemento = nombreIterator.next();
        int numElementos = posRecorridasnArrayList.size();
        boolean encontrado= false;
        String[] array=arrayMov1;
        //while(nombreIterator.hasNext() && tam < numElementos){
        //    System.out.println(elemento.get("x").asInt());
        //    tam++;
        //}
        System.out.println("entra en sigMovoviMiento");
        
        while(i<arrayMov1.length || encontrado==true){
        //for(int i=0;i<(arrayMov1.length);i++){
        System.out.println("entra en el while");
            
                if(arrayMov1[i] == "N"){                    
                    y = y+1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if( x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1,tam);
                        }
                        else{
                            movimiento = "moveN";
                            encontrado=true;
                        }  
                        tam++;
                    }  
                }
                  
                if(arrayMov1[i] == "NE"){
                    x = x+1;
                    y = y+1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveNE";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
                if(arrayMov1[i] == "E"){
                    x = x+1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveE";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
                if(arrayMov1[i] == "SE"){
                    x = x+1;
                    y = y-1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveSE";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
                if(arrayMov1[i] == "S"){
                    y = y-1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveS";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
                if(arrayMov1[i] == "SW"){
                    x = x-1;
                    y = y-1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveSW";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
                if(arrayMov1[i] == "W"){
                    x = x-1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveW";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
                if(arrayMov1[i] == "NW"){
                    x = x-1;
                    y = y+1;
                    while(nombreIterator.hasNext() && tam < numElementos){
                        if(x == elemento.get("x").asInt() && y == elemento.get("y").asInt()){
                            movimiento = sigMovimiento(array, posRecorridasnArrayList, x, y, i+1, tam);
                        }
                        else{
                            movimiento = "moveNW";
                            encontrado=true;
                        }
                        tam++;
                    }
                }
                
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
        interfaz = new Interfaz(objeto.get("perceptions").asObject());

        double fuelActual;
        double siguienteAlturaRelativa;
        double siguienteAlturaAbsoluta;
        double alturaActual;
        
        //Comprobar que puede realizar un movimiento antes de hacerlo
        boolean movimientoValido = true;
        String mov = null;
        
        
        int cont=0;
        int x;
        int y;
        int indice=0;
        int tam=0;
                
        //Declaramos el estado actual del drone
        estadoActual = EstadosDrone.ESTADO_INICIAL;
        
        while(objeto.get("perceptions").asObject().get("goal").asBoolean() == false && !estadoActual.equals( EstadosDrone.ESTRELLADO)){ 
            //Obtenemos el vector de alturas relativas
            alturasRelativas = objeto.get("perceptions").asObject().get("elevation").asArray();
            alturasAbsolutas = objeto.get("perceptions").asObject().get("radar").asArray();
            fuelActual = objeto.get("perceptions").asObject().get("fuel").asDouble();
            alturaActual = alturasRelativas.get(POSACTUAL).asDouble();
             
           
            //Justo al principio se añade la posición inicial, y a partir de ahí se van añadiendo las posiciones donde se situa(aqui)
            posRecorridasnArrayList.add(objeto.get("perceptions").asObject().get("gps").asObject());
            System.out.println(posRecorridasnArrayList.get(cont).asObject());
            cont++;
            
            
            if(!this.mismaAltura(alturaActual) && !estadoActual.equals(EstadosDrone.SUBIENDO)){
                //Para ponerse al nivel del suelo
                mov = "moveDW";
                estadoActual = EstadosDrone.MOVIENDO;
                System.out.println("Posandome en tierra...");
            } else {
                
                
                
                x = objeto.get("perceptions").asObject().get("gps").asObject().get("x").asInt();
                System.out.println(objeto.get("perceptions").asObject().get("gps").asObject().get("x").asInt());
                
                y = objeto.get("perceptions").asObject().get("gps").asObject().get("y").asInt();
                System.out.println(objeto.get("perceptions").asObject().get("gps").asObject().get("y").asInt());
                
                arrayMov1 = this.masPrometedor(valorAngle);
                for(int i=0;i<(arrayMov1.length);i++){
                    System.out.println(arrayMov1[i]);
                }
                
                //nuevo
                mov = this.sigMovimiento(arrayMov1,posRecorridasnArrayList, x, y, indice, tam);
             
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
            interfaz.actualizar(objeto.get("perceptions").asObject());
           
           
           
           
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