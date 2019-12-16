/*
 * Practica 3
 * Grupo L
 */
package practica3;

import es.upv.dsic.gti_ia.core.AgentID;

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
        
        if(alemanesIniciales == alemanesRescatados)
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
        
        while(!todosRescatados()) {
            actualizarPercepcion();
            System.out.println( getRolname() + " - percepción actualizada." );
            
            if( getGoal() )
                inicioRescate();
           
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
}