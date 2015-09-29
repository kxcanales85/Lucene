package Buscador;

import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;

/**
 *
 * @author Kevin Canales
 */
public class Lucene {
    
    /*Declaración de los string necesarios para las rutas de conexion de mysql*/
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/memento";
    
    /*Declaracion de las credenciales necesarias*/
    static final String USER = "root";
    static final String PASS = "123miel";
    
    
    private static String crearConsulta(String campo1, String campo2, String tabla){
        String consulta;
        consulta = "SELECT " + campo1 + ", " + campo2 + " FROM " + tabla;
        System.out.println(consulta);
        return consulta;
    }
    
    private static String ejecutarConsulta(Connection conn, Statement stmt, ResultSet rs, String sql, String campo){
        String resultado = "";
        try{
            Class.forName(JDBC_DRIVER);/*Le pasamos el nombre del driver*/
            //System.out.println("Conectando a la base de datos");
            conn = DriverManager.getConnection(DB_URL,USER,PASS); /*Obtenemos la conexion con la url de la DB, y las credenciales necesarias*/
            stmt = conn.createStatement(); /*Obtenemos la declaración*/
            //System.out.println("La consulta es: " + sql+ " y el campo es: "+campo);
            rs = stmt.executeQuery(sql); /*Ejecutamos la consulta*/
            //System.out.println("ejecute la consulta");
            while(rs.next()){
                resultado = rs.getString(campo);
            }
            //resultado = rs.getString("id_usuario");
            System.out.println("Realice la consulta con total normalidad y retornó: "+resultado);
            
        }
        /****** DECLARACIONES DE TODOS LOS CATCH Y FINALLY NECESARIOS ******/ 
        catch(SQLException | ClassNotFoundException se){
            System.out.println("ERROR: en la funcion 'ejecutarConsulta'");
        }
        return resultado;
    }
    
    public static void ordenar(String matriz[][], int izq, int der) {
        int pivote = Integer.parseInt(matriz[izq][2]);
        int i = izq; 
        int j = der;
        String aux;
 
        while(i<j){            // mientras no se crucen las búsquedas
            while(Integer.parseInt(matriz[i][2])<=pivote && i<j){
                i++;
                int auxiliar = Integer.parseInt(matriz[j][2]);
                while(auxiliar > pivote){
                    j--;
                    if (i<j) {
                        aux = matriz[i][2];
                        matriz[i][2] = matriz[j][2];
                        matriz[j][2] = aux;
                    }
                }
            }
        }
        matriz[izq][2] = matriz[j][2];
        matriz[j][2]=Integer.toString(pivote);
        if(izq < j-1){
            ordenar(matriz,izq,j-1);
        }
        if(j+1 <der){
            ordenar(matriz,j+1,der);
        }
    }
    
    private static int calculaPuntaje(String id, Connection conn, Statement stmt, ResultSet rs){
        int puntaje = 0;
        String cAmistoso, cComentador, cImagenes, cImagenesF, cImagenesC;
        cAmistoso = "Select seguidores from usuario where id_usuario = "+id+";";
        cAmistoso = ejecutarConsulta(conn, stmt, rs, cAmistoso, "seguidores");
        cComentador = "Select comentarios_positivos from usuario where id_usuario = "+id+";";
        cComentador = ejecutarConsulta(conn, stmt, rs, cComentador, "comentarios_positivos");
        cImagenes = "Select count(*) from imagen where id_usuario = "+id+";";
        cImagenes = ejecutarConsulta(conn, stmt, rs, cImagenes, "count(*)");
        cImagenesF = "call PuntajeParaFavoritos("+id+");";
        cImagenesF = ejecutarConsulta(conn, stmt, rs, cImagenesF, "count(*)");
        cImagenesC = ""; /*Aun no existe*/
        int amistoso = 0, comentador = 0, imagenes = 0, imagenesF = 0, imagenesC = 0;
        amistoso = Integer.parseInt(cAmistoso);
        comentador = Integer.parseInt(cComentador);
        imagenes = Integer.parseInt(cImagenes);
        imagenesF = Integer.parseInt(cImagenesF);
        //imagenesC = Integer.parseInt(cImagenesC);
        /*Puntajes por amistad*/
        if(amistoso < 15){
            puntaje = puntaje + 1;
        }
        else {
            if(amistoso >= 15 || amistoso < 50){
                puntaje = puntaje + 2;
            }
            else {
                if(amistoso >= 50 || amistoso < 100){
                    puntaje = puntaje + 3;
                }
                else {
                    puntaje = puntaje + 4;
                }
            }
        }
        /*Puntajes por comentar*/
        if(comentador < 20){
            puntaje = puntaje + 1;
        }
        else {
            if(comentador >= 20 || comentador < 40){
                puntaje = puntaje + 2;
            }
            else {
                if(comentador >= 40 || comentador < 60){
                    puntaje = puntaje + 3;
                }
                else {
                    puntaje = puntaje + 4;
                }
            }
        }
        /*Puntajes por imagenes subidas*/
        if(imagenes < 10){
            puntaje = puntaje + 1;
        }
        else {
            if(imagenes >= 10 || imagenes < 20){
                puntaje = puntaje + 2;
            }
            else {
                if(imagenes >= 20 || imagenes < 100){
                    puntaje = puntaje + 3;
                }
                else {
                    puntaje = puntaje + 4;
                }
            }
        }
        /*Puntajes por imagenes con favoritos*/
        if(imagenesF == 0){
            puntaje = puntaje + 1;
        }
        else {
            if(imagenesF == 1){
                puntaje = puntaje + 2;
            }
            else {
                if(imagenesF > 2 || imagenesF < 10){
                    puntaje = puntaje + 3;
                }
                else {
                    puntaje = puntaje + 4;
                }
            }
        }
        /*Puntajes por imagenes comentadas*/
        if(imagenesC == 0){
            puntaje = puntaje + 1;
        }
        else {
            if(imagenesC == 1){
                puntaje = puntaje + 2;
            }
            else {
                if(imagenesC > 2 || imagenesC < 10){
                    puntaje = puntaje + 3;
                }
                else {
                    puntaje = puntaje + 4;
                }
            }
        }
        return puntaje;
    }
    
    private static ResultSet conectar(Connection conn, Statement stmt, ResultSet rs, String campo1, String campo2, String tabla){
        try{
            Class.forName(JDBC_DRIVER);/*Le pasamos el nombre del driver*/
            System.out.println("Conectando a la base de datos");
            conn = DriverManager.getConnection(DB_URL,USER,PASS); /*Obtenemos la conexion con la url de la DB, y las credenciales necesarias*/
            stmt = conn.createStatement(); /*Obtenemos la declaración*/
            String sql; /*Creamos el string para la consulta a ejecutar en la DB*/
            sql = crearConsulta(campo1, campo2, tabla);
            rs = stmt.executeQuery(sql); /*Ejecutamos la consulta*/
            System.out.println("Realice la consulta con total normalidad");
            
        }
        /****** DECLARACIONES DE TODOS LOS CATCH Y FINALLY NECESARIOS ******/ 
        catch(SQLException | ClassNotFoundException se){
            System.out.println("ERROR: en la funcion 'conectar'");
        }
        return rs;
    }
    
    private static void desconectar(ResultSet rs, Statement stmt, Connection conn){
        try{
            if(stmt != null && conn != null && rs != null){
                rs.close();
                stmt.close();
                conn.close();
            }
        }catch(SQLException se2){}
            try{
                if(conn != null){
                    conn.close();
                }
            }catch(SQLException se){
            }
    }
    
    private static void addDoc(IndexWriter w, String camp1, String camp2, String id1, String id2) throws IOException { /*Declaración de la función para añadir documentos*/
        Document doc = new Document(); /*Inicializamos un nuevo documento*/
        doc.add(new TextField(id2, camp2, Field.Store.YES));
        doc.add(new StringField(id1, camp1, Field.Store.YES));
        w.addDocument(doc); /*Añadimos el nuevo documento*/
    }
    
    public static String[][] buscar(String termino, String campo1, String campo2, String tabla, int hitsPerPage, int crear) throws IOException, SQLException, ParseException{
        StandardAnalyzer analyzer = new StandardAnalyzer();
        //Directory index = FSDirectory.open(Paths.get("INDICE"));
        Directory index = new RAMDirectory();
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        rs = conectar(conn, stmt, rs, campo1, campo2, tabla);
        
        if(crear == 1){
            /*Connection conn = null;
            Statement stmt = null;
            ResultSet rs = null;
            rs = conectar(conn, stmt, rs, campo1, campo2, tabla);*/
            
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try (IndexWriter w = new IndexWriter(index, config)) {
                while(rs.next()){
                    String comentario = rs.getString(campo2);
                    String identificador = rs.getString(campo1);
                    addDoc(w, identificador, comentario, campo1, campo2);
                } /*Cierre del while*/
                w.close();
            } /*Cierre del try*/
            
        }
        Query q = new QueryParser(campo2, analyzer).parse(termino);
        ScoreDoc[] hits;
        String[][] matriz;
        try (IndexReader reader = DirectoryReader.open(index)) {            
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
            searcher.search(q, collector);
            hits = collector.topDocs().scoreDocs;
            matriz = new String[hits.length][3];
            for(int i=0; i < hits.length; i++) { /*Recorremos cada uno de los documentos*/
                int docId = hits[i].doc;
                Document d = searcher.doc(docId); /*Buscamos el documento*/
                matriz[i][0] = d.get(campo1);
                matriz[i][1] = d.get(campo2);
                String resultado = "";
                if(tabla.equals("imagen")){
                    /*obtener id desde la tabla imagen*/
                    String consulta = "Select id_usuario from imagen where id_imagen = "+d.get(campo1)+";";
                    resultado = ejecutarConsulta(conn, stmt, rs, consulta,"id_usuario");
                }
                else{
                    if(tabla.equals("comentario")){
                        /*Obtener id desde la tabla comentario*/
                        String consulta = "Select id_usuario from comentario where id_comentario = "+d.get(campo1)+";";
                        resultado = ejecutarConsulta(conn, stmt, rs, consulta,"id_usuario");
                    }                        
                }
                String puntajeFinal = Integer.toString(calculaPuntaje(resultado, conn, stmt, rs));
                matriz[i][2] = puntajeFinal;
            }/*Cierre del for*/
            desconectar(rs, stmt, conn);
            int axl = matriz.length - 1;
            if (matriz.length != 0){
                ordenar(matriz, 0, axl);
            }/*Cierre if*/
            
            return matriz;
        }/*Cierre del try*/
    } /*Cierre de la funcion buscar*/
    
    public static void mostrar(String[][] matriz){
        if (matriz.length == 0){
            System.out.println("La matriz es nula");
        }/*Cierre if*/
        else{
            for (int i = 0; i < matriz.length; i++){
                System.out.println("hola");
                System.out.println((i + 1) + ". " + matriz[i][0] + "\t" + matriz[i][1] + "\t" + matriz[i][2]);
            }
        }/*Cierre else*/
    }/*Cierre funcion mostrar*/
}
