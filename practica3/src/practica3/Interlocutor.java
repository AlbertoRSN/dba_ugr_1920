/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package practica3;

import DBA.SuperAgent;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.logging.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Juan Francisco Díaz Moreno
 */
public class Interlocutor extends SuperAgent {
    
    /**
     * 
     * Constructor de la clase Interlocutor
     * @author Juan Francisco Díaz Moreno
     * @param aid Identificador del agente
     * 
     */
    public Interlocutor( AgentID aid ) throws Exception {
        super( aid );
    }
    
    /**
     * 
     * Establece la conexión con el controlador
     * @author Juan Francisco Díaz Moreno
     * 
     */
    void establecerConexion() throws InterruptedException {
        
    }
    
    /**
     * 
     * @author Juan Francisco Díaz Moreno
     */
    public void execute() {
        
        try {
            establecerConexion();
        } catch( InterruptedException e ) {
            Logger.getLogger( Interlocutor.class.getName() ).log( Level.SEVERE, null, e );
        }
        
    }
    
}
