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
    static final String PASS = "145xswZO";
    
    private static String crearConsulta(String campo1, String campo2, String tabla){
        String consulta;
        consulta = "SELECT " + campo1 + ", " + campo2 + " FROM " + tabla;
        return consulta;
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
            
            
        }
        /****** DECLARACIONES DE TODOS LOS CATCH Y FINALLY NECESARIOS ******/ 
        catch(SQLException | ClassNotFoundException se){
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
        doc.add(new TextField(id1, camp1, Field.Store.YES));
        doc.add(new StringField(id2, camp2, Field.Store.YES));
        w.addDocument(doc); /*Añadimos el nuevo documento*/
    }
    
    private static ScoreDoc[] buscar(String termino, String campo1, String campo2, String tabla, int hitsPerPage) throws IOException, SQLException, ParseException{
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        rs = conectar(conn, stmt, rs, campo1, campo2, tabla);
        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory index = FSDirectory.open(Paths.get("INDICE"));
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        try (IndexWriter w = new IndexWriter(index, config)) {
            while(rs.next()){
                String comentario = rs.getString(campo1);
                String identificador = rs.getString(campo2);
                addDoc(w, comentario, identificador, campo1, campo2);
            } /*Cierre del while*/
            w.close();
        } /*Cierre del try*/
        desconectar(rs, stmt, conn);
        Query q = new QueryParser(campo1, analyzer).parse(termino);
        IndexReader reader = DirectoryReader.open(index);
        IndexSearcher searcher = new IndexSearcher(reader);
        TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
        searcher.search(q, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        reader.close();
        return hits;
    } /*Cierre de la funcion buscar*/
    
    private static void mostrar(ScoreDoc hits, String campo1, String campo2){
        /*ACA LAS SENTENCIAS PARA MOSTRAR LOS RESULTADOS
        SOLUCION ALTERNA ES RETORNAR UN ARRAY DE STRINGS, UNA MATRIS QUIZAS*/
    }/*Cierre funcion mostrar*/
}
