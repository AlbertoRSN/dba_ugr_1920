/*
 * Practica 3
 * Grupo L
 */
package practica3;

import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;

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
        actualizarPercepcion();
        subirMaxima();
        
        //Mientras los alemanes encontrados no superen al numero total a rescatar
        while( ( getAlemanesEncontrados() < getAlemanesTotales() ) || recorridoTerminado ) {
            
            if( estoyEnObjetivo() )
                siguienteObjetivo();
            
            enviarMove( calcularSiguienteMovimiento() );
            
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
}