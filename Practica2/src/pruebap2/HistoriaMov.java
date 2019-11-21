/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pruebap2;

import java.util.ArrayList;

/**
 *
 * @author Alicia Rodríguez
 */
public class HistoriaMov {
     ArrayList<KeyPosition>table;
     
     public HistoriaMov () {
         table = new ArrayList<KeyPosition>();
     }


    public void add(KeyPosition key) {
        System.out.println(key.getKeyX());
        System.out.println(key.getKeyY());
        table.add(key);
    }
    public int size(){
        return table.size();
    }
    
    public KeyPosition get(int i){
        KeyPosition k;
        k = table.get(i);
        return k;
    }
}

//class key{
//     int x, y;
//
//     public key(int xx, int yy) {
//         x = xx;
//         y = yy;
//     }
//     public int getKeyX(){
//      return this.x;
//  }
//  
//    public int getKeyY(){
//        return this.y;
//    }
//     public boolean equals(key other) {
//         return other.x==x && other.y == y;
//     }
//}



