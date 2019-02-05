package MDfromLogQueries.LogCleaning

import java.io.{File, PrintWriter}
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

import MDfromLogQueries.Declarations.Declarations
import MDfromLogQueries.Util.FileOperation
import org.apache.http.client.utils.URLEncodedUtils

import scala.collection.JavaConversions._
import scala.collection.JavaConverters

//class LogCleaning{
object Main extends App {


  val t1 = System.currentTimeMillis()

println("je suis dans log cleaner")

  // directory contenant les ficheir a lire
  //val dirPath = Declarations.directoryPath
  val dirPath = Declarations.directoryPath
  //fichier ou ecrire
  val filePath = Declarations.cleanedQueriesFileCopie
  //taille buffer pour paralleliser

  private val PATTERN = Pattern.compile("[^\"]*\"(?:GET )?/sparql/?\\?([^\"\\s\\n]*)[^\"]*\".*")
var nb_queries=0
  val duration = System.currentTimeMillis() - t1

  def writeFiles(directoryPath: String, destinationfilePath: String) = {
    val dir = new File(directoryPath)
    val logs = dir.listFiles().toList.par.flatMap(x => extractQueries(x))

    val writer = new PrintWriter(new File(destinationfilePath))
    logs.foreach(x => if(x != null) writer.write(x.replaceAll("[\n\r]","\t")+"\n"))
    writer.close()
  }

  /** Read lines of log file passed as parameter and return queries **/

  def extractQueries(file: File) = {

    val iterable = JavaConverters.collectionAsScalaIterable(FileOperation.ReadFile(file.toString))
    iterable.par.map {
      line => {
        nb_queries += 1
        queryFromLogLine(line)
      }
    }
  }

  def queryFromLogLine(line: String) = {
    val matcher = PATTERN.matcher(line)

    if (matcher.find) {
      val requestStr = matcher.group(1)
      val queryStr = queryFromRequest(requestStr)
      if (queryStr != null) {
        //println(queryStr)
        queryStr
      }
      else requestStr
    }
    else null
  }

  writeFiles(dirPath, filePath)
  println(s"nb queries $nb_queries")

  def queryFromRequest(requestStr: String): String = {
    val pairs = URLEncodedUtils.parse(requestStr, StandardCharsets.UTF_8)

    for (pair <- pairs) {
      if ("query" == pair.getName) {
        return pair.getValue
      }
    }
    null
  }

  println(duration)
}
//}
