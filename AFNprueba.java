import java.io.*;
import java.util.*;

/**
 * Clase para representar un AFN y convertirlo a AFD.
 * NO debe cambiar los nombres de la clase ni de los métodos existentes.
 */
public class AFNprueba{
    // Lectura AFN.
    private String direccionAFN;
    private String[] alfabeto;
    private int cantidadEstadosAFN;
    private int[] estadosFinalAFN;
    private List<List<Integer>> transicionesLambdaAFN;
    private List<List<List<Integer>>> transicionesEstadosAFN;

    // Algoritmo de clausura-λ.
    private Queue<Set<Integer>> conjuntoCreadosPendientes;                    
    private Map<Integer, Set<Integer>> mapaClausuras;
    private int contadorEstadosClausuras = 1;

    //Variables resultado AFD.
    private String direccionAFD;
    private int cantidadEstadosAFD;
    private List<Integer> estadosFinalAFD;
    private Map<Integer, List<List<Integer>>> transicionesEstadosAFD;

    int cantidadFilasMatrizAFD;
    ArrayList<TransicionAFD>[] transicionesEstadoAFD;

    //Lectura cuerdas.
    private Boolean cuerdaAceptada;

    public AFNprueba(String path){
        this.direccionAFN = path;
        this.transicionesLambdaAFN = new ArrayList<>();
        this.transicionesEstadosAFN = new ArrayList<>();
        this.cuerdaAceptada = false;

        this.conjuntoCreadosPendientes = new LinkedList<>();
        this.mapaClausuras = new HashMap<>();

        this.estadosFinalAFD = new ArrayList<>();


    }

    public boolean accept(String string) {
        return this.cuerdaAceptada;
    }

    public void toAFD(String afdPath){
        this.direccionAFD = afdPath;
        lecturaAFN();
        Set<Integer> estadoInicial = new HashSet<>();
        estadoInicial.add(1);
        clausura_lambda(estadoInicial);
        this.transicionesEstadoAFD = new ArrayList[alfabeto.length];
        for (int i = 0; i < alfabeto.length; i++) {
            this.transicionesEstadoAFD[i] = new ArrayList<>();
        }
        while (!conjuntoCreadosPendientes.isEmpty()) {
            //Obtengo conjunto estado clausura lambda
            Set<Integer> cambioEstado = conjuntoCreadosPendientes.poll();
            //Obtengo el nombre que se le puso a ese conjunto
            int nombreEstadoOrigen = obtenerEstado(cambioEstado);

            for (int i = 0; i < alfabeto.length; i++) {
                //Obtengo el caracter en el indice i del alfabeto
                String caracterCambio = alfabeto[i];
                //Creo el set para el conjunto que buscaremos
                Set<Integer> conjuntoDestino = new HashSet<>();
                //Hago un for para iterar en cada estado individual del conjunto que se obtuvo de la fila de lambda
                for (Integer estado : cambioEstado) {
                    //CReo una lista con las transiciones de la matriz de ese estado.
                    List<Integer> transiciones = transicionesEstadosAFN.get(i).get(estado);
                    //Voy agregando cada transicion al conjunto destino
                    for (Integer siguiente : transiciones) {
                        conjuntoDestino.add(siguiente);
                    }
                }
                //mando a "encolar" el conjunto destino y que tenga su nombre segun lambda
                clausura_lambda(conjuntoDestino);
                Integer nombreEstadoDestino = obtenerEstado(conjuntoDestino);
                //verifico que sea estado final o no.
                if (nombreEstadoDestino != null) {
                    boolean esEstadoFinal = false;
                    for (Integer estado : conjuntoDestino) {
                        if (Arrays.asList(estadosFinalAFN).contains(estado)) {
                            esEstadoFinal = true;
                            break;
                        }
                    }
                    //creo el objeto
                    TransicionAFD transicionCreada = new TransicionAFD(nombreEstadoOrigen, caracterCambio, nombreEstadoDestino, esEstadoFinal);
                    //lo agrego a la matriz que tenemos para AFD
                    transicionesEstadoAFD[i].add(transicionCreada);
                    //agrego el estado si es final al conjunto de estados finales AFD
                    if (esEstadoFinal && !estadosFinalAFD.contains(nombreEstadoDestino)) {
                        estadosFinalAFD.add(nombreEstadoDestino);
                    }
                }
                
            }
        }
        Collections.sort(estadosFinalAFD);

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


    public void clausura_lambda(Set<Integer> conjuntoActual){
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
        boolean yaExiste = mapaClausuras.values().stream().anyMatch(lista -> new HashSet<>(lista).equals(estadoNuevo));

        if (!yaExiste) {
            mapaClausuras.put(contadorEstadosClausuras, estadoNuevo);
            contadorEstadosClausuras++;
            conjuntoCreadosPendientes.add(estadoNuevo);
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
    
    private int getIndiceSimbolo(char simbolo) {
        for (int i = 0; i < alfabeto.length; i++) {
            if (alfabeto[i].equals(String.valueOf(simbolo))) {
                return i;
            }
        }
        return -1;
    }
    
    
    private void escribirAFD(String afdPath) {
    }
    
    


    public static void main(String[] args) {
        //*Prueba lectura AFN
        System.out.println("\n--- Prueba de lectura AFN ---");
        AFNprueba clase = new AFNprueba("pruebas/afn/prueba1.afn");
        clase.probarLecturaAFN();
        //*Prueba clausura de lambda
        //System.out.println("\n--- Prueba de clausura_Lambda ---");
        //  clase.lecturaAFN();
        //  Set<Integer> conjuntoEntrada = new LinkedHashSet<>(Arrays.asList(1));
        //  clase.clausura_lambda(conjuntoEntrada);
        //  Set<Integer> conjuntoEntrada2 = new LinkedHashSet<>(Arrays.asList(2));
        //  clase.clausura_lambda(conjuntoEntrada2);
        //  System.out.println("Clausuras en el mapa:");
        //  clase.mapaClausuras.forEach((key, value) -> {
        //      System.out.println("Clave: " + key + " -> Valor: " + value);
        //  });
        //*Prueba obtener nombre
        // System.out.println("\n--- Prueba de obtenerEstado ---");
        // Set<Integer> conjuntoABuscar = new LinkedHashSet<>(Arrays.asList(4,1));
        // Integer nombre = clase.obtenerEstado(conjuntoABuscar);
        // if (nombre != null) {
        //     System.out.println("El conjunto " + conjuntoABuscar + " tiene el nombre (clave): " + nombre);
        // } else {
        //     System.out.println("El conjunto " + conjuntoABuscar + " no fue encontrado en el mapa de clausuras.");
        // }
        //*Prueba toAFD
        System.out.println("\n--- Prueba del método toAFD ---");
        clase.toAFD("pruebas/afd/prueba1.afd"); // Ruta de salida, aunque escribirAFD está vacío

        // Iteramos por las transiciones construidas
        System.out.println("Transiciones del AFD:");
        for (int i = 0; i < clase.alfabeto.length; i++) {
            System.out.println("Símbolo '" + clase.alfabeto[i] + "':");
            ArrayList<AFNprueba.TransicionAFD> lista = clase.transicionesEstadoAFD[i];
            if (lista != null) {
                for (AFNprueba.TransicionAFD transicion : lista) {
                    System.out.println("  Desde " + transicion.getEstadoOrigen() + " con '" + transicion.getCaracter() +
                        "' -> " + transicion.getEstadoDestino() + (transicion.getFinaloNo() ? " (final)" : ""));
                }
            } else {
                System.out.println("  No hay transiciones registradas.");
            }
        }

        // Estados finales del AFD
        System.out.println("Estados finales del AFD: " + clase.estadosFinalAFD);
        
    }
    

    //***************************************************
    private class TransicionAFD {
        int estadoOrigen;
        String caracter;
        int estadoDestino;
        boolean finalOno;

        public TransicionAFD(int origen, String caracter, int destino, boolean finalOno) {
            this.estadoOrigen = origen;
            this.caracter = caracter;
            this.estadoDestino = destino;
            this.finalOno = finalOno;
        }
        public int getEstadoOrigen(){
            return this.estadoOrigen;
        }
        public int getEstadoDestino(){
            return this.estadoDestino;
        }
        public String getCaracter(){
            return this.caracter;
        }
        public boolean getFinaloNo(){
            return this.finalOno;
        }

    }




    //**************************
    public void probarLecturaAFN() {
        this.lecturaAFN();
        System.out.println("Alfabeto: " + Arrays.toString(alfabeto));
        System.out.println("Cantidad de estados: " + cantidadEstadosAFN);
        System.out.println("Estados finales: " + Arrays.toString(estadosFinalAFN));
        
        System.out.println("Transiciones lambda:");
        for (int i = 0; i < transicionesLambdaAFN.size(); i++) {
            System.out.println("  Estado " + i + " -> " + transicionesLambdaAFN.get(i));
        }
    
        System.out.println("Transiciones por símbolo:");
        for (int estado = 0; estado < cantidadEstadosAFN; estado++) {
            System.out.println("  Estado " + estado + ":");
            for (int simbolo = 0; simbolo < alfabeto.length; simbolo++) {
                List<List<Integer>> fila = transicionesEstadosAFN.get(simbolo);
                System.out.println("    Con símbolo '" + alfabeto[simbolo] + "' -> " + fila.get(estado));
            }
        }
    }
    
}
