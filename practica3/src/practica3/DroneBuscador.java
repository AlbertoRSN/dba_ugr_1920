/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import com.eclipsesource.json.JsonObject;
import es.upv.dsic.gti_ia.core.AgentID;

/**
 * Clase principal para el dron de rescate
 * 
 * @author Juan Francisco Diaz
 */
public class DroneBuscador extends AbstractDrone {
    
    // Direccion hacia la que se dirige el drone
    private DireccionBuscador direccion;

    
    /**
      * 
      * Constructor de la clase
      * 
      * @param aid Tipo de drone buscador
      * @param mapa Nombre del mapa en el que buscar
      * @throws Exception Excepcion a lanzar
      * @Author Juan Francisco Diaz Moreno, Ana Rodriguez Duran
      */
    public DroneBuscador(AgentID aid, String mapa ) throws Exception {
        super( aid, mapa );
    }
    
    @Override
    public void actuacion() {
        
        seleccionPrimerObjetivo();
        
        while( getAlemanesEncontrados() < getAlemanesTotales() ) {
            
            actualizarPercepcion();
            
            
            if( alemanVisualizado() ) {
                notificarPosicion();
            }
            
            mover();
            
        }
        
    }
    
    private void notificarPosicion() {
        
        JsonObject coordenadas = calcularPosicion( );
        
    }

    private void seleccionPrimerObjetivo() {
        
        setObjetivox ( getPosx() );
        
        switch( super.getRolname() ) {
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

    @Override
    public String calcularSiguienteMovimiento() {
        
        String movimiento;
        
        switch( direccion ) {
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