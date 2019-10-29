/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebap2;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 *
 * @author Alberto Rodriguez
 */
public class PruebaP2 {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //Instanciacion del agente para enviar primeros mensajes al agente controlador.
        MiAgente dragonFly;
        
        //Crear conexion AgentsConnection.connect(“isg2.ugr.es”,6000,virtualhost,username,pass,false);
        AgentsConnection.connect(
                "isg2.ugr.es", //host
                6000, //puerto
                "Practica2", 
                "Lackey", //Username
                "iVwGdxOa", //Passw
                false //SSL
        ); 

        //Creacion del objeto MiAgente
        try{
            dragonFly = new MiAgente(new AgentID("DragonFly-AL")); //Este agente se ha quedado zombie en el servido
        } catch(Exception e){
            System.err.println("El agente ya existe en la plataforma");
            return;
        }
        //Lanzo el agente
        dragonFly.start();   
    }   
}