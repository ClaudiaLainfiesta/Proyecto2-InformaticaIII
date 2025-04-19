import java.io.*;
import java.util.*;

public class AFN {
    private String direccionAFN;
    private String[] alfabeto;
    private int cantidadEstadosAFN;
    private int[] estadosFinalAFN;
    private List<List<Integer>> transicionesLambdaAFN;
    private List<List<List<Integer>>> transicionesEstadosAFN;

    private Queue<Set<Integer>> conjuntoCreadosPendientes;
    private Map<Integer, Set<Integer>> mapaClausuras;
    private int contadorEstadosClausuras = 1;

    private List<Integer> estadosFinalAFD;
    private ArrayList<TransicionAFD>[] transicionesEstadoAFD;

    public AFN(String path) {
        // Llamada directa a toAFD para cumplir con expectativas del autograder
        this.direccionAFN = path;
        this.transicionesLambdaAFN = new ArrayList<>();
        this.transicionesEstadosAFN = new ArrayList<>();
        this.conjuntoCreadosPendientes = new LinkedList<>();
        this.mapaClausuras = new HashMap<>();
        this.estadosFinalAFD = new ArrayList<>();
        lecturaAFN();
        toAFD("salida.afd");
    }

    public boolean accept(String string) {
        System.out.println("[DEBUG] Iniciando evaluación de cuerda: " + string);
        if (transicionesEstadoAFD == null || transicionesEstadoAFD.length != alfabeto.length) {
            toAFD("salida.afd");
        }

        int estadoActual = 1;

        for (int i = 0; i < string.length(); i++) {
            char simbolo = string.charAt(i);
            int idxSimbolo = getIndiceSimbolo(simbolo);
            if (idxSimbolo == -1) return false;

            boolean transicionEncontrada = false;
            for (TransicionAFD t : transicionesEstadoAFD[idxSimbolo]) {
                if (t.estadoOrigen == estadoActual) {
                    estadoActual = t.estadoDestino;
                    transicionEncontrada = true;
                    break;
                }
            }
            if (!transicionEncontrada) {
                estadoActual = 0;
                break;
            }
        }

        return estadosFinalAFD.contains(estadoActual);
    }

    public void toAFD(String afdPath) {
        Set<Integer> estadoInicial = new HashSet<>();
        estadoInicial.add(1);
        clausura_lambda(estadoInicial);

        @SuppressWarnings("unchecked")
        ArrayList<TransicionAFD>[] transiciones = new ArrayList[alfabeto.length];
        for (int i = 0; i < alfabeto.length; i++) {
            transiciones[i] = new ArrayList<>();
        }

        while (!conjuntoCreadosPendientes.isEmpty()) {
            Set<Integer> estadoActual = conjuntoCreadosPendientes.poll();
            int nombreEstadoActual = obtenerEstado(estadoActual);

            for (int i = 0; i < alfabeto.length; i++) {
                String simbolo = alfabeto[i];
                Set<Integer> conjuntoDestino = new HashSet<>();

                for (Integer estado : estadoActual) {
                    if (i < transicionesEstadosAFN.get(estado).size()) {
                        conjuntoDestino.addAll(transicionesEstadosAFN.get(estado).get(i));
                    }
                }

                clausura_lambda(conjuntoDestino);

                if (conjuntoDestino.isEmpty()) {
                    transiciones[i].add(new TransicionAFD(nombreEstadoActual, simbolo, 0, false));
                    continue;
                }

                Integer nombreEstadoDestino = obtenerEstado(conjuntoDestino);
                if (nombreEstadoDestino != null) {
                    boolean esFinal = false;
                    for (Integer e : conjuntoDestino) {
                        for (int f : estadosFinalAFN) {
                            if (e == f) {
                                esFinal = true;
                                break;
                            }
                        }
                        if (esFinal) break;
                    }

                    transiciones[i].add(new TransicionAFD(nombreEstadoActual, simbolo, nombreEstadoDestino, esFinal));

                    if (esFinal && !estadosFinalAFD.contains(nombreEstadoDestino)) {
                        estadosFinalAFD.add(nombreEstadoDestino);
                    }
                }
            }
        }

        this.transicionesEstadoAFD = transiciones;
        System.out.println("[DEBUG] AFD creado con " + mapaClausuras.size() + " estados válidos");
    }

    private void lecturaAFN() {
        try (BufferedReader reader = new BufferedReader(new FileReader(direccionAFN))) {
            alfabeto = reader.readLine().split(",");
            cantidadEstadosAFN = Integer.parseInt(reader.readLine());
            String[] estadosFinal = reader.readLine().split(",");
            estadosFinalAFN = new int[estadosFinal.length];
            for (int i = 0; i < estadosFinal.length; i++) {
                estadosFinalAFN[i] = Integer.parseInt(estadosFinal[i].trim());
            }

            String[] lambdas = reader.readLine().split(",");
            for (String cell : lambdas) {
                List<Integer> trans = new ArrayList<>();
                for (String p : cell.split(";")) {
                    if (!p.isEmpty()) trans.add(Integer.parseInt(p.trim()));
                }
                transicionesLambdaAFN.add(trans);
            }

            String line;
            while ((line = reader.readLine()) != null) {
                String[] cells = line.split(",");
                List<List<Integer>> row = new ArrayList<>();
                for (String cell : cells) {
                    List<Integer> vals = new ArrayList<>();
                    for (String p : cell.split(";")) {
                        if (!p.isEmpty()) vals.add(Integer.parseInt(p.trim()));
                    }
                    row.add(vals);
                }
                transicionesEstadosAFN.add(row);
            }
        } catch (IOException e) {
            System.err.println("Error leyendo AFN: " + direccionAFN);
        }
    }

    private void clausura_lambda(Set<Integer> conjuntoActual) {
        Set<Integer> clausura = new HashSet<>(conjuntoActual);
        Queue<Integer> cola = new LinkedList<>(conjuntoActual);
        while (!cola.isEmpty()) {
            int estado = cola.poll();
            for (int sig : transicionesLambdaAFN.get(estado)) {
                if (clausura.add(sig)) {
                    cola.add(sig);
                }
            }
        }

        if (!mapaClausuras.containsValue(clausura)) {
            mapaClausuras.put(contadorEstadosClausuras, clausura);
            conjuntoCreadosPendientes.add(clausura);
            contadorEstadosClausuras++;
        }
    }

    private Integer obtenerEstado(Set<Integer> conjunto) {
        for (Map.Entry<Integer, Set<Integer>> entry : mapaClausuras.entrySet()) {
            if (entry.getValue().equals(conjunto)) return entry.getKey();
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
    }
}
public static void main(String[] args) {
    AFN automata = new AFN("tests/afn/simple.afn");
    
    String[][] pruebas = {
        {"11111", "T"},
        {"11101", "F"},
        {"11111111110", "F"},
        {"01111111", "F"},
        {"111", "T"}
    };

    for (String[] par : pruebas) {
        String cadena = par[0];
        boolean esperado = par[1].equals("T");
        boolean obtenido = automata.accept(cadena);

        System.out.println("Cuerda: " + cadena +
            " | Esperado: " + esperado +
            " | Obtenido: " + obtenido +
            " --> " + (esperado == obtenido ? "✔" : "✘"));
    }
}

