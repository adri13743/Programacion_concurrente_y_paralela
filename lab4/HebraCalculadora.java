package la04;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

public class HebraCalculadora extends Thread{
    private JTextField  txfMensajes;
    private AtomicBoolean fin;

    public HebraCalculadora(JTextField txfMensajes, AtomicBoolean fin) {
        this.txfMensajes = txfMensajes;
        this.fin = fin;
    }

    @Override
    public void run() {
        long i = 1L ;
          while (!fin.get()) {
            if (esPrimo(i)) {
                final long fnum = i;
                try {
                    SwingUtilities.invokeAndWait(new Runnable() {
                        public void run() {
                            txfMensajes.setText(Long.valueOf(fnum).toString());
                        }
                    });
                } catch (InterruptedException | InvocationTargetException e) {
                    throw new RuntimeException(e);
                }
            }
            i ++;
          }
    }

    static boolean esPrimo( long num ) {
        boolean primo;
        if( num < 2 ) {
            primo = false;
        } else {
            primo = true;
            long i = 2;
            while( ( i < num )&&( primo ) ) {
                primo = ( num % i != 0 );
                i++;
            }
        }
        return( primo );
    }
}


