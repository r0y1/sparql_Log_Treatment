package MDfromLogQueries.SPARQLSyntacticalValidation

import java.io.{File, FileOutputStream, PrintWriter}

import MDPatternDetection.QueryUpdate
import MDfromLogQueries.Declarations.Declarations._
import MDfromLogQueries.Util.Constants2
import org.apache.jena.query.{Query, QueryFactory}

import scala.collection.parallel.ParSeq
import scala.io.Source

object SyntacticValidationParallel extends App {


  val t1 = System.currentTimeMillis()
  val duration = System.currentTimeMillis() - t1

  //: util.ArrayList[Query]
  def valideQueriesInFile(filePath: String) = {


    new Constants2(dbPediaOntologyPath)

    val lines = Source.fromFile(filePath).getLines

    lines.grouped(100000).foreach {

      groupOfLines => {

        var nb_req = 0

        val treatedGroupOfLines = groupOfLines.par.map {

          line => {
            nb_req = nb_req + 1
            println("* " + nb_req)


            var constructedQuery = QueryFactory.create()
            try {
              val query = QueryFactory.create(line)
              val queryUpdate = new QueryUpdate()
              constructedQuery = queryUpdate.toConstruct(query)



              /* Some meaning if there is a result != null */
              Some(constructedQuery)


            } catch {
              case unknown => {
                println("une erreur\n\n\n\n\n\n\n\n\n")
                writeInLogFile(logFile, constructedQuery)
                None
              }
            }

          }

        }

        println("--------------------- un group finished ---------------------------------- ")

        writeInFile(syntaxValidFile, treatedGroupOfLines.collect { case Some(x) => x })
      }
    }

  }

  /** Function that writes into destinationFilePath the list passed as parameter **/
  def writeInFile(destinationFilePath: String, queries: ParSeq[Query]) = {


    val writer = new PrintWriter(new FileOutputStream(new File(destinationFilePath), true))

    queries.foreach(query => writer.write(query.toString().replaceAll("[\n\r]", "\t") + "\n"))

    writer.close()
  }

  def writeInLogFile(destinationFilePath: String, query: Query) = {

    val writer = new PrintWriter(new FileOutputStream(new File(destinationFilePath), true))

    writer.write(query.toString().replaceAll("[\n\r]", "\t") + "\n")

    writer.close()
  }


  valideQueriesInFile(writingDedupFilePath)

  private def Validate(queryStr: String) = {
    val queryStr2 = QueryFixer.get.fix(queryStr)
    /*System.out.println(queryStr2);*/ QueryFixer.toQuery(queryStr2).toString
  }

  println(duration)


}
