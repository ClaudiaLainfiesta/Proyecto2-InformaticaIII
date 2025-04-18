import java.io.*;
import java.util.*;

/**
 * Clase para representar un AFN y convertirlo a AFD.
 * NO debe cambiar los nombres de la clase ni de los métodos existentes.
 */
public class AFN2{
    // Lectura AFN.
    private String direccionAFN;
    private String[] alfabeto;
    private int cantidadEstadosAFN;
    private int[] estadosFinalAFN;
    private List<List<Integer>> transicionesLambdaAFN;
    private List<List<List<Integer>>> transicionesEstadosAFN;

    // Algoritmo de clausura-λ.
    private Queue<ArrayList<Integer>> conjuntoCreadosPendientes;            
    private Queue<ArrayList<Integer>> conjuntoCreadosProcesados;           
    private Map<Integer, Set<Integer>> mapaClausuras;
    //private Map<Integer, ArrayList<Integer>> mapaAFD = new HashMap<>();
    private int contadorEstadosClausuras = 1;

    //Variables resultado AFD.
    private String direccionAFD;
    private int cantidadEstadosAFD;
    private List<Integer> estadosFinalAFD;
    private Map<Integer, List<List<Integer>>> transicionesEstadosAFD;

    //Lectura cuerdas.
    private Boolean cuerdaAceptada;

    public AFN2(String path){
        this.direccionAFN = path;
        this.transicionesLambdaAFN = new ArrayList<>();
        this.transicionesEstadosAFN = new ArrayList<>();
        this.cuerdaAceptada = false;

        this.conjuntoCreadosPendientes = new LinkedList<>();
        this.conjuntoCreadosProcesados = new LinkedList<>();
        this.mapaClausuras = new HashMap<>();

        this.estadosFinalAFD = new ArrayList<>();
        this.transicionesEstadosAFD = new HashMap<>();
    }

    public boolean accept(String string){
        // TODO: implementar según proyecto 2
        return false;
    }

    public void toAFD(String afdPath){
        this.direccionAFD = afdPath;
        lecturaAFN();
    }

    private void lecturaAFN(){
        try(BufferedReader reader = new BufferedReader(new FileReader(direccionAFN))){
            alfabeto = reader.readLine().split(",");
            cantidadEstadosAFN = Integer.parseInt(reader.readLine());
            String[] estadosFinal = reader.readLine().split(",");
            estadosFinalAFN = new int[estadosFinal.length];
            for (int i = 0; i < estadosFinal.length; i++) {
                estadosFinalAFN[i] = Integer.parseInt(estadosFinal[i].trim());
            }

            String[] lambdas = reader.readLine().split(",");
            transicionesLambdaAFN = new ArrayList<>();
            for (String cell : lambdas) {
                List<Integer> transicionLambda = new ArrayList<>();
                for (String p : cell.split(";")) {
                    if (!p.isEmpty()) {
                        transicionLambda.add(Integer.parseInt(p.trim()));
                    }
                }
                transicionesLambdaAFN.add(transicionLambda);
            }

            String line;
            transicionesEstadosAFN = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split(",");
                List<List<Integer>> row = new ArrayList<>();
                for (String cell : cells) {
                    List<Integer> vals = new ArrayList<>();
                    for (String p : cell.split(";")) {
                        if (!p.isEmpty()) {
                            vals.add(Integer.parseInt(p.trim()));
                        }
                    }
                    row.add(vals);
                }
                transicionesEstadosAFN.add(row);
            }
        } catch(IOException e){
            System.err.println("Error leyendo AFN: " + direccionAFN);
        }
    }


    public void clausura_lambda(ArrayList<Integer> conjuntoActual){
        Set<Integer> conjuntoNuevo = new LinkedHashSet<>(conjuntoActual);
        Queue<Integer> cola = new LinkedList<>(conjuntoActual);
        while (!cola.isEmpty()) {
            int estado = cola.poll();
            List<Integer> transiciones = transicionesLambdaAFN.get(estado);
            if (transiciones != null) {
                for (Integer siguiente : transiciones) {
                    if (conjuntoNuevo.add(siguiente)) {
                        cola.add(siguiente);
                    }
                }
            }
        }
        Set<Integer> estadoNuevo = new HashSet<>(conjuntoNuevo);
        boolean yaExiste = mapaClausuras.values().stream()
        .anyMatch(lista -> new HashSet<>(lista).equals(estadoNuevo));

        if (!yaExiste) {
            mapaClausuras.put(contadorEstadosClausuras, estadoNuevo);
            contadorEstadosClausuras++;
        }
        
    }

    public Integer obtenerEstado(Set<Integer> conjuntoABuscar) {
        for (Map.Entry<Integer, Set<Integer>> entry : mapaClausuras.entrySet()) {
            if (entry.getValue().equals(conjuntoABuscar)) {
                return entry.getKey();
            }
        }
        return null;
    }


    public static void main(String[] args) throws Exception{
        // TODO: implementar invocación
    }
}
/*NOTAS:
 * Falta imprimir AFD
 * Algoritmo de conversion
 * main
 * accept cuerda
 */