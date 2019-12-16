/*
 * Practica 3
 * Grupo L
 */
package practica3;

import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;

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
        while( ( getAlemanesEncontrados() < getAlemanesTotales() ) || recorridoTerminado ) {
            
            if( estoyEnObjetivo() )
                siguienteObjetivo();
            
            String siguienteMovimiento = calcularSiguienteMovimiento();
            
            //while( !puedoAcceder( siguienteMovimiento ) )
                //siguienteMovimiento = redirigirMovimiento( siguienteMovimiento );
            
            if( necesitoRepostar( siguienteMovimiento ) ) {
                repostar();
                subirMaxima();
            }
            
            enviarMove( siguienteMovimiento );
      
        }
    }
    
    /**
     * Funcion para notificar en que coordenadas esta un aleman encontrado
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    private void notificarPosicion() {
        JsonObject coordenadas = calcularPosicion( );
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
        ArrayList<Integer> unosx = new ArrayList();
        ArrayList<Integer> unosy = new ArrayList();
        int tabla[][] = new int[getRango()][getRango()];
        int contador = 0;
    
            /// Cambiar i j
        for( int i = 0; i < getRango(); i++)
            for( int j = 0; j < getRango(); j++ ) {
                tabla[j][i] = datos.get(contador).asInt();
                contador++;
                
                if( tabla[j][i] == 1 ) {
                    unosx.add(i);
                    unosy.add(j);
                }
            }
        
        if( unosx.size() > 0 ) {
            
            int dronex = getRango() / 2;
            int droney = dronex;
            int alemanx, alemany;
            
            for( int i = 0; i < unosx.size(); i++ ) {
                if( unosx.get(i) < dronex )
                    alemanx = getPosx() - ( dronex - unosx.get(i) );
                else if( unosx.get(i) > dronex )
                    alemanx = getPosx() + ( dronex - unosx.get(i) );
                else
                    alemanx = getPosx();
                
                if( unosy.get(i) < droney )
                    alemany = getPosy() - ( droney - unosy.get(i) );
                else if( unosy.get(i) > droney )
                    alemany = getPosy() + ( droney - unosy.get(i) );
                else
                    alemany = getPosy();
                
                // INTRODUCIR COORDENADAS EN JSONARRAY
                // ENVIAR JSONARRAY A RESCUE
                    
            }
            
        }
        
    }
    
    
}



