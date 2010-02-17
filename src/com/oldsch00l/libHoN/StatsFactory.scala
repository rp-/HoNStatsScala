package com.oldsch00l.libHoN

import scala.xml._;

object StatsFactory {
  val XMLRequester = "http://xml.heroesofnewerth.com/xml_requester.php"

  def getPlayerStatsByNick( nicks : List[String]) : List[PlayerStats] = {
    val query = nicks.mkString( "&nick[]=");
    val xmlData = XML.load( XMLRequester + "?f=player_stats&opt=nick&nick[]=" + query);
    if (xmlData.label == "error")
      Nil
    else {
      val ret = (for { player <- (xmlData \\ "player_stats") } yield new PlayerStats( player)).toList;

      if( nicks.length > 50)
        ret ::: getPlayerStatsByNick( nicks.drop(50))
      else
        ret
    }
  }

  def getPlayerStatsByAid( aids : List[Int]) : List[PlayerStats] = {
    val query = aids.take(50).mkString( "&aid[]=");
    val xmlData = XML.load(XMLRequester + "?f=player_stats&opt=aid&aid[]=" + query)
    if( xmlData.child.exists( _.label == "player_stats") ) {
      val ret = (for { player <- (xmlData \\ "player_stats") } yield new PlayerStats( player)).toList;

      if( aids.length > 50)
        ret ::: getPlayerStatsByAid( aids.drop(50))
      else
        ret
    }
    else
      Nil

  }
}
