package com.oldsch00l.libHoN

import scala.xml._;
import java.sql.{ Connection, DriverManager, ResultSet };

object StatsFactory {
  val XMLRequester = "http://xml.heroesofnewerth.com/xml_requester.php"
  //load db driver
  Class.forName("org.hsqldb.jdbc.JDBCDriver").newInstance
  val connection = DriverManager.getConnection("jdbc:hsqldb:file:statsdb", "SA", "")
  MatchStatsSql.createTable(connection)

  def getPlayerStatsByNick(nicks: List[String]): List[PlayerStats] = {
    val query = nicks.mkString("&nick[]=");
    val xmlData = XML.load(XMLRequester + "?f=player_stats&opt=nick&nick[]=" + query);
    if (xmlData.label == "error")
      Nil
    else {
      val ret = (for { player <- (xmlData \\ "player_stats") } yield new PlayerStats(player)).toList;

      if (nicks.length > 50)
        ret ::: getPlayerStatsByNick(nicks.drop(50))
      else
        ret
    }
  }

  def getPlayerStatsByAid(aids: List[Int]): List[PlayerStats] = {
    val query = aids.take(50).mkString("&aid[]=");
    val xmlData = XML.load(XMLRequester + "?f=player_stats&opt=aid&aid[]=" + query)
    if (xmlData.child.exists(_.label == "player_stats")) {
      val ret = (for { player <- (xmlData \\ "player_stats") } yield new PlayerStats(player)).toList;

      if (aids.length > 50)
        ret ::: getPlayerStatsByAid(aids.drop(50))
      else
        ret
    } else
      Nil

  }

  def getMatchStatsByMatchId(ids: List[Int]): List[MatchStats] = {
    if (ids.size == 0)
      return Nil

    val cached = MatchStatsSql.getEntries(connection, ids)
    val fetchids = ids -- (for { c <- cached } yield c.getMatchID)
    return cached ::: fetchMachStats(fetchids)
  }

  private def fetchMachStats(ids: List[Int]): List[MatchStats] = {
    if (ids.size == 0)
      return Nil

    val qids = ids.take(50)
    val query = qids.mkString("&mid[]=");
    val xmlData = XML.load(XMLRequester + "?f=match_stats&opt=mid&mid[]=" + query)
    val ret = (for { match_ <- (xmlData \\ "match") } yield new MatchStats((match_ \ "@mid").text.toInt, match_.toString)).toList;

    ret.foreach(m => m.cacheEntry(connection))
    return ret ::: fetchMachStats(ids.drop(50))
  }

  def dispose = {
    connection.close
  }
}

object SQLHelper {
  def using[Closeable <: { def close(): Unit }, B](closeable: Closeable)(getB: Closeable => B): B =
    try {
      getB(closeable)
    } finally {
      closeable.close()
    }

  import scala.collection.mutable.ListBuffer

  def bmap[T](test: => Boolean)(block: => T): List[T] = {
    val ret = new ListBuffer[T]
    while (test) ret += block
    ret.toList
  }

  /** Executes the SQL and processes the result set using the specified function. */
  def query[B](connection: Connection, sql: String)(process: ResultSet => B): B =
    using(connection.createStatement) { statement =>
      using(statement.executeQuery(sql)) { results =>
        process(results)
      }
    }

  /** Executes the SQL and uses the process function to convert each row into a T. */
  def queryEach[T](connection: Connection, sql: String)(process: ResultSet => T): List[T] =
    query(connection, sql) { results =>
      bmap(results.next) {
        process(results)
      }
    }
}
