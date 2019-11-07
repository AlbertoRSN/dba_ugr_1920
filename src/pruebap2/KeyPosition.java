/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebap2;

/**
 *
 * @author Alicia Rodríguez
 */


public class KeyPosition {

  public int X;
  public int Y;

  public KeyPosition(int X, int Y) {
    this.X = X;
    this.Y = Y;
  }

  public int getKeyX(){
      return this.X;
  }
  
    public int getKeyY(){
        return this.Y;
    }
   
  public boolean equals (final Object O) {
    if (!(O instanceof KeyPosition)) return false;
    if (((KeyPosition) O).X != X) return false;
    if (((KeyPosition) O).Y != Y) return false;
    return true;
  }

}

/*
key.toString() 
*/