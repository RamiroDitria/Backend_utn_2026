import java.util.Scanner;

public class Ejercicio1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int op;
        op = 0;
        String a, b;
        a = "*";
        b = " ";
        int i, j;
        System.out.print("Ingrese el numero de figura deseada solicitada: ");
        op = scanner.nextInt();
        switch (op) {
            case (1):
                j = 1;
                i = 1;
                for (i = 1; i < 5; i++) {

                    for (j = 1; j < 5; j++) {


                        System.out.print(a + b);
                    }
                    System.out.println(" ");
                    System.out.println(" ");

                }
                break;
            case (2):
                for (i = 1; i < 5; i++) {
                    for (j = 1; j < 5; j++) {
                        System.out.print(a + b);
                    }
                    if (i % 2 == 0) {
                        System.out.println(b);
                        System.out.println(b);
                    } else {
                        System.out.println(b);
                        System.out.println(b);
                        System.out.print(b);
                    }
                }
                    break;
            case (3):
                for (i = 1;  i < 5; i++){
                    for (j = 1 ; j<=i ; j++){
                        System.out.print(a + b);

                        }
                        System.out.println(b);
                        System.out.println(b);
                    }
                    break;
            case (4) :
                // Hacemos que recorra las 7 filas
                for (i = 1; i <= 9; i++) {

                    if (i <= 5) {
                        // PARTE ASCENDENTE (Filas 1 a 4)
                        for (j = 1; j <= i; j++) {
                            System.out.print("* ");
                        }
                    } else {
                        // PARTE DESCENDENTE (Filas 5 a 7)
                        // Si i=6, queremos 4 asteriscos -> 10 - 6 = 4
                        // Si i=7, queremos 2 asteriscos -> 10 - 7 = 3
                        for (j = 1; j <= (10 - i); j++) {
                            System.out.print(a + b);
                        }
                    }

                    System.out.println(); // Un solo salto de línea para que no quede tan separado
                    System.out.println();
                }
                break;    }}}