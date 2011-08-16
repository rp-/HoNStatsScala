package libHoN

import scala.xml._;
import scala.actors.Actor
import scala.actors.Actor._
import java.sql.{ Connection, DriverManager, ResultSet };

object StatsFactory extends Actor {
  //load db driver
  Class.forName("org.sqlite.JDBC").newInstance
  val connection = DriverManager.getConnection("jdbc:sqlite:statsdb.db")

  val XMLRequester = "http://xml.heroesofnewerth.com/xml_requester.php"
  MatchStatsSql.createTable(connection)
  PlayerStatsSql.createTable(connection)
  start

  def getPlayerStatsByNickCached(nicks: List[String]): List[PlayerStats] = {
    val cached = PlayerStatsSql.getEntries(connection, nicks)

    val lowernicks = for { n <- nicks } yield n.toLowerCase
    val fetchnicks = lowernicks filterNot ((for { c <- cached } yield c.attribute(PlayerAttr.NICKNAME).toLowerCase) contains)
    return cached ::: getPlayerStatsByNick(fetchnicks)
  }

  def getPlayerStatsByNick(nicks: List[String]): List[PlayerStats] = {
    if (nicks.isEmpty)
      return Nil

    val query = nicks.mkString("&nick[]=");
    val xmlData = XML.load(new java.net.URL(XMLRequester + "?f=player_stats&opt=nick&nick[]=" + query))
    if (xmlData.label == "error")
      Nil
    else {
      val ret = (for { player <- (xmlData \ "stats" \ "player_stats") } yield new PlayerStats(player)).toList;

      ret.foreach(p => p.cacheEntry(connection))

      if (nicks.length > 50)
        ret ::: getPlayerStatsByNick(nicks.drop(50))
      else
        ret
    }
  }

  def getPlayerStatsByAidCached(aids: List[Int]): List[PlayerStats] = {
    val cached = PlayerStatsSql.getEntriesByAID(connection, aids)

    val fetchids = aids filterNot ((for { c <- cached } yield c.getAID.toInt) contains)
    return cached ::: getPlayerStatsByAid(fetchids)
  }

  def getPlayerStatsByAid(aids: List[Int]): List[PlayerStats] = {
    val query = aids.take(50).mkString("&aid[]=");
    val xmlData = XML.load(XMLRequester + "?f=player_stats&opt=aid&aid[]=" + query)

    if (xmlData.label == "error")
      Nil
    else {
	    val ret = (for { player <- (xmlData \ "stats" \ "player_stats") } yield new PlayerStats(player)).toList;

	    ret.foreach(p => p.cacheEntry(connection))

	    if (aids.length > 50)
	      ret ::: getPlayerStatsByAid(aids.drop(50))
	    else
	      ret
    }

  }

  def getMatchStatsByMatchId(ids: List[Int]): List[MatchStats] = {
    if (ids.isEmpty)
      return Nil

    val cached = MatchStatsSql.getEntries(connection, ids)
    val fetchids = ids filterNot ((for { c <- cached } yield c.getMatchID) contains)

    m_results = Nil
    if (!fetchids.isEmpty) {
      //println("tofetch: " + fetchids.size)
      val workers = for (n <- 0 until 10) yield new WorkerActor

      inProgress = workers.size;
      workers.foreach(w => w.start)

      var curWorker = 0
      def createQueryList(ids: List[Int]): List[String] = ids match {
        case List() => List()
        case x => {
          val qSize = 50
          x.take(qSize).mkString("&mid[]=") :: createQueryList(x.drop(qSize))
        }
      }

      for (query <- createQueryList(fetchids)) {
        workers(curWorker) ! QueryArgs(XMLRequester + "?f=match_stats&opt=mid&mid[]=" + query)
        curWorker += 1
        if (curWorker == workers.size)
          curWorker = 0
      }

      workers.foreach(w => w ! "STOP")

      while (inProgress > 0) Thread.sleep(10)
    }

    val res = m_results
    //val parfetchids = fetchids.par
    //val res = parfetchids.map(id => fetchMachStats(id)).toList
    res.foreach(m => m.cacheEntry(connection))
    return (cached ::: res).sortWith((m1, m2) => (m1.getMatchID < m2.getMatchID))
  }

  private def fetchMatchStats(id: Int): MatchStats = {
    val xmlData = XML.load(XMLRequester + "?f=match_stats&opt=mid&mid[]=" + id)
    val match_ = (xmlData \ "stats" \ "match")
    return new MatchStats((match_ \ "@mid").text.toInt, match_.toString);
  }

  private def fetchMatchStats(ids: List[Int]): List[MatchStats] = {
    if (ids.size == 0)
      return Nil

    val qids = ids.take(50)
    val query = qids.mkString("&mid[]=");
    val xmlData = XML.load(XMLRequester + "?f=match_stats&opt=mid&mid[]=" + query)
    val ret = (for { match_ <- (xmlData \ "stats" \ "match") } yield new MatchStats((match_ \ "@mid").text.toInt, match_.toString)).toList;

    assert(ret.size > 0)
    ret.foreach(m => m.cacheEntry(connection))
    return ret ::: fetchMatchStats(ids.drop(50))
  }

  def dispose = {
    connection.close
    this ! "EXIT"
  }

  case class QueryArgs(query: String)
  case class Result(stats: List[MatchStats])
  var inProgress = 0
  var m_results: List[MatchStats] = Nil

  def act {
    loop {
      react {
        case res: Result => {
          m_results = m_results ::: res.stats
          //println(m_results)
        }
        case "STOP" => {
          inProgress -= 1
        }
        case "EXIT" => exit
      }
    }
  }

  class WorkerActor extends Actor {
    def act() {
      loop {
        react {
          case "STOP" => {
            StatsFactory ! "STOP"
            exit
          }
          case msg: QueryArgs => {
            val xmlData = XML.load(msg.query)
            val result = (for { match_ <- (xmlData \ "stats" \ "match") } yield new MatchStats((match_ \ "@mid").text.toInt, match_.toString)).toList
            StatsFactory ! Result(result)
          }
        }
      }
    }
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
