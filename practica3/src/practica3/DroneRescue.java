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
    
    public DroneRescue( AgentID aid ) throws Exception {
        super( aid );
    }
    
    /**
       * 
       * Actuación del Drone Rescue
       * @Author Juan Francisco Díaz Moreno
       * 
       */
    public void actuacion() {
        
        // Comprueba si está encima de un alemán
        // Comprueba si se ha detectado algún alemán
        //      Si se ha detectado -> Movimiento hacia el más cercano
        //      Si no se ha detectado -> Espera
        
    }
    
}
