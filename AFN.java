import java.io.*;
import java.util.*;

/*
	Utilice esta clase para guardar la informacion de su
	AFN. NO DEBE CAMBIAR LOS NOMBRES DE LA CLASE NI DE LOS 
	METODOS que ya existen, sin embargo, usted es libre de 
	agregar los campos y metodos que desee.
*/
public class AFN{
	//Lectura AFN.
	String direccionAFN;
	String[] alfabeto;
	int cantidadEstadosAFN;
	String[] estadosFinalAFN;
	List<List<String>> transicionesLambda;
	List<List<List<String>>> transicionesEstados;
	//Algoritmo.
	Queue<ArrayList<Integer>> clausuraLambda1 = new LinkedList<>();
    Queue<ArrayList<Integer>> clausuraLambda2 = new LinkedList<>();
    Map<Integer, ArrayList<Integer>> mapaClausuras = new HashMap<>();
    int contadorClausuras = 1;
	//Lectura cuerdas.
	Boolean cuerdaAceptada;


	/*
		Implemente el constructor de la clase AFN
		que recibe como argumento un string que 
		representa el path del archivo que contiene
		la informacion del AFN (i.e. "Documentos/archivo.AFN").
		Puede utilizar la estructura de datos que desee
	*/
	public AFN(String path){
		this.direccionAFN = path;
		this.transicionesLambda = new ArrayList<>();
		this.transicionesEstados = new ArrayList<>();
		this.cuerdaAceptada = false;
	}

	/*
		Implemente el metodo accept, que recibe como argumento
		un String que representa la cuerda a evaluar, y devuelve
		un boolean dependiendo de si la cuerda es aceptada o no 
		por el AFN. Recuerde lo aprendido en el proyecto 1.
	*/
	public boolean accept(String string){
		return false;
	}

	/*
		Implemente el metodo toAFD. Este metodo debe generar un archivo
		de texto que contenga los datos de un AFD segun las especificaciones
		del proyecto.
	*/
	
	public void toAFD(String afdPath){
		lecturaAFN(); // Asegura que el AFN esté cargado
	
		// Inicializar estructuras
		Map<Integer, Map<String, Integer>> afdTransiciones = new HashMap<>();
		Set<Integer> afdFinales = new HashSet<>();
		List<String> alfabetoAFD = Arrays.asList(alfabeto);
	
		// Estado inicial del AFD: clausura-lambda del estado 0
		ArrayList<Integer> estadoInicialAFN = new ArrayList<>();
		estadoInicialAFN.add(0);
		int estadoInicialAFD = clausura_lambda(estadoInicialAFN);
	
		while (!clausuraLambda1.isEmpty()) {
			ArrayList<Integer> estadoAFN = clausuraLambda1.poll();
			int estadoAFD = getKeyFromValue(estadoAFN);
	
			// Transiciones para cada símbolo del alfabeto
			for (int simboloIndex = 0; simboloIndex < alfabetoAFD.size(); simboloIndex++) {
				String simbolo = alfabetoAFD.get(simboloIndex);
				Set<Integer> mov = new HashSet<>();
	
				for (int estado : estadoAFN) {
					List<String> destinos = transicionesEstados.get(estado).get(simboloIndex);
					for (String destino : destinos) {
						if (!destino.isEmpty()) {
							mov.add(Integer.parseInt(destino));
						}
					}
				}
	
				ArrayList<Integer> movList = new ArrayList<>(mov);
				if (!movList.isEmpty()) {
					int clausuraDestino = clausura_lambda(movList);
	
					if (!afdTransiciones.containsKey(estadoAFD)) {
						afdTransiciones.put(estadoAFD, new HashMap<>());
					}
					afdTransiciones.get(estadoAFD).put(simbolo, clausuraDestino);
				}
			}
		}
	
		// Identificar estados finales en el AFD
		for (Map.Entry<Integer, ArrayList<Integer>> entry : mapaClausuras.entrySet()) {
			int estadoAFD = entry.getKey();
			ArrayList<Integer> grupoAFN = entry.getValue();
	
			for (String estadoFinalStr : estadosFinalAFN) {
				int estadoFinal = Integer.parseInt(estadoFinalStr);
				if (grupoAFN.contains(estadoFinal)) {
					afdFinales.add(estadoAFD);
					break;
				}
			}
		}
	
		// Escribir el archivo .afd con el nuevo formato solicitado
		try (PrintWriter writer = new PrintWriter(new FileWriter(afdPath))) {
			// Línea 1: Alfabeto
			writer.println(String.join(",", alfabeto));
	
			// Línea 2: Número de estados
			writer.println(mapaClausuras.size());
	
			// Línea 3: Estados finales
			List<String> finales = new ArrayList<>();
			for (int f : afdFinales) {
				finales.add(String.valueOf(f));
			}
			writer.println(String.join(",", finales));
	
			// Transiciones por símbolo (una línea por símbolo)
			for (String simbolo : alfabeto) {
				List<String> linea = new ArrayList<>();
				for (int i = 1; i <= mapaClausuras.size(); i++) {
					Map<String, Integer> transiciones = afdTransiciones.getOrDefault(i, new HashMap<>());
					Integer destino = transiciones.get(simbolo);
					linea.add(destino != null ? destino.toString() : "");
				}
				writer.println(String.join(",", linea));
			}
	
		} catch (IOException e) {
			System.out.println("Error al escribir el archivo: " + afdPath);
			e.printStackTrace();
		}
	}
	
	// Método auxiliar
	private int getKeyFromValue(ArrayList<Integer> valor) {
		for (Map.Entry<Integer, ArrayList<Integer>> entry : mapaClausuras.entrySet()) {
			if (entry.getValue().equals(valor)) {
				return entry.getKey();
			}
		}
		return -1;
	}
	


	/*
		El metodo main debe recibir como primer argumento el path
		donde se encuentra el archivo ".afd" y debe empezar a evaluar 
		cuerdas ingresadas por el usuario una a una hasta leer una cuerda vacia (""),
		en cuyo caso debe terminar. Tiene la libertad de implementar este metodo
		de la forma que desee. Si se envia la bandera "-to-afd", entonces en vez de
		evaluar, debe generar un archivo .afd
	*/
	public static void main(String[] args) throws Exception{
		
	}
	
	private void lecturaAFN(){
		try(BufferedReader lector = new BufferedReader(new FileReader(this.direccionAFN))){
			//Lectura de alfabeto.
			alfabeto = lector.readLine().split(",");
			//Lectura de la cantidad de estados del AFN.
			cantidadEstadosAFN = Integer.parseInt(lector.readLine());
			//Lectura de los estados finales del AFN.
			String[] estadosFinalString = lector.readLine().split(",");
			estadosFinalAFN = new String[estadosFinalString.length];
			for(int i=0; i < estadosFinalString.length; i++){
				estadosFinalAFN[i] = estadosFinalString[i];
			}
			//Lectura de transiciones con lambda.
			String[] lambdaStrings = lector.readLine().split(",");
			for(String celda : lambdaStrings){
				List<String> grupo = new ArrayList<>();
				for(String parte : celda.split(";")){
					if(!parte.isEmpty()){
						grupo.add(parte);
					}
				}
				transicionesLambda.add(grupo);
			}
			//Lectura de transiciones con estados.
			String linea;
			while ((linea=lector.readLine())!=null) {
				String[] celdas = linea.split(",");

				List<List<String>> fila = new ArrayList<>();
				for(String celda : celdas){
					List<String> valores = new ArrayList<>();
					String[] partes = celda.split(";");
					for(String parte : partes){
						if (!parte.isEmpty()) {
							valores.add(parte);
						}
					}
					fila.add(valores);
				}
				transicionesEstados.add(fila);
			}
		} catch(IOException e){
			System.out.println("Error al leer el archivo: " + direccionAFN);
			e.printStackTrace();
		}
	}
	//Clausura-lamda.
	public int clausura_lambda(ArrayList<Integer> conjuntoEstados) {
		Set<Integer> nuevaClausura = new HashSet<>(conjuntoEstados);
		Stack<Integer> pila = new Stack<>();
		pila.addAll(conjuntoEstados);
	
		while (!pila.isEmpty()) {
			int estado = pila.pop();
	
			List<String> destinos = transicionesLambda.get(estado);
	
			for (String destinoStr : destinos) {
				int destino = Integer.parseInt(destinoStr);
				if (nuevaClausura.add(destino)) {
					pila.push(destino);
				}
			}
		}
	
		ArrayList<Integer> clausuraFinal = new ArrayList<>(nuevaClausura);
		Collections.sort(clausuraFinal);
	
		// Verificar si ya existe en el mapa
		for (Map.Entry<Integer, ArrayList<Integer>> entry : mapaClausuras.entrySet()) {
			if (entry.getValue().equals(clausuraFinal)) {
				return entry.getKey(); // Ya existía, devolver su número
			}
		}
	
		// Si no existe, lo agregamos
		mapaClausuras.put(contadorClausuras, clausuraFinal);
		clausuraLambda1.offer(clausuraFinal);
		return contadorClausuras++; // Devolver el número asignado
	}
	
}