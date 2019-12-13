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
    private DireccionBuscador direccion;
    
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
    }
    
    /**
     * Bucle con la actuacion de los drones
     * 
     * @author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
     */
    @Override
    public void actuacion() {
        seleccionPrimerObjetivo();
        
        //Mientras los alemanes encontrados no superen al numero total a rescatar
        while(getAlemanesEncontrados() < getAlemanesTotales()) {
            actualizarPercepcion();
            
            if( alemanVisualizado() ) {
                notificarPosicion();
            }
            mover();
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