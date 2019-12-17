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
 * Clase principal para los drones buscadores (fly,hawck y sparrow)
 * Hereda de la clase abstracta AbstractDrone
 * 
 * @author Juan Francisco Diaz, Ana Rodriguez Duran
 */
public class DroneBuscador extends AbstractDrone {
    
    // Direccion hacia la que se dirige el drone
    private DireccionBuscador direccion, direccionAntigua;
    
    // Es true si ha terminado su recorrido y puede dejar de moverse
    private boolean recorridoTerminado;
    
    // Variable que comprueba si ha detectado todos los alemanes en la ultima
    // vision
    private int ultimosDetectados;
    
    /**
     * Constructor de la clase DroneBuscador
     * @param aid tipo de drone buscador
     * @param mapa nombre del mapa en el que buscar
     * @throws Exception excepcion a lanzar
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    public DroneBuscador(AgentID aid, String mapa) throws Exception {
        super(aid, mapa);
        recorridoTerminado = false;
    }
    
    /**
      *
      * Funcion que comprueba si el RESCUE ha detectado a todos los alemanes del
      * mapa
      * 
      * @return Devuelve true si el RESCUE tiene detectados a todos los alemanes
      * @Author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
      * 
      */
    private boolean todosDetectadosRescue() {
        return getAlemanesEncontrados() == getAlemanesTotales();
    }
    
    /**
      *
      * Funcion que comprueba si este drone ha detectado a todos los alemanes
      * del mapa
      * 
      * @return Devuelve true si el drone ha detectado a todos los alemanes
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private boolean todosDetectadosEsteDrone() {
        return ultimosDetectados == getAlemanesTotales();
    }
    
    /**
     * Bucle con la actuacion de los drones
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    @Override
    public void actuacion() {
        seleccionPrimerObjetivo();
        System.out.println( getRolname() + "- primer objetivo seleccionado." );
        actualizarPercepcion();
        System.out.println( getRolname() + " - percepción actualizada." );
        //repostar();
        System.out.println( getRolname() + " - refuel." );
        subirMaxima();
        System.out.println( getRolname() + " - subió a su altura máxima.");
        
        //Mientras los alemanes encontrados no superen al numero total a rescatar
        while( !todosDetectadosRescue() || recorridoTerminado || !todosDetectadosEsteDrone() ) {    

            if( estoyEnObjetivo() )
                siguienteObjetivo();

            String siguienteMovimiento = calcularSiguienteMovimiento();

            if( necesitoRepostar( siguienteMovimiento ) ) {
                repostar();
                subirMaxima();
            }

            enviarMove( siguienteMovimiento );
            encontrarAlemanes();
      
        }
        
        System.out.println( getRolname() + " - todos los alemanes han sido detectados." );
    }
    
    /**
     * Funcion para notificar en que coordenadas esta un aleman encontrado
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void enviarAlemanes( JsonArray alemanes ) {
        
        ACLMessage outbox = new ACLMessage();
        
        outbox.setSender( this.getAid() );
        outbox.setReceiver( new AgentID( "RESCUE" ) );
        outbox.setPerformative( ACLMessage.INFORM );
        //outbox.setConversationId( getConvID() );
        //outbox.setInReplyTo( getReply() );
        outbox.setContent( alemanes.toString() );
        
        this.send( outbox );
        //System.out.println( "Enviando alemanes: " + alemanes.toString() );
        
        ACLMessage inbox = new ACLMessage();
        
        try {
            inbox = this.receiveACLMessage();
            setReply( inbox.getReplyWith() );
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            System.out.println( "Drone RESCUE HA RECIBIDO ALEMANESSSSS" );
        }
        
    }
    
    /**
     * Funcion que decide el primer movimiento a realizar por cada drone
     * segun el objetivo impuesto
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void seleccionPrimerObjetivo() {
        setObjetivox (getPosx());
        
        switch(super.getRolname()) {
            case "FLY":
                setObjetivoy( getVisibilidad() );
                direccion = DireccionBuscador.SUR;
                break;
            case "SPARROW":
                setObjetivoy( getMap().getHeight() - super.getVisibilidad() );
                direccion = DireccionBuscador.NORTE;
                break;
            case "HAWK":
                setObjetivoy( getMap().getHeight() - super.getVisibilidad() );
                direccion = DireccionBuscador.NORTE;
                break;
        }
        
        direccionAntigua = direccion;
    }
    
    /**
      *
      * Funcion que calcula las coordenadas del siguiente objetivo
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void calcularSiguienteObjetivo() {
        
        switch( direccion ) {
            case NORTE:
                setObjetivoy( getMap().getHeight() - getRadioRango() );
                break;
            case SUR:
                setObjetivoy( getRadioRango() );
                break;
            case ESTE:
                setObjetivox( getPosx() + getRadioRango() );
                break;
            case OESTE:
                setObjetivox( getPosx() - getRadioRango() );
                break;
        }
        
    }
    
    /**
      *
      * Funcion que decide el siguiente movimiento a realizar por cada drone
      * segun el objetivo impuesto
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    private void siguienteObjetivo() {
        
        switch( super.getRolname() ) {
            case "FLY":
                switch( direccion ) {
                    case ESTE:
                        if( direccionAntigua == DireccionBuscador.NORTE ) {
                            direccionAntigua = direccion;
                            direccion = DireccionBuscador.SUR;
                        } else {
                            direccionAntigua = direccion;
                            direccion = DireccionBuscador.NORTE;
                        }
                        break;
                    default:
                        direccionAntigua = direccion;
                        direccion = DireccionBuscador.ESTE;
                        break;
                }
                break;
            case "SPARROW":
                switch( direccion ) {
                    case OESTE:
                        if( direccionAntigua == DireccionBuscador.NORTE ) {
                            direccionAntigua = direccion;
                            direccion = DireccionBuscador.SUR;
                        } else {
                            direccionAntigua = direccion;
                            direccion = DireccionBuscador.NORTE;
                        }
                        break;
                    default:
                        direccionAntigua = direccion;
                        direccion = DireccionBuscador.OESTE;
                        break;
                }
                break;
            case "HAWK":
                switch( direccion ) {
                    case NORTE:
                        direccion = DireccionBuscador.SUR;
                        break;
                    case SUR:
                        recorridoTerminado = true;
                        break;
                }
        }
        
        calcularSiguienteObjetivo();
        
    }
    
    /**
     * Funcion para calcular el siguiente movimiento segun un objetivo
     * @return movimiento siguiente movimiento
     * 
     * @author Juan Francisco Diaz Moreno
     */
    @Override
    public String calcularSiguienteMovimiento() {
        String movimiento;
        
        switch(direccion) {
            case NORTE:
                movimiento = "moveN";
                break;
            case ESTE:
                movimiento = "moveE";
                break;
            case OESTE:
                movimiento = "moveW";
                break;
            default:
                movimiento = "moveS";
                break;
        }
        return movimiento;
    }
    
    /**
      * 
      * Funcion que calcula el siguiente movimiento si no puede acceder al lugar
      * al que queria moverse antes
      * 
      * @param move Movimiento que quiere realizar
      * @return Devuelve el siguiente movimiento recomendado a realizar en 
      * funcion del que queria realizar
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    /*
    public String redirigirMovimiento( String move ) {
        
        switch( move ) {
            case "moveN":
                return "moveNE";
            case "moveNE":
                return "moveNW";
            case "moveNW":
                return "moveE";
            case "moveE":
                return "moveSE";
            case "moveSE":
                return 
            default:
                return "moveN";
        }
        
    }
    */
    
    
    /**
      *
      * Funcion que comprueba si el drone detecta alemanes, calcula sus
      * coordenadas y las envia al rescue
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    
    private void encontrarAlemanes() {
        
        JsonArray datos = getInfrared();
        ArrayList<CoordenadaXY> unos = new ArrayList();
        int tabla[][] = new int[getRango()][getRango()];
        int contador = 0;
        
        System.out.println( getRolname() + " - infrared recibida: " + datos.toString() );
    
            /// Cambiar i j
        for( int i = 0; i < getRango(); i++)
            for( int j = 0; j < getRango(); j++ ) {
                tabla[j][i] = datos.get(contador).asInt();
                contador++;
                
                if( tabla[j][i] == 1 ) 
                    unos.add( new CoordenadaXY( i,j ) );
            }
        
        //System.out.println( getRolname() + " - número de alemanes detectados: " + unos.size() );
        
        //for( CoordenadaXY c : unos )
            //System.out.println( getRolname() + " - alemanx = " + c.getX() + " alemany = " + c.getY() );
        
        if( unos.size() > 0 ) {
            
            int dronex = getRango() / 2;
            int droney = dronex;
            int alemanx, alemany;
            ArrayList<CoordenadaXY> alemanes = new ArrayList();
            
            for( int i = 0; i < unos.size(); i++ ) {
                if( unos.get(i).getX() < dronex )
                    alemanx = getPosx() - ( dronex - unos.get(i).getX() );
                else if( unos.get(i).getX() > dronex )
                    alemanx = getPosx() + ( dronex - unos.get(i).getX() );
                else
                    alemanx = getPosx();
                
                if( unos.get(i).getY() < droney )
                    alemany = getPosy() - ( droney - unos.get(i).getY() );
                else if( unos.get(i).getY() > droney )
                    alemany = getPosy() + ( droney - unos.get(i).getY() );
                else
                    alemany = getPosy();
                
                alemanes.add( new CoordenadaXY( alemanx, alemany ) );
                    
            }
            
            JsonArray contenido = new JsonArray();
            
            for( CoordenadaXY c : alemanes ) {
                JsonObject pareja = new JsonObject();
                pareja.add( "x", c.getX() );
                pareja.add( "y", c.getY() );
                contenido.add( pareja );
                //System.out.println( getRolname() + " - introduciendo alemanx = " + c.getX() + " alemany = " + c.getY() );
            }
            
            enviarAlemanes( contenido );
            
        }
        
    }
    
    /**
      *
      * Funcion que es llamada para recibir la respuesta del servidor de un
      * movimiento.
      * 
      * @param move Movimiento que se quiso realizar.
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void recibirRespuestaMove( String move ) {
        
        ACLMessage inbox = new ACLMessage();
        
        try {
            inbox = this.receiveACLMessage();
            setReply( inbox.getReplyWith() );
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            //System.out.println( "Drone " + getRolname() + " se ha movido: " + move );
            actualizarPercepcion();
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            //System.out.println( "Drone " + getRolname() + " ERROR ENVIARMOVE: " + inbox.getPerformative() + " - result: " + result );
        }
        
    }
    
    /**
      *
      * Funcion para subir hasta la altura maxima del drone.
      * Comprueba si detecta alemanes cada vez que sube. 
      * 
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void subirMaxima() {
        
        while( ( getPosz() < alturaMax ) && !todosDetectadosEsteDrone() ) {
            enviarMove( "moveUP" );
            encontrarAlemanes();
        }
        
    }
    
    
}



