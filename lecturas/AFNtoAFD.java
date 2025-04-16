package lecturas;
import java.io.*;
import java.util.*;

//import AFN;

public class AFNtoAFD {
    public static void main(String[] args) {
        // Definir las rutas dentro del código
        String direccionAFN = "pruebas/afn/prueba1.afn";  // Cambia esta ruta con la de tu archivo AFN
        String direccionAFD = "pruebas/afd";  // Cambia esta ruta con la ubicación donde guardar el archivo AFD

        // Crear el objeto AFN con la dirección del archivo AFN
        //AFN afn = new AFN(direccionAFN);

        // Generar el AFD en el archivo especificado
        //afn.toAFD(direccionAFD);
        
        System.out.println("Archivo AFD generado en: " + direccionAFD);
    }
}
