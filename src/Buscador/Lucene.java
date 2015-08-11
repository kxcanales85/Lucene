package Buscador;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

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
    
    private String crearConsulta(String campo1, String campo2, String tabla){
        String consulta;
        consulta = "SELECT " + campo1 + ", " + campo2 + " FROM " + tabla;
        return consulta;
    }
    
    private static void conectar(Connection conn, Statement stmt){
        conn = null;
        stmt = null;
        try{
            Class.forName(JDBC_DRIVER);/*Le pasamos el nombre del driver*/
            System.out.println("Conectando a la base de datos");
            conn = DriverManager.getConnection(DB_URL,USER,PASS); /*Obtenemos la conexion con la url de la DB, y las credenciales necesarias*/
            stmt = conn.createStatement(); /*Obtenemos la declaración*/
            String sql; /*Creamos el string para la consulta a ejecutar en la DB*/
            sql = "SELECT id_comentario, texto_comentario FROM comentario"; /*Realizamos la consulta pertinente*/
            ResultSet rs = stmt.executeQuery(sql); /*Ejecutamos la consulta*/
            
            /*Cerramos las conexciones pertinentes*/
            rs.close();
            stmt.close();
            conn.close();            
        }
        /****** DECLARACIONES DE TODOS LOS CATCH Y FINALLY NECESARIOS ******/ 
        catch(SQLException | ClassNotFoundException se){
        }
    }
    
    private static void desconectar(Statement stmt, Connection conn){
        try{
            if(stmt != null){
                stmt. close();
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
}
