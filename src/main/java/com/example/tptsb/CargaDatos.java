package com.example.tptsb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class CargaDatos {
    final static int SEXO = 0;
    final static int TIPOVACUNA = 1;
    final static int ORDENDOSIS = 2;

/*El método carga() asigna en el objeto tabla de la clase TSBHashTableDA, la tabla retornada por el método crearTabla(),
 informando al principio y al final de la carga de la misma, al usuario. Retorna el objeto tabla.
 */
    public TSBHashTableDA carga() throws FileNotFoundException {


        long inicio = System.currentTimeMillis();
        System.out.println("Cargando datos...Espere un momento");

        TSBHashTableDA<String, TSBHashTableDA<String, Acumulador>[]> tabla = crearTabla();

        System.out.println("Datos cargados!!!");
        long finalT = System.currentTimeMillis();

        System.out.println("Creacion de la tabla finalizada en: " + (finalT - inicio) / 1000 + "segundos");
        return tabla;
    }

/*El método buscarDatos() recibe como parámetros un int que identifica al tipo de filtro que se seleccionó para aplicar (sexo, tipovacuna, ordendosis), un String rango que indica si los datos que se quieren obtener son por departamento o por toda la provincia y la tabla TSBHashTableDA que contiene todos los conteos.
En el caso de que el rango sea Todas los departamentos, por cada departamento se recorrera cada contador del filtro seleccionado y se guardará el dato en la tablaAcumuladora de la clase TSBHashTableDA, que calculará el valor total de todas los departamentos.
Si el rango elegido fuese un departamento en particular, solo se recorrerían los filtros de ese departamento en particular, recuperando los valores totales del mismo.
 */
    public static TSBHashTableDA<String, Acumulador> buscarDatos(int filtro, String rango, TSBHashTableDA tabla) {
        Set<Map.Entry<String, TSBHashTableDA<String, Acumulador>[]>> recorrerDepto = tabla.entrySet();
        Iterator<Map.Entry<String, TSBHashTableDA<String, Acumulador>[]>> itDepto = recorrerDepto.iterator();

        Set<Map.Entry<String, Acumulador>> recorrerFiltro;
        Iterator<Map.Entry<String, Acumulador>> itFiltro;

        TSBHashTableDA<String, Acumulador> tablaAcumuladora = new TSBHashTableDA<>();


        if (rango == "Todos los departamentos") {

            while (itDepto.hasNext()) {

                Map.Entry<String, TSBHashTableDA<String, Acumulador>[]> deptoEntry = itDepto.next();
                TSBHashTableDA<String, Acumulador>[] depto = deptoEntry.getValue();

                recorrerFiltro = depto[filtro].entrySet();
                itFiltro = recorrerFiltro.iterator();

                while (itFiltro.hasNext()) {
                    Map.Entry<String, Acumulador> ac = itFiltro.next();

                    Acumulador acTabla = tablaAcumuladora.get(ac.getValue().getId());
                    if (acTabla == null) {
                        acTabla = new Acumulador();
                        acTabla.setId(ac.getValue().getId());
                        acTabla.setTotal(ac.getValue().getTotal());
                        tablaAcumuladora.put(ac.getValue().getId(), acTabla);
                    }
                    else {

                        acTabla.setTotal(acTabla.getTotal() + ac.getValue().getTotal());
                        tablaAcumuladora.put(ac.getValue().getId(), acTabla);
                    }
                }
            }
        }
        else {
            String nomDepto = '"' + rango + '"';
            TSBHashTableDA<String, Acumulador>[] depto = (TSBHashTableDA<String, Acumulador>[]) tabla.get(nomDepto);
            recorrerFiltro = depto[filtro].entrySet();
            itFiltro = recorrerFiltro.iterator();
            while (itFiltro.hasNext()) {
                Map.Entry<String, Acumulador> ac = itFiltro.next();

                Acumulador acTabla = tablaAcumuladora.get(ac.getValue().getId());

                if (acTabla == null) {
                    tablaAcumuladora.put(ac.getValue().getId(), ac.getValue());
                } else {
                    acTabla.setTotal(acTabla.getTotal() + ac.getValue().getTotal());
                    tablaAcumuladora.put(ac.getValue().getId(), acTabla);
                }
                ;
            }
        }
        return tablaAcumuladora;
    }

/*El método crearTabla() crea un objeto de tipo File, dada la ruta donde se encuentre el archivo con los registros. Luego se crea el objeto tabla de la clase TSBHashTableDA que está compuesta por un String y por otra tabla TSBHashTableDA (la cual a su vez, estará conformada por un String y un objeto de la clase Acumulador, que llevará el proceso de conteo por cada categoría).
Se recorrerá registro por registro el archivo y por cada columna (separada por una coma), buscando aquellos registros cuya provincia sea Córdoba y de ser ese el caso, primero: tomará el string que indiqué la localidad y se fijará si ya existe la tabla TSBHashTableDA en ese índice (el calculado con el hashcode), sino existe lo crea (con tablas TSBHashTableDA por cada uno de los tipos de atributo que nos interese llevar un control, con su respectivo contador), de existir pasa a lo siguiente.
Segundo: por cada columna a la que se quiere mantener un conteo, se accede a la subTabla del tipo de atributo (sexo, ordendosis, tipovacuna), de no existir el acumulador de ese campo (en caso de tipovacuna, el acumulador para "sputnik"), se crea el objeto de la clase Acumulador y se llama al método incrementar() para sumar uno al conteo.
*/

    public static TSBHashTableDA crearTabla() throws FileNotFoundException {
        File arch = new File("C:\\Users\\tinch\\OneDrive\\Escritorio\\TPU_Grupo2_HashTableDA\\out\\production\\TPU_Grupo2_HashTableDA\\datos_nomivac_covid19.csv");
        Scanner sc = new Scanner(arch);
        TSBHashTableDA<String, TSBHashTableDA<String, Acumulador>[]> tabla = new TSBHashTableDA<>(300, 0.5f);

        while (sc.hasNextLine()) {



            String h = sc.nextLine();
            String[] cadena = h.split(",");
            if (cadena[6].equals('"' + "Córdoba" + '"')) {
                String idDepto = cadena[8];

                TSBHashTableDA<String, Acumulador>[] subTablaDepto = tabla.get(idDepto);
                TSBHashTableDA<String, Acumulador> subTablaSexo;
                TSBHashTableDA<String, Acumulador> subTablaDosis;
                TSBHashTableDA<String, Acumulador> subTablaVacuna;

                if (subTablaDepto == null) {
                    subTablaDepto = new TSBHashTableDA[3];

                    subTablaSexo = new TSBHashTableDA<>();
                    subTablaDosis = new TSBHashTableDA<>();
                    subTablaVacuna = new TSBHashTableDA<>();

                    Acumulador acSexo = new Acumulador();
                    acSexo.setId(cadena[0]);
                    Acumulador acDosis = new Acumulador();
                    acDosis.setId(cadena[13]);
                    Acumulador acVacuna = new Acumulador();
                    acVacuna.setId(cadena[11]);

                    subTablaSexo.put(cadena[0], acSexo);
                    subTablaDosis.put(cadena[13], acDosis);
                    subTablaVacuna.put(cadena[11], acVacuna);

                    subTablaDepto[SEXO] = subTablaSexo;
                    subTablaDepto[ORDENDOSIS] = subTablaDosis;
                    subTablaDepto[TIPOVACUNA] = subTablaVacuna;


                    tabla.put(idDepto, subTablaDepto);
                } else {
                    subTablaSexo = subTablaDepto[SEXO];
                    Acumulador acSexo = subTablaSexo.get(cadena[0]);
                    if (acSexo == null) {
                        acSexo = new Acumulador();
                        acSexo.setId(cadena[0]);
                        subTablaSexo.put(cadena[0], acSexo);
                    } else {
                        acSexo.incrementar();
                    }

                    subTablaDosis = subTablaDepto[ORDENDOSIS];
                    Acumulador acDosis = subTablaDosis.get(cadena[13]);
                    if (acDosis == null) {
                        acDosis = new Acumulador();
                        acDosis.setId(cadena[13]);
                        subTablaDosis.put(cadena[13], acDosis);
                    } else {
                        acDosis.incrementar();
                    }

                    subTablaVacuna = subTablaDepto[TIPOVACUNA];
                    Acumulador acVacuna = subTablaVacuna.get(cadena[11]);
                    if (acVacuna == null) {
                        acVacuna = new Acumulador();
                        acVacuna.setId(cadena[11]);
                        subTablaVacuna.put(cadena[11], acVacuna);
                    } else {
                        acVacuna.incrementar();
                    }

                }
            }
        }
        return tabla;
    }
}