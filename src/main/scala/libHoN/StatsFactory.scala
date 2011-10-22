package libHoN

import scala.xml._
import scala.actors.Actor
import scala.actors.Actor._
import java.sql.{ Connection, DriverManager, ResultSet };
import scala.actors.DaemonActor

object StatsFactory extends Actor {
  val XMLRequester : String = "http://xml.heroesofnewerth.com/xml_requester.php"

  //load db driver and create tables
  Class.forName("org.sqlite.JDBC").newInstance

  val createDB = !(new java.io.File("statsdb.db").exists())
  val connection = DriverManager.getConnection("jdbc:sqlite:statsdb.db")
  if( createDB )
    SQLHelper.createTables(connection)
  else
    SQLHelper.upgradeDB(connection)
  start

  private def fetchPlayerByNick(nicks: List[String]): List[PlayerStats] = {
    if (nicks.isEmpty)
      return Nil

    val query = nicks.mkString("&nick[]=");
    val xmlData = XML.load(new java.net.URL(XMLRequester + "?f=player_stats&opt=nick&nick[]=" + query))
    if (xmlData.label != "error") {
      val ret = (for { player <- (xmlData \ "stats" \ "player_stats") } yield new PlayerStats(player));

      ret.foreach(p => p.cacheEntry(connection))

      if (nicks.length > 50)
        return ret.toList ::: fetchPlayerByNick(nicks.drop(50))
      else
        return ret.toList
    }
    return Nil
  }

  private def fetchPlayerByAID(aids: List[Int]): List[PlayerStats] = {
    if(aids.isEmpty)
      return Nil

    val query = aids.take(50).mkString("&aid[]=");
    val xmlData = XML.load(XMLRequester + "?f=player_stats&opt=aid&aid[]=" + query)

    if (xmlData.label != "error") {
	    val ret = (for { player <- (xmlData \ "stats" \ "player_stats") } yield new PlayerStats(player));

	    ret.foreach(p => p.cacheEntry(connection))

	    if (aids.length > 50)
	      return ret.toList ::: fetchPlayerByAID(aids.drop(50))
	    else
	      return ret.toList
    }
    return Nil
  }

  def getPlayerStatsByNick(nicks: List[String]): List[PlayerStats] = {
    if(!CommandMain.fetch) {
	    val cached = PlayerStatsSql.getEntries(connection, nicks)

	    val cachedCurrent = for ( p <- cached if p.isCurrent(StatsFactory.connection)) yield p

	    val lowernicks = for { n <- nicks } yield n.toLowerCase
	    val fetchnicks = lowernicks filterNot ((for { c <- cachedCurrent } yield c.attribute(PlayerAttr.NICKNAME).toLowerCase) contains)
	    return cachedCurrent ::: fetchPlayerByNick(fetchnicks)
    }

    return fetchPlayerByNick(nicks)
  }

  def getPlayerStatsByAID(aids: List[Int]): List[PlayerStats] = {
    if(!CommandMain.fetch) {
	    val cached = PlayerStatsSql.getEntriesByAID(connection, aids)

	    val cachedCurrent = for ( p <- cached if p.isCurrent(StatsFactory.connection)) yield p
	    val cachedids = for { c <- cachedCurrent } yield c.getAID.toInt
	    val fetchids = aids filterNot (cachedids contains)
	    return cachedCurrent ::: fetchPlayerByAID(fetchids)
    }

    return fetchPlayerByAID(aids)
  }

  def getMatchStatsByMatchId(ids: List[Int]): List[MatchStats] = {
    if (ids.isEmpty)
      return Nil

    val cached = if (CommandMain.fetch) Nil else MatchStatsSql.getEntries(connection, ids)
    val fetchids = ids filterNot ((for { c <- cached } yield c.getMatchID) contains)

    m_results = Nil
    if (!fetchids.isEmpty) {
      //println("tofetch: " + fetchids.size)
      val workers = for (n <- 0 until 10) yield new WorkerActor

      inProgress = workers.size;
      workers.foreach(w => w.start)

      var curWorker = 0
      def createQueryList(ids: List[Int]): List[List[Int]] = ids match {
        case List() => List()
        case x => {
          val qSize = 50
          x.take(qSize) :: createQueryList(x.drop(qSize))
        }
      }

      for (ids <- createQueryList(fetchids)) {
        workers(curWorker) ! QueryArgs(XMLRequester + "?f=match_stats&opt=mid&mid[]=" + ids.mkString("&mid[]="), ids)
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
    val emptyless = res.filter( m => !m.isEmpty) ::: cached.filter( m => !m.isEmpty)
    return emptyless.sortWith((m1, m2) => (m1.getMatchID < m2.getMatchID))
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

  case class QueryArgs(query: String, ids : List[Int])
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

  class WorkerActor extends DaemonActor {
    def act() {
      loop {
        react {
          case "STOP" => {
            StatsFactory ! "STOP"
            exit
          }
          case msg: QueryArgs => {
            val xmlData = XML.load(msg.query)
            val result = (
                for ( match_ <- (xmlData \ "stats" \ "match") if !match_.isEmpty )
                  yield new MatchStats((match_ \ "@mid").text.toInt, match_.toString)
            ).toList
            val emptyids = msg.ids filterNot ((for { m <- result } yield m.getMatchID) contains)
            val emptyRes = for ( id <- emptyids ) yield new MatchStats( id, MatchStatsSql.emptyString, true)
            StatsFactory ! Result(result ::: emptyRes)
          }
        }
      }
    }
  }
}

object SQLHelper {
  val DBVERSION = 2

  def using[Closeable <: { def close(): Unit }, B](closeable: Closeable)(getB: Closeable => B): B =
    try {
      getB(closeable)
    } finally {
      closeable.close()
    }

  def createTables(conn: java.sql.Connection) {
    val query = conn.createStatement
    query.executeUpdate(
      """CREATE TABLE IF NOT EXISTS dbmeta (
         key TEXT PRIMARY KEY,
         value TEXT
       );
       CREATE TABLE IF NOT EXISTS playerstats (
		     ID INTEGER PRIMARY KEY AUTOINCREMENT,
		     aid integer,
		     nickname TEXT,
		     insertDate TIMESTAMP,
         gamesplayed INTEGER,
		     xmlData TEXT
		   );
		   CREATE TABLE IF NOT EXISTS playermatches (
		     id INTEGER PRIMARY KEY AUTOINCREMENT,
		     statstype TEXT,
		     aid INTEGER,
		     matchid integer
		  );
      CREATE TABLE IF NOT EXISTS matchstats (
         mid integer primary key,
         xmlData TEXT
      );
      INSERT INTO dbmeta (key, value) VALUES ('version', '""" + DBVERSION + "');")
    query close
  }

  def upgradeDB(conn: java.sql.Connection) {
    var dbversion = 0
    try {
      val prepStm = conn.prepareStatement("SELECT value FROM dbmeta WHERE key='version';")
      val rs = prepStm.executeQuery
      dbversion = if (rs.next) rs.getInt("value") else 0
      rs.close
    } catch {
      case e: java.sql.SQLException => {

      }
    }

    if (dbversion == 0) {
      val insert = conn.createStatement()
      insert.executeUpdate(
        """CREATE TABLE IF NOT EXISTS dbmeta (
                 key TEXT PRIMARY KEY,
                 value TEXT
               );
               INSERT INTO dbmeta (key, value) VALUES ('version', '""" + DBVERSION + "');")
      insert.close
    }

    if (dbversion <= 1) {
      val update = conn.createStatement
      update.executeUpdate("ALTER TABLE playerstats ADD COLUMN gamesplayed INTEGER;")
      update.close
    }

    val updateVersion = conn.createStatement
    updateVersion.executeUpdate("UPDATE dbmeta SET value='" + DBVERSION + "' WHERE key='version';")
    updateVersion.close
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
