/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 * Clase principal para el dron de rescate
 * 
 * @author Juan Francisco Díaz
 */
public class DroneRescue extends AbstractDrone {
    
    // Estado del drone
    private EstadoRescue estado;
    
    // Variables que controlan el número de alemanes del mapa
    private int alemanesIniciales;
    private int alemanesRescatados;
    
    public DroneRescue( AgentID aid, String mapa ) throws Exception {
        super( aid, mapa );
        
        estado = EstadoRescue.OCIOSO;
        alemanesRescatados = 0;
    }
    
    /**
       *
       *  Funcion que establece el numero de alemanes que hay en el mapa inicialmente.
       * 
       * @Author Juan Francisco Diaz Moreno
       * 
       */
    public void setAlemanesIniciales( int alemanesIniciales ) {
        this.alemanesIniciales = alemanesIniciales;
    }
    
    /**
       *
       * Funcion que comprueba si todos los alemanes se han rescatado ya
       * 
       * @Author Juan Francisco Diaz Moreno
       * 
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
     * @author Juan Francisco Diaz Moreno
     */
    public void actuacion() {
        
        while( !todosRescatados() ) {
            
            
            
        }
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