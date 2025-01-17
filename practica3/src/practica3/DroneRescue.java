/*
 * Practica 3
 * Grupo L
 */
package practica3;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase principal para los drones de rescate
 * Hereda de la clase abstracta AbstractDrone
 * 
 * @author Juan Francisco Diaz, Ana Rodriguez Duran
 */
public class DroneRescue extends AbstractDrone {
    
    //Estado del drone
    private EstadoRescue estado;
    
    //Variables que controlan el numero de alemanes del mapa
    private int alemanesIniciales;
    private int alemanesRescatados;
    private int nuevosAlemanes;
    
    // Vector de alemanes detectados
    private ArrayList<CoordenadaXY> objetivos;
    
    /**
     * Constructor de la clase DroneRescue
     * @param aid tipo de drone buscador
     * @param mapa nombre del mapa en el que buscar
     * @throws Exception excepcion a lanzar
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public DroneRescue(AgentID aid, String mapa) throws Exception {
        super(aid, mapa);
        estado = EstadoRescue.OCIOSO;
        alemanesRescatados = 0;
        objetivos = new ArrayList();
    }
    
    /**
     * Setter del numero de alemanes iniciales
     * 
     * @param alemanesIniciales alemanesIniciales
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public void setAlemanesIniciales(int alemanesIniciales) {
        this.alemanesIniciales = alemanesIniciales;
    }
    
    /**
     * Funcion que comprueba si todos los alemanes han sido rescatados
     * @return true en caso de que todos se hayan rescatado, false en caso contrario
     * 
     * @author Juan Francisco Diaz Moreno
     */
    public boolean todosRescatados() {
        boolean rescatados = false;
        
        if( alemanesIniciales == alemanesRescatados )
            rescatados = true;
        
        return rescatados;
    }
    
    /**
     * Actuacion del drone Rescue
     * 
     * @author Juan Francisco Diaz Moreno, Alberto Rodriguez
     */
    @Override
    public void actuacion() {
        
        actualizarPercepcion();
        System.out.println( getRolname() + " - percepción actualizada." );
        alemanesIniciales = getToRescue();
        
        while(!todosRescatados()) {
            
            if( getGoal() ) {
                inicioRescate();
                quitarAleman( getCoordenadasXY() );
            }
            
            if ( objetivos.isEmpty() ) {
                esperarInbox();
            } else {    
                System.out.println( getRolname() + " - comienza a subir." );
                subirMaxima();
                System.out.println( getRolname() + " - subió a su altura máxima." );

                String siguienteMovimiento = calcularSiguienteMovimiento();

                if( necesitoRepostar( siguienteMovimiento ) ) {
                    repostar();
                    subirMaxima();
                }

                enviarMove( siguienteMovimiento );
            }
           
        }
          
    }
    
    /**
      *
      * Funcion para esperar a recibir un mensaje con alemanes
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void recibirDetectados( ACLMessage inbox ) {
        
        System.out.println( getRolname() + " - mensaje recibido de " + inbox.getSender().toString() );
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            
            JsonArray contenido = ( Json.parse( inbox.getContent() ).asArray() );
            System.out.println( getRolname() + " - recibido: " + contenido.toString() );
            
            for( int i = 0; i < contenido.size(); i++ ) {
                int x = contenido.get(i).asObject().get( "x" ).asInt();
                int y = contenido.get(i).asObject().get( "y" ).asInt();
                
                if( nuevoAleman( x, y ) ) {
                    insertarAleman( x, y );
                    nuevosAlemanes++;
                }
                
                setAlemanesEncontrados( getAlemanesEncontrados() + nuevosAlemanes );
                
                contestarBuscador( inbox );
            }
            
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + getRolname() + " ERROR RECIBIR ALEMANES: " + inbox.getPerformative() + " - result: " + result );
        }
        
    }
    
    /**
     * Funcion para calcular el siguiente movimiento segun un objetivo
     * @return movimiento siguiente movimiento
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    @Override
    public String calcularSiguienteMovimiento() {
        
        double anguloEnRadianes = objetivos.get(0).calcularAngulo( getCoordenadasXY() );
        double valorAngle = Math.toDegrees( anguloEnRadianes );
        
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
      *
      * Funcion que comprueba si un aleman ya habia sido detectado (si sus
      * coordenadas estan en el vector de objetivos)
      * 
      * @param x Coordenada x del aleman a buscar
      * @param y Coordenada y del aleman a buscar
      * @return Devuelve true si el aleman no estaba en el vector y false si si
      * que lo estaba
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private boolean nuevoAleman( int x, int y ) {
        
        boolean nuevo = true;
        
        for( int i = 0; i < objetivos.size() && nuevo; i++ )
            if( objetivos.get(i).getX() == x && objetivos.get(i).getY() == y )
                nuevo = false;
        
        return nuevo;
    }
    
    /**
      * 
      * Funcion que saca un aleman del vector de objetivos cuando es rescatado
      * 
      * @param aleman Coordenadas del aleman a quitar
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void quitarAleman( CoordenadaXY aleman ) {
        
        objetivos.remove( aleman );
        ordenarAlemanes();
        
    }
    
    /**
      *
      * Funcion que inserta las coordenadas de un aleman en el vector de
      * objetivos
      * 
      * @param x Coordenada x del aleman a insertar
      * @param y Coordenada y del aleman a insertar
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void insertarAleman( int x, int y ) {
        
        CoordenadaXY aleman = new CoordenadaXY( x, y );
        objetivos.add( aleman );
        ordenarAlemanes();
        
    }
    
    /**
      *
      * Funcion que ordena el vector de objetivos segun su proximidad al drone
      * 
      * @Author Juan Francisco Diaz Moreno, Valentine Seguineau
      * 
      */
    private void ordenarAlemanes() {
        
        if( !objetivos.isEmpty() ) {
            ArrayList<CoordenadaXY> ordenados = new ArrayList();
            double min, distancia;
            int pos;

            while( !objetivos.isEmpty() ) {
                min = objetivos.get(0).calcularDistancia( getCoordenadasXY() );
                pos = 0;

                for( int i = 1; i < objetivos.size(); i++ ) {
                    distancia = objetivos.get(i).calcularDistancia( getCoordenadasXY() );

                    if( distancia < min ) {
                        min = distancia;
                        pos = i;
                    }
                }

                ordenados.add( objetivos.get(pos) );
                objetivos.remove(pos);
            }

            for( CoordenadaXY c : ordenados )
                objetivos.add(c);
            
            setObjetivox( objetivos.get(0).getX() );
            setObjetivoy( objetivos.get(0).getY() );
        }
        
    }
    
    /**
      *
      * Funcion que realiza el rescate: baja hasta el suelo y manda el mensaje
      * correspondiente
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void inicioRescate() {
        
        bajarSuelo();
        enviarRescue();
        
    }
    
    /**
      *
      * Funcion que es llamada para recibir la respuesta del servidor de un
      * movimiento. Tiene en cuenta que a parte de mensajes del servidor, pueden
      * llegar mensajes de los buscadores
      * 
      * @param move Movimiento que se quiso realizar.
      * @Author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
      * 
      */
    @Override
    public void recibirRespuestaMove( String move ) {
        
        ACLMessage inbox = new ACLMessage();
        
        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            //System.out.println( "ANAAAAA " +inbox.getSender().toString());
            if("HAWK".equals(inbox.getSender().getLocalName()) ||
                "SPARROW".equals(inbox.getSender().getLocalName()) ||
                "FLY".equals(inbox.getSender().getLocalName()) ) {
                System.out.println( getRolname() + " - recibiendo alemanes de " + inbox.getSender() );
                recibirDetectados( inbox );
                recibirRespuestaMove( move );
            } else {
                System.out.println( "Drone " + getRolname() + " se ha movido: " + move + ", recibido de " + inbox.getSender().toString() );
                setReply( inbox.getReplyWith() );
                actualizarPercepcion();
            }
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + getRolname() + " ERROR ENVIARMOVE: " + inbox.getPerformative() + " - result: " + result );
        }
        
    }
    
    /**
      *
      * Funcion que espera un nuevo mensaje de parte de los buscadores
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void esperarInbox() {
        
        ACLMessage inbox = new ACLMessage();
        
        System.out.println( getRolname() + " - esperando inbox" );
        
        try {
            inbox = this.receiveACLMessage();
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        System.out.println( getRolname() + " - inbox recibido" );
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            System.out.println( getRolname() + " - recibe INFORM" );
            recibirDetectados( inbox );
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + getRolname() + " ERROR RECIBIRDETECTADOS: " + inbox.getPerformative() + " - result: " + result );
            esperarInbox();
        }
    }
    
    /**
      *
      * Funcion para subir hasta la altura maxima del drone.
      * Sube directamente sin considerar nada entre subida y subida.
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void subirMaxima() {
        
        while( getPosz() < alturaMax )
            enviarMove( "moveUP" );
        
    }
    
    /**
      *
      * Funcion que comprueba si un mensaje procede de un drone buscador
      * 
      * @param inbox Mensaje cuya procedencia se quiere comprobar
      * @return Devuelve true si el mensaje procede de HAWK, SPARROW o FLY,
      * false en otro caso
      * @Author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
      */
    private boolean procedeBuscador( ACLMessage inbox ) {
        
        return ( "HAWK".equals( inbox.getSender().getLocalName() ) ||
                 "SPARROW".equals( inbox.getSender().getLocalName() ) ||
                 "FLY".equals( inbox.getSender().getLocalName() ) );
                
    }
    
    /**
     * Funcion que solicita la percepcion del drone. Añade la posibilidad de que
     * se interponga un mensaje procedente de los drones buscadores
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    @Override
    public void actualizarPercepcion() {
        
        //Envio de la solicitud
        ACLMessage outbox = new ACLMessage();
        outbox.setPerformative(ACLMessage.QUERY_REF);
        outbox.setSender(this.getAid());
        outbox.addReceiver( getServer() );
        outbox.setConversationId( getConvID() );
        outbox.setInReplyTo( getReply() );
        
        this.send(outbox);
        
        //Recepcion del mensaje
        ACLMessage inbox = new ACLMessage();
        
        do{
            
            try {
                inbox = this.receiveACLMessage();
            } catch (InterruptedException ex) {
                Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            if( procedeBuscador( inbox ) ) {
                System.out.println( getRolname() + " - recibiendo alemanes de " + inbox.getSender() );
                recibirDetectados( inbox );
            }
            
        }while ( procedeBuscador( inbox ) );
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            setReply( inbox.getReplyWith() );
            
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            JsonObject percepcion = contenido.get( "result" ).asObject();

            setPercepcion( percepcion );
        } else {
            
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + getRolname() + " ERROR PERCEPCIÓN: " + inbox.getPerformative() + " - result: " + result );
            
        }
    }
    
    /**
      *
      * Funcion que envia un mensaje de confirmacion al buscador cuyo mensaje ha
      * procesado
      * 
      * @param inbox Mensaje al que se quiere contestar
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void contestarBuscador( ACLMessage inbox ) {
        
        String sender = inbox.getSender().getLocalName();
        
        ACLMessage outbox = new ACLMessage();
        outbox.setSender( this.getAid() );
        outbox.setReceiver( new AgentID( sender ) );
        outbox.setPerformative( ACLMessage.INFORM );
        
        JsonObject content = new JsonObject();
        content.add( "totales", getAlemanesTotales() );
        content.add( "encontrados", getAlemanesEncontrados() );
        
        outbox.setContent( content.toString() );
        
        this.send( outbox );
        
    }
    
}