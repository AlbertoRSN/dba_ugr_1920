/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author juanfrandm98
 */
public class DroneRescue extends AbstractDrone {
    
    // Estado del drone
    EstadoRescue estado;
    
    public DroneRescue( AgentID aid ) throws Exception {
        super( aid );
        estado = EstadoRescue.OCIOSO;
    }
    
    /**
       * 
       * Actuación del Drone Rescue
       * @Author Juan Francisco Díaz Moreno
       * 
       */
    public void actuacion() {
        
        // SOLICITA PERCEPCIÓN
        
        // COMPRUEBA SI ESTÁ ENCIMA DE ALEMÁN
        
        // ESTADO == OCIOSO
        //      ESPERA
        
        // ESTADO == MOVIENDO
        //      SELECCIONA MEJOR MOVIMIENTO
        //      SE MUEVE
        
        // ESTADO == OBJETIVO
        //      SI ESTÁ AL NIVEL DEL SUELO
        //          RECOGE ALEMÁN
        //      NO LO ESTÁ
        //          BAJA
        
    }
    
}
