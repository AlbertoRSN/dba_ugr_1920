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
public class Key {

  public int X;
  public int Y;

  public Key(int X, int Y) {
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
    if (!(O instanceof Key)) return false;
    if (((Key) O).X != X) return false;
    if (((Key) O).Y != Y) return false;
    return true;
  }

}