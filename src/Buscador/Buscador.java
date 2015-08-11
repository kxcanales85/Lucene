package Buscador;

/*Importamo la libreria para la lectura del termino ingresado por el usuario*/
import java.io.BufferedReader;
/*Importamos todas las librerias necesarias para Apache Lucene*/
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
import org.apache.lucene.store.RAMDirectory;
/*Importamos las librerias necesarias para las excepciones de I/O*/
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
/*Importamos las librerias necesarias para la conexion con mysql*/
import java.sql.*;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @authors Kevin Canales, Sebastián Ossandón, Gerson Aguirre.
 */

public class Buscador {
    /*Declaración de los string necesarios para las rutas de conexion de mysql*/
    static final String JDBC_DRIVER = "com.mysql.jdbc.Driver"; /*Acá le indicamos con que driver se debe conectar*/
    static final String DB_URL = "jdbc:mysql://localhost:3306/prueba"; /*Acá le damos la ruta de la base de datos*/
    /*Declaracion de las credenciales necesarias*/
    static final String USER = "root"; /*Usuario que se conectará a la base de datos*/
    static final String PASS = "admin"; /*Password del usuario que se conectará*/
    
    public static void main(String[] args) throws IOException, ParseException {
        Connection conn = null; /*Iniciamos la conexion en null, para manejar las excepciones correspondientes*/
        Statement stmt = null; /*Iniciamos el statement en null, para lo mismo*/
        
        try{
            /****** DECLARACIONES DE MYSQL ******/
            Class.forName(JDBC_DRIVER);/*Le pasamos el nombre del driver*/
            System.out.println("Conectando a la base de datos");
            conn = DriverManager.getConnection(DB_URL,USER,PASS); /*Obtenemos la conexion con la url de la DB, y las credenciales necesarias*/
            stmt = conn.createStatement(); /*Obtenemos la declaración*/
            String sql; /*Creamos el string para la consulta a ejecutar en la DB*/
            sql = "SELECT ISBN, nombre_libro FROM libro"; /*Realizamos la consulta pertinente*/
            ResultSet rs = stmt.executeQuery(sql); /*Ejecutamos la consulta*/
            /****** DECLARACIONES DE LUCENE ******/
            StandardAnalyzer analyzer = new StandardAnalyzer(); /*Creamos un nuevo analyzer*/
            //Directory index = new RAMDirectory(); /*SE CREA EL INDEX EN LA RAM LA SIGUIENTE LINEA ES PARA CREAR EN HDD*/
            Directory index = FSDirectory.open(Paths.get("Indice")); /*Creamos un nuevo index en la capeta Indice, que se crea por defecto en la carpeta del proyecto.*/
            IndexWriterConfig config = new IndexWriterConfig(analyzer); /*Creamos una nueva config*/
            IndexWriter w = new IndexWriter(index, config); /*Creamos un escritor de indices, con el index creado y la config*/
            while(rs.next()){ /*Leemos todas las columnas de la tabla*/
                String isbn = rs.getString("ISBN"); /*Creamos el string isbn donde se guardara el dato obtenido desde la DB*/
                String nombre = rs.getString("nombre_libro"); /*Creamos el string nombre donde se guardara el dato obtenidos desde la DB*/
                addDoc(w, nombre, isbn); /*Añadimos el documento, con el escritor*/
            }
            w.close(); /*Cerramos el escritor*/
            rs.close(); /*Cerramos el ejecutor de las consultas*/
            stmt.close();/*Cerramos el statement de mysql*/
            conn.close(); /*Cerramos la conexion a la base de datos*/
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            System.out.print("Ingrese un término para la búsqueda: ");
            String busqueda = br.readLine(); /*Obtenemos el string que el usuario quiere buscar*/
            //String querystr = args.length > 0 ? args[0] : busqueda;
            Query q = new QueryParser("nombre", analyzer).parse(busqueda); /*Creamos la nueva query para el buscador, le pasa el string que ingreso el usuario*/
            int hitsPerPage = 20; /*Declaramos cuantas coincidencias mostraremos por página*/
            IndexReader reader = DirectoryReader.open(index); /*Inicializamos el lector, desde el directorio para leer*/
            IndexSearcher searcher = new IndexSearcher(reader); /*Inicializamos el buscador, pasandole como parámetro el lector*/
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage); /*Incializamos el collector, con el parámetro de las coincidencias por hoja*/
            searcher.search(q, collector); /*Realizamos la busqueda*/
            ScoreDoc[] hits = collector.topDocs().scoreDocs; /*Agregamos el puntaje a cada documento*/
            System.out.println("Encontramos " + hits.length + " coincidencias."); /*Anunciamos la cantidad de documentos encontrados*/
            for(int i=0; i < hits.length; i++) { /*Recorremos cada uno de los documentos*/
                int docId = hits[i].doc;
                Document d = searcher.doc(docId); /*Buscamos el documento*/
                System.out.println((i + 1) + ". " + d.get("isbn") + "\t" + d.get("nombre")); /*Se muestra por pantalla el resultado encontrado*/
            }   

            reader.close(); /*Cerramos el lector*/
        /****** DECLARACIONES DE TODOS LOS CATCH Y FINALLY NECESARIOS ******/ 
        }catch(SQLException se){
        }catch(ClassNotFoundException | IOException | ParseException e){
        }finally{
            try{
                if(stmt != null){
                    stmt. close();
                }
            }catch(SQLException se2){
                
            }
            try{
                if(conn != null){
                    conn.close();
                }
            }catch(SQLException se){
            }
        }
    }

  private static void addDoc(IndexWriter w, String title, String isbn) throws IOException { /*Declaración de la función para añadir documentos*/
    Document doc = new Document(); /*Inicializamos un nuevo documento*/
    doc.add(new TextField("nombre", title, Field.Store.YES)); /*Agregamos el campo nombre al documento*/
    doc.add(new StringField("isbn", isbn, Field.Store.YES)); /*Agregamos el campo isbn al documento*/
    w.addDocument(doc); /*Añadimos el nuevo documento*/
  }
}