package libHoN

import java.sql._;
import scala.xml._;

class MatchStats(MatchID: Int, matchData: String) {
  require(MatchID.isValidInt)
  override def toString = matchData.toString
  def getMatchID = this.MatchID
  lazy val xmlMatchData : scala.xml.Node = XML.loadString(matchData)

  def isCached(conn: java.sql.Connection) = {
    val query = "SELECT mid FROM MatchStats WHERE mid = " + MatchID
    val s = conn.createStatement
    val rs = s.executeQuery(query)
    val cached = rs.next
    s.close
    cached
  }

  def cacheEntry(conn: java.sql.Connection) = {
    //if (!isCached(conn)) {
    val query = "INSERT INTO MatchStats ( mid, xmlData) VALUES ( ?, ?)"
    val ps = conn.prepareStatement(query)
    ps.setInt(1, MatchID)
    ps.setString(2, matchData.toString)
    try {
      ps.executeUpdate
    } catch {
      case see: java.sql.SQLSyntaxErrorException => {
        println("ERROR(" + MatchID + ": " + query)
      }
    }
    ps.close
    //}
  }

  def getMatchStat(stat: String): String = {
    (xmlMatchData \ "summ" \ "stat").filter(ms => ms.attribute("name").get.toString == stat).head.text
  }

  def getMatchStatAsInt(stat: String): Int = {
    getMatchStat(stat).toInt
  }

  def getPlayerStats(aid: String): scala.xml.Node = {
    (xmlMatchData \ "match_stats" \ "ms").filter(ms => ms.attribute("aid").get.toString == aid).head
  }

  def getPlayerMatchStat(aid: String, attribute: String): String = {
    val stats = getPlayerStats(aid)

    (stats \ "stat").filter(st => st.attribute("name").get.toString == attribute).head.text
  }

  def getPlayerMatchStatAsInt(aid: String, attribute: String): Int = {
    getPlayerMatchStat(aid, attribute).toInt
  }

  def playerWon(aid: String): Boolean = {
    getPlayerMatchStat(aid, "team").toInt == getWinningTeam()
  }

  def getTeamStat(side: String, stat: String): String = {
    val sideMap = Map("Legion" -> 1, "Hellbourne" -> 2)

    val team = (xmlMatchData \ "team").filter(t => t.attribute("side").get.text.toInt == sideMap(side)).head
    (team \ "stat").filter(ms => ms.attribute("name").get.toString == stat).head.text
  }

  def getWinningTeam(): Int = {
    val winning = for { team <- (xmlMatchData \ "team") } yield (team \ "stat").filter(s => s.attribute("name").get.head.text == "tm_wins")

    val legionWins = getTeamStat("Legion", "tm_wins").toInt
    if (legionWins > 0)
      1
    else
      2
  }

  def getGameDuration(): String = {
    val time = getMatchStat(MatchAttr.TIME_PLAYED).toInt

    "%d:%02d".format((time % 3600) / 60, (time % 60))
  }
}

object MatchAttr {
  val MAP = "map"
  val TIME_PLAYED = "time_played"
  val MATCH_DATE = "mdt"
  val SINGLE_DRAFT = "sd"
  val ALL_PICK = "ap"
  val BANNING_DRAFT = "bd"
  val BANNING_PICK = "bp"
  val ALL_RANDOM = "ar"
  val MAX_PLAYERS = "max_players"
}

object MatchPlayerAttr {
  val WARDS = "wards"
  val DENIES = "denies"
  val TEAMCREEPKILLS = "teamcreepkills"
  val NEUTRALCREEPKILLS = "neutralcreepkills"
  val GOLD = "gold"
}

object MatchStatsSql {
  def createTable(conn: java.sql.Connection) = {
    val dmd = conn.getMetaData
    val rs = dmd.getTables(conn.getCatalog, null, "MATCHSTATS", null)
    if (!rs.next()) {
      val query = conn.createStatement
      query.execute(
        """CREATE CACHED TABLE MATCHSTATS (
             mid integer primary key,
             xmlData CLOB(32k)
           )""")
      query close
    }
  }

  def getEntries(conn: java.sql.Connection, mid: List[Int]) = {
    if (mid.size > 0) {
      val s = conn.createStatement
      val query = "SELECT mid, xmlData FROM MatchStats WHERE mid IN (" + mid.mkString(",") + ") ORDER BY mid"
      SQLHelper.queryEach(conn, query) { rs =>
        new MatchStats(rs.getInt("mid"), rs.getString("xmlData"))
      }
    } else
      Nil
  }
}
