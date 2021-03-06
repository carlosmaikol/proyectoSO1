package proyecto_so_han_morales;

import java.util.concurrent.Semaphore;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import javax.swing.JOptionPane;
import static java.lang.Integer.parseInt; // Se utiliza para leer datos y realizar la conversion a int de manera mas eficaz

public class Gama {
    
    // ATRIBUTOS DE LA CLASE
    
    // private Client cliente[]; 
    public static Employee empleado[];
    public static Shelf estante[];
    private Interfaz interfaz;
    
    private int tiempoHora;    // Tiempo que dura un dia en el programa
    private int estantesMax;   // Maximo de los estantes
    private int capacidadMax;  // Capacidad maxima de productos por estante
    private int cajasMax;      // Maximo de cajas
    private int carritosMax;   // Maximo de carritos
    
    // Semaforos (EM = exclusion mutua, C = cliente, E = empleado)
    private Semaphore[] SEME, SCE, SEE;  // Semaforos de los estantes
    private Semaphore SCC;               // Semaforo del carrito de compras
    private Semaphore SEMCR, SCCR, SECR; // Semaforo de las cajas registradoras
    
    public static int[] pCE; // Apunta al estante para el cliente
    public static int[] pEE; // Apunta al estante para el empleado
    
    // Datos a mostrar en la interfaz grafica
    public static int clientes;            // Numero de clientes totales
    public static int clientesActivos = 0; // Contador de los clientes en el sistema 
    public static int clientesEspera = 0;  // Contador de los clientes  esperando
    public static int estantes;            // Contador de los estantes
    public static int carritos;            // Contador de los carritos
    public static int cajeros;             // Contador de los cajeros
    public static int horasLaboradas = 0;  // Contador de las horas que se han cursado en el dia
    public static int ganancias = 0;       // Contador de las ganancias
    
    private Gama gama;
    private Manager gerente;
    
    // CONSTRUCTOR DE LA CLASE 
    
    public Gama(Interfaz vista) {
        this.gama = this;
        this.interfaz = vista;
    }
    
    // METODOS DE LA CLASE
    
    /*
     * Metodo para leer los datos iniciales del archivo de texto
     */
    public void leerDatosIniciales() throws FileNotFoundException {
        
        Scanner doc = new Scanner(new File("DatosIniciales.txt"));
        String line = doc.nextLine();
        
        // Se procede a leer cada dato en el archivo y se guarda en su variable respectiva
        this.tiempoHora = parseInt(line.substring(20, 30).trim());
        System.out.println(this.tiempoHora);
        line = doc.nextLine();
        
        // Se tiene que verificar la validez de cada dato y corregirla de ser necesario
        if(this.tiempoHora < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");  
            tiempoHora = 100000;
        }
        
        Gama.estantes = parseInt(line.substring(20, 25).trim());
        System.out.println(Gama.estantes);
        line = doc.nextLine();
        
        if(Gama.estantes < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");
            estantes = 1;
        }
        
        this.estantesMax = parseInt(line.substring(20, 25).trim());
        System.out.println(this.estantesMax);
        line = doc.nextLine();
        
        if(this.estantesMax < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");  
            estantesMax = 3;
        }
        
        this.capacidadMax = parseInt(line.substring(20, 25).trim());
        System.out.println(this.capacidadMax);
        line = doc.nextLine();
        
        if(this.capacidadMax < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");  
            capacidadMax = 10;
        }
        
        Gama.cajeros = parseInt(line.substring(20, 25).trim());
        System.out.println(Gama.cajeros);
        line = doc.nextLine();
        
        if(Gama.cajeros < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");
            cajeros = 4;
        }
        
        this.cajasMax = parseInt(line.substring(20, 25).trim());
        System.out.println(this.cajasMax);
        line = doc.nextLine();
        
        if(this.cajasMax < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");  
            cajasMax = 10;
        }
        
        Gama.carritos = parseInt(line.substring(20, 25).trim());
        System.out.println(Gama.carritos);
        line = doc.nextLine();
        
        if(Gama.carritos < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido");  
            carritos = 10;
        }
        
        this.carritosMax = parseInt(line.substring(20, 25).trim());
        System.out.println(this.carritosMax);
            
        if(this.carritosMax < 0){
            JOptionPane.showMessageDialog(null, "Dato invalido"); 
            carritosMax = 20;
        }

    }
    
    /*
     * Metodo que crea los semaforos
    */
    public void CrearSemaforos() throws InterruptedException {
        
        // Semaforos de los estantes
        
        // Se crean los arreglos
        SEME = new Semaphore[this.estantesMax];
        SEE = new Semaphore[this.estantesMax];
        SCE = new Semaphore[this.estantesMax];
        
        // Se crean todos los semaforos
        for (int i = 0; i < SEME.length; i++) {
            this.SEME[i] = new Semaphore(1);
        }
        
        for (int i = 0; i < SEE.length; i++) {
            this.SEE[i] = new Semaphore(this.capacidadMax);
        }
        
        for (int i = 0; i < SCE.length; i++) {
            this.SCE[i] = new Semaphore(0);
        }
        
        // Semaforos de las cajas registradoras
        this.SEMCR = new Semaphore(1);
        this.SECR = new Semaphore(Gama.cajeros);
        this.SCCR = new Semaphore(0);
        
        // Semaforo de los carritos de compra
        this.SCC = new Semaphore(this.carritosMax);
        this.SCC.acquire(this.carritosMax - Gama.carritos);
        
    }
    
    /*
     *  Metodo para inicializar los apuntadores y estantes
    */
    public void inicializarValores() {
        
        // Gama.empleado = new Employee[this.estantesMax];
        
        // Se crean los estantes iniciales
        Gama.estante = new Shelf[this.estantesMax];     
        for (int i = 0; i < estantes; i++) {         
            Gama.estante[i] = new Shelf(capacidadMax, i);
        }
        
        // Se inicializan los apuntadores del cliente
        for (int i = 0; i < pCE.length; i++) {
            pCE[i] = 0;
        }
        
        // Se inicializan los apuntadores del empleado
        for (int i = 0; i < pEE.length; i++) {
            pEE[i] = 0;
        }
        
    }
    
    /*
     *  Metodo para crear hilos
    */
    public void crearHilo(int tipo) {
        
    }
    
    /*
     * Metodo para iniciar el simulador
    */
    public void Start() throws FileNotFoundException, InterruptedException{
        
        this.leerDatosIniciales();
        this.CrearSemaforos();
        this.inicializarValores();
        
        // Se muestra la data actual en la interfaz
        this.interfaz.getTxtCashier().setText(Integer.toString(cajeros));
        this.interfaz.getTxtEarnings().setText(Integer.toString(ganancias));
        this.interfaz.getTxtHours().setText(Integer.toString(horasLaboradas));
        this.interfaz.getTxtShelf().setText(Integer.toString(estantes));
        this.interfaz.getTxtShoppingCarts().setText(Integer.toString(carritos));
        
        // Se crean todos los hilos iniciales
        
        // los hilos de los empleados que no pueden ser mayor a la cantidad de estantes
        for (int i = 0; i < estantes; i++) {
            Employee e = new Employee();
            e.start();
        }
        
        
    }
    
    
    // GETTERS Y SETTERS

    public Interfaz getInterfaz() {
        return interfaz;
    }

    public void setInterfaz(Interfaz interfaz) {
        this.interfaz = interfaz;
    }

    public int getTiempoHora() {
        return tiempoHora;
    }

    public void setTiempoHora(int tiempoHora) {
        this.tiempoHora = tiempoHora;
    }

    public int getEstantesMax() {
        return estantesMax;
    }

    public void setEstantesMax(int estantesMax) {
        this.estantesMax = estantesMax;
    }

    public int getCapacidadMax() {
        return capacidadMax;
    }

    public void setCapacidadMax(int capacidadMax) {
        this.capacidadMax = capacidadMax;
    }

    public int getCajasMax() {
        return cajasMax;
    }

    public void setCajasMax(int cajasMax) {
        this.cajasMax = cajasMax;
    }

    public int getCarritosMax() {
        return carritosMax;
    }

    public void setCarritosMax(int carritosMax) {
        this.carritosMax = carritosMax;
    }

    public Semaphore[] getSEME() {
        return SEME;
    }

    public void setSEME(Semaphore[] SEME) {
        this.SEME = SEME;
    }

    public Semaphore[] getSCE() {
        return SCE;
    }

    public void setSCE(Semaphore[] SCE) {
        this.SCE = SCE;
    }

    public Semaphore[] getSEE() {
        return SEE;
    }

    public void setSEE(Semaphore[] SEE) {
        this.SEE = SEE;
    }

    public Semaphore getSCC() {
        return SCC;
    }

    public void setSCC(Semaphore SCC) {
        this.SCC = SCC;
    }

    public Semaphore getSEMCR() {
        return SEMCR;
    }

    public void setSEMCR(Semaphore SEMCR) {
        this.SEMCR = SEMCR;
    }

    public Semaphore getSCCR() {
        return SCCR;
    }

    public void setSCCR(Semaphore SCCR) {
        this.SCCR = SCCR;
    }

    public Semaphore getSECR() {
        return SECR;
    }

    public void setSECR(Semaphore SECR) {
        this.SECR = SECR;
    }

    public Gama getGama() {
        return gama;
    }

    public void setGama(Gama gama) {
        this.gama = gama;
    }

    public Manager getGerente() {
        return gerente;
    }

    public void setGerente(Manager gerente) {
        this.gerente = gerente;
    }

    public int getpCE(int index) {
        return pCE[index];
    }

    public void setpCE(int index, int value) {
        Gama.pCE[index] = value;
    }

    public int getpEE(int index) {
        return pEE[index];
    }

    public void setpEE(int index, int value) {
        Gama.pEE[index] = value;
    }
    
}
