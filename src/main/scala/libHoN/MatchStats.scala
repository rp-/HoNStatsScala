package libHoN

import java.sql._;
import scala.xml._;
import oldsch00l.Log;

class MatchStats(MatchID: Int, matchData: String) {
  require(MatchID.isValidInt)
  Log.debug(matchData)
  override def toString = matchData.toString
  def getMatchID = this.MatchID
  lazy val xmlMatchData: scala.xml.Node = XML.loadString(matchData)

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

  def getMatchStat(stat: String): Option[String] = {
    if (!(xmlMatchData \ "summ" \ "stat").isEmpty)
      Option((xmlMatchData \ "summ" \ "stat").filter(ms => ms.attribute("name").get.toString == stat).head.text)
    else
      None
  }

  def getPlayerId(ms: scala.xml.Node): String = ms.attribute("aid").get.toString

  def getMatchStatAsInt(stat: String): Int = {
    getMatchStat(stat).getOrElse("0").toInt
  }

  def getPlayerStats(aid: String): scala.xml.Node = {
    (xmlMatchData \ "match_stats" \ "ms").filter(ms => getPlayerId(ms) == aid).head
  }

  lazy val getPlayersAIDs: List[String] = {
    val players = (xmlMatchData \ "match_stats" \ "ms").sortWith((m1, m2) =>
      getPlayerMatchStatAsInt(getPlayerId(m1), "position") > getPlayerMatchStatAsInt(getPlayerId(m2), "position"))

    (for(player <- players) yield getPlayerId(player)).toList
  }

  lazy val getLegionPlayers: List[Int] = {
    for(player <- getPlayersAIDs
        if getPlayerMatchStat(player, "team") == "1") yield player.toInt
  }

  lazy val getHellbournePlayers: List[Int] = {
    for(player <- getPlayersAIDs
        if getPlayerMatchStat(player, "team") == "2") yield player.toInt
  }

  val dfm = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z")
  def getLocalMatchDateTime(): java.util.Date = {
    if (getMatchStat(MatchAttr.MATCH_DATETIME).isEmpty)
      new java.util.Date()
    else
      dfm.parse(getMatchStat(MatchAttr.MATCH_DATETIME).get + " -0400")
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

    val teams = (xmlMatchData \ "team")
    if(teams.size > 0) {
      val team = (xmlMatchData \ "team").filter(t => t.attribute("side").get.text.toInt == sideMap(side)).head
      (team \ "stat").filter(ms => ms.attribute("name").get.toString == stat).head.text
    } else {
      null
    }
  }

  def getWinningTeam(): Int = {
    val teams = (xmlMatchData \ "team")
    if(!teams.isEmpty) {
      val winning = for { team <- (xmlMatchData \ "team") } yield (team \ "stat").filter(s => s.attribute("name").get.head.text == "tm_wins")

      val legionWins = getTeamStat("Legion", "tm_wins").toInt
      if (legionWins > 0)
        1
      else
        2
    } else {
      0
    }
  }

  def getGameDuration(): String = {
    val time = getMatchStatAsInt(MatchAttr.TIME_PLAYED)

    "%d:%02d".format((time / 60).toInt, (time % 60))
  }
}

object MatchAttr {
  val MAP = "map"
  val TIME_PLAYED = "time_played"
  val MATCH_DATETIME = "mdt"
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
  val GOLDLOST2DEATH = "goldlost2death"
}

object MatchStatsSql {
  def createTable(conn: java.sql.Connection) = {
    val dmd = conn.getMetaData
    val rs = dmd.getTables(conn.getCatalog, null, "MATCHSTATS", null)
    if (!rs.next()) {
      val query = conn.createStatement
      query.execute(
        """CREATE TABLE IF NOT EXISTS MATCHSTATS (
             mid integer primary key,
             xmlData TEXT
           )""")
      query close
    }
  }

  def getEntries(conn: java.sql.Connection, mid: List[Int]) = {
    if (!mid.isEmpty) {
      val query = "SELECT mid, xmlData FROM MatchStats WHERE mid IN (" + mid.mkString(",") + ") ORDER BY mid"
      SQLHelper.queryEach(conn, query) { rs =>
        new MatchStats(rs.getInt("mid"), rs.getString("xmlData"))
      }
    } else
      Nil
  }
}
