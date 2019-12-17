/*
 * Practica 3
 * Grupo L
 */
package practica3;

/**
 *
 * @author Juan Francisco Diaz Moreno
 */
public class CoordenadaXY {
    
    private int x;
    private int y;
    
    /**
      * 
      * Constructor de la clase CoordenadaXY
      * 
      * @param x Coordenada x
      * @param y Coordenada y
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public CoordenadaXY( int x, int y ) {
        
        this.x = x;
        this.y = y;
        
    }
    
    /**
      *
      * Getter de la coordenada x
      * 
      * @return Valor de x
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public int getX() { return x; }
    
    /**
      *
      * Getter de la coordenada y
      * 
      * @return Valor de y
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public int getY() { return y; }
    
    /**
      *
      * Setter de la coordenada x
      * 
      * @param x Valor de x
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public void setX( int x ) { this.x = x; }
    
    /**
      *
      * Setter de la coordenada y
      * 
      * @param y Valor de y
      * @Author Juan Franciso Diaz Moreno
      * 
      */
    public void setY( int y ) { this.y = y; }
    
    /**
      *
      * Funcion que calcula la distancia entre dos puntos
      * 
      * @param otra Coordenada cuya distancia a esta se quiere calcular
      * @return Distancia entre dos CoordenadaXY
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public double calcularDistancia( CoordenadaXY otra ) {
        
        double cateto1 = x + otra.x;
        double cateto2 = y + otra.y;
        
        return Math.sqrt( cateto1 * cateto1 + cateto2 * cateto2 );
        
    }
    
    /**
      *
      * Funcion que calcula el angulo entre dos puntos
      * 
      * @param otra Coordenada cuyo angulo quiero calcular con respecto a esta
      * @return Angulo EN RADIANES entre dos CoordenadaXY
      * @Author Juan Francisco Diaz Moreno
      * 
      */
    public double calcularAngulo( CoordenadaXY otra ) {
        
        return Math.atan2( x - otra.x, y - otra.y );
        
    }
    
}
