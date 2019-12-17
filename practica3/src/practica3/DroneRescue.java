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
        System.out.println( "TODOS RESCATADOS = " + todosRescatados() );
        
        while(!todosRescatados()) {
            actualizarPercepcion();
            System.out.println( getRolname() + " - percepción actualizada." );
            
            if( getGoal() ) {
                inicioRescate();
                quitarAleman( getCoordenadasXY() );
            }
            
            if ( objetivos.isEmpty() ) {
                esperarInbox();
            } else {
                
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
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            
            JsonArray contenido = ( Json.parse( inbox.getContent() ).asArray() );
            
            for( int i = 0; i < contenido.size(); i++ ) {
                int x = contenido.get(i).asObject().get( "x" ).asInt();
                int y = contenido.get(i).asObject().get( "y" ).asInt();
                
                if( nuevoAleman( x, y ) )
                    insertarAleman( x, y );
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
        String movimiento = null;
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
    
    public void recibirRespuestaMove( String move ) {
        
        ACLMessage inbox = new ACLMessage();
        
        try {
            inbox = this.receiveACLMessage();
            setReply( inbox.getReplyWith() );
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            if( inbox.getSender() == getServer() ) {
                System.out.println( "Drone " + getRolname() + " se ha movido: " + move );
                actualizarPercepcion();
            } else {
                recibirDetectados( inbox );
                recibirRespuestaMove( move );
            }
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + getRolname() + " ERROR ENVIARMOVE: " + inbox.getPerformative() + " - result: " + result );
        }
        
    }
    
    private void esperarInbox() {
        
        ACLMessage inbox = new ACLMessage();
        
        try {
            inbox = this.receiveACLMessage();
            setReply( inbox.getReplyWith() );
        } catch (InterruptedException ex) {
            Logger.getLogger(AbstractDrone.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if( inbox.getPerformativeInt() == ACLMessage.INFORM ) {
            recibirDetectados( inbox );
        } else {
            JsonObject contenido = (Json.parse(inbox.getContent()).asObject());
            String result = contenido.get( "result" ).asString();
            System.out.println( "Drone " + getRolname() + " ERROR RECIBIRDETECTADOS: " + inbox.getPerformative() + " - result: " + result );
            esperarInbox();
        }
    }
}