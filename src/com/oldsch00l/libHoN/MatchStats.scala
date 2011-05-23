package com.oldsch00l.libHoN

import java.sql._;

class MatchStats(MatchID: Int, matchData: scala.xml.Node) {
  require(MatchID.isValidInt)
  override def toString = matchData.toString
  def getMatchID = this.MatchID

  def isCached(conn: java.sql.Connection) = {
    val query = "SELECT mid FROM MatchStats WHERE mid = " + MatchID
    val s = conn.createStatement
    val rs = s.executeQuery(query)
    val cached = rs.next
    s.close
    cached
  }

  def cacheEntry(conn: java.sql.Connection) = {
    if (!isCached(conn)) {
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
    }
  }
}

object MatchStatsSql {
  def createTable(conn: java.sql.Connection) = {
    val dmd = conn.getMetaData
    val rs = dmd.getTables(conn.getCatalog, null, "MATCHSTATS", null)
    if (!rs.next()) {
      val query = conn.createStatement
      query.execute(
        """CREATE TABLE MATCHSTATS (
             mid integer primary key,
             xmlData CLOB(32k)
           )""")
      query close
    }
  }

  def getEntries(conn: java.sql.Connection, mid: List[Int]) = {
    if (mid.size > 0) {
      val s = conn.createStatement
      val query = "SELECT mid, xmlData FROM MatchStats WHERE mid IN (" + mid.mkString(",") + ")"
      SQLHelper.queryEach(conn, query) { rs =>
        new MatchStats(rs.getInt("mid"), scala.xml.XML.loadString(rs.getString("xmlData")))
      }
    } else
      Nil
  }
}
