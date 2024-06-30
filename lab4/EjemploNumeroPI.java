import java.util.concurrent.atomic.DoubleAdder;

// ===========================================================================
class Acumula {
// ===========================================================================
  double  suma;

  // -------------------------------------------------------------------------
  Acumula() {
    this.suma = 0;
  }

  // -------------------------------------------------------------------------
  synchronized void acumulaDato( double dato ) {
    this.suma +=dato;
  }

  // -------------------------------------------------------------------------
  synchronized double dameDato() {
    return this.suma;
  }
}

// ===========================================================================
class MiHebraMultAcumulaciones extends Thread {
// ===========================================================================
  int      miId, numHebras;
  long     numRectangulos;
  Acumula  a;

  // -------------------------------------------------------------------------
  MiHebraMultAcumulaciones( int miId, int numHebras, long numRectangulos,Acumula a ) {
    this.miId=miId;
    this.numHebras=numHebras;
    this.numRectangulos=numRectangulos;
    this.a = a;
  }

  // -------------------------------------------------------------------------
   public void run() {
    double baseRectangulo = 1.0 / ( ( double ) numRectangulos );
    for( long i = miId; i < numRectangulos; i+=numHebras ) {
      double x = baseRectangulo * ( ( ( double ) i ) + 0.5 );
      double suma = ( 4.0/( 1.0 + x*x ));
      a.acumulaDato(suma);
    }
  }
}

// ===========================================================================
class MiHebraUnaAcumulacion extends Thread {
// ===========================================================================
  int      miId, numHebras;
  long     numRectangulos;
  double sumaL;
  Acumula  a;

  MiHebraUnaAcumulacion( int miId, int numHebras, long numRectangulos,Acumula a ) {
    this.miId=miId;
    this.numHebras=numHebras;
    this.numRectangulos=numRectangulos;
    this.sumaL = 0.0;
    this.a = a;
  }
  public void run() {
    double baseRectangulo = 1.0 / ( ( double ) numRectangulos );
    for( long i = miId; i < numRectangulos; i+=numHebras ) {
      double x = baseRectangulo * ( ( ( double ) i ) + 0.5 );
      sumaL +=( 4.0/( 1.0 + x*x ) );
    }
    a.acumulaDato(sumaL);
  }

}

// ===========================================================================
class MiHebraMultAcumulacionAtomica extends Thread {
  DoubleAdder suma;
  int      miId, numHebras;
  long     numRectangulos;


  MiHebraMultAcumulacionAtomica( int miId, int numHebras, long numRectangulos,DoubleAdder a ) {
    this.miId=miId;
    this.numHebras=numHebras;
    this.numRectangulos=numRectangulos;
    this.suma = a;
  }
// ===========================================================================
   public void run() {
    double baseRectangulo = 1.0 / ( ( double ) numRectangulos );
    for( long i = miId; i < numRectangulos; i+=numHebras ) {
      double x = baseRectangulo * ( ( ( double ) i ) + 0.5 );
      suma.add( 4.0/( 1.0 + x*x ) );
    }
    suma.sum();
  }
}

// ===========================================================================
class MiHebraUnaAcumulacionAtomica extends Thread {
  DoubleAdder suma;
  int      miId, numHebras;
  long     numRectangulos;
  MiHebraUnaAcumulacionAtomica( int miId, int numHebras, long numRectangulos,DoubleAdder a ) {
    this.miId=miId;
    this.numHebras=numHebras;
    this.numRectangulos=numRectangulos;
    this.suma = a;
  }
  // ===========================================================================
  public void run() {
    double baseRectangulo = 1.0 / ( ( double ) numRectangulos );
    for( long i = miId; i < numRectangulos; i+=numHebras ) {
      double x = baseRectangulo * ( ( ( double ) i ) + 0.5 );
      suma.add( 4.0/( 1.0 + x*x ) );
    }
  }
// ===========================================================================
// ...
}



// ===========================================================================
class EjemploNumeroPI {
// ===========================================================================

  // -------------------------------------------------------------------------
  public static void main( String args[] ) {
    long                        numRectangulos;
    double                      baseRectangulo, x, suma, pi;
    int                         numHebras;
    long                        t1, t2;
    double                      tSec, tPar;
    Acumula                     a;
    MiHebraMultAcumulaciones  vt[];

    // Comprobacion de los argumentos de entrada.
    if( args.length != 2 ) {
      System.out.println( "ERROR: numero de argumentos incorrecto.");
      System.out.println( "Uso: java programa <numHebras> <numRectangulos>" );
      System.exit( -1 );
    }
    try {
      numHebras      = Integer.parseInt( args[ 0 ] );
      numRectangulos = Long.parseLong( args[ 1 ] );
    } catch( NumberFormatException ex ) {
      numHebras      = -1;
      numRectangulos = -1;
      System.out.println( "ERROR: Numeros de entrada incorrectos." );
      System.exit( -1 );
    }

    System.out.println();
    System.out.println( "Calculo del numero PI mediante integracion." );

    //
    // Calculo del numero PI de forma secuencial.
    //
    System.out.println();
    System.out.println( "Comienzo del calculo secuencial." );
    t1 = System.nanoTime();
    baseRectangulo = 1.0 / ( ( double ) numRectangulos );
    suma           = 0.0;
    for( long i = 0; i < numRectangulos; i++ ) {
      x = baseRectangulo * ( ( ( double ) i ) + 0.5 );
      suma += f( x );
    }
    pi = baseRectangulo * suma;
    t2 = System.nanoTime();
    tSec = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Version secuencial. Numero PI: " + pi );
    System.out.println( "Tiempo secuencial (s.):        " + tSec );

    //
    // Calculo del numero PI de forma paralela: 
    // Multiples acumulaciones por hebra.
    //
    System.out.println();
    System.out.print( "Comienzo del calculo paralelo: " );
    System.out.println( "Multiples acumulaciones por hebra." );
    t1 = System.nanoTime();
    a = new Acumula();
    vt = new MiHebraMultAcumulaciones[numHebras];
    for (int i = 0; i<numHebras;i++){
      MiHebraMultAcumulaciones v = new MiHebraMultAcumulaciones(i,numHebras,numRectangulos,a);
      vt[i] = v;
      v.start();
    }
    for (MiHebraMultAcumulaciones miHebra : vt) {
      try {
        miHebra.join();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    pi=a.dameDato()*baseRectangulo;
    t2 = System.nanoTime();
    tPar = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Calculo del numero PI:   " + pi );
    System.out.println( "Tiempo ejecucion (s.):   " + tPar );
    System.out.println( "Incremento velocidad :   " + tSec/tPar  );

    //
    // Calculo del numero PI de forma paralela: 
    // Una acumulacion por hebra.
    //
    System.out.println();
    System.out.print( "Comienzo del calculo paralelo: " );
    System.out.println( "Una acumulacion por hebra." );
    t1 = System.nanoTime();
    a = new Acumula();
    MiHebraUnaAcumulacion[] vt2;
    vt2 = new MiHebraUnaAcumulacion[numHebras];
    for (int i = 0; i<numHebras;i++){
      MiHebraUnaAcumulacion v = new MiHebraUnaAcumulacion(i,numHebras,numRectangulos,a);
      vt2[i] = v;
      v.start();
    }
    for (MiHebraUnaAcumulacion miHebra : vt2) {
      try {
        miHebra.join();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    pi=a.dameDato()*baseRectangulo;
    t2 = System.nanoTime();
    tPar = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Calculo del numero PI:   " + pi );
    System.out.println( "Tiempo ejecucion (s.):   " + tPar );
    System.out.println( "Incremento velocidad :   " + tSec/tPar );


    //
    // Calculo del numero PI de forma paralela: 
    // Multiples acumulaciones por hebra (Atomica)
    //
    System.out.println();
    System.out.print( "Comienzo del calculo paralelo: " );
    System.out.println( "Multiples acumulaciones por hebra (At)." );
    t1 = System.nanoTime();
    DoubleAdder b = new DoubleAdder();
    MiHebraMultAcumulacionAtomica[] vt3;
    vt3 = new MiHebraMultAcumulacionAtomica[numHebras];
    for (int i = 0; i<numHebras;i++){
      MiHebraMultAcumulacionAtomica v = new MiHebraMultAcumulacionAtomica(i,numHebras,numRectangulos,b);
      vt3[i] = v;
      v.start();
    }
    for (MiHebraMultAcumulacionAtomica miHebra : vt3) {
      try {
        miHebra.join();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    pi=(b.doubleValue()*baseRectangulo);
    t2 = System.nanoTime();
    tPar = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Calculo del numero PI:   " + pi );
    System.out.println( "Tiempo ejecucion (s.):   " + tPar );
    System.out.println( "Incremento velocidad :   " + tSec/tPar );



    //
    // Calculo del numero PI de forma paralela: 
    // Una acumulacion por hebra (Atomica).
    //
    System.out.println();
    System.out.print( "Comienzo del calculo paralelo: " );
    System.out.println( "Una acumulacion por hebra (At)." );
    t1 = System.nanoTime();
    b = new DoubleAdder();
    MiHebraUnaAcumulacionAtomica[] vt4;
    vt4 = new MiHebraUnaAcumulacionAtomica[numHebras];
    for (int i = 0; i<numHebras;i++){
      MiHebraUnaAcumulacionAtomica v = new MiHebraUnaAcumulacionAtomica(i,numHebras,numRectangulos,b);
      vt4[i] = v;
      v.start();
    }
    for (MiHebraUnaAcumulacionAtomica miHebra : vt4) {
      try {
        miHebra.join();
      } catch (InterruptedException ex) {
        ex.printStackTrace();
      }
    }
    pi=(b.sum()*baseRectangulo);
    t2 = System.nanoTime();
    tPar = ( ( double ) ( t2 - t1 ) ) / 1.0e9;
    System.out.println( "Calculo del numero PI:   " + pi );
    System.out.println( "Tiempo ejecucion (s.):   " + tPar );
    System.out.println( "Incremento velocidad :   " + tSec/tPar );

    System.out.println();
    System.out.println( "Fin de programa." );
  }

  // -------------------------------------------------------------------------
  static double f( double x ) {
    return ( 4.0/( 1.0 + x*x ) );
  }
}

