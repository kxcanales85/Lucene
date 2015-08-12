package Buscador;

import Buscador.Lucene;
import java.io.IOException;
import java.sql.SQLException;
import org.apache.lucene.queryparser.classic.ParseException;


/**
 *
 * @authors Kevin Canales, Sebastián Ossandón, Gerson Aguirre.
 */

public class Buscador {
    public static void main(String[] args) throws IOException, SQLException, ParseException{
        String termino = "Comentario0", campo1 = "id_comentario", campo2 = "texto_comentario", tabla = "comentario";
        int hits = 20, crear = 0;
        /*La variable crear es una variable de condicion, que basicamente decide si 
        se crea nuevamente el indice, o no es necesario.
        1: para que se cree nuevamente
        0: para que no se cree y solamente se lea*/
        String[][] resultados = Lucene.buscar(termino,campo1,campo2,tabla,hits, crear);
        Lucene.mostrar(resultados);
    }
        
}