package com.oldsch00l.libHoN

import scala.xml._;

object StatsFactory {
  val XMLRequester = "http://xml.heroesofnewerth.com/xml_requester.php"

  def getPlayerStats( nick : String) : Option[PlayerStats] = {
          val xmlData = XML.load( XMLRequester + "?f=player_stats&opt=nick&nick[]=" + nick);
          if (xmlData.label == "error")
                  None
          else
                  Some(new PlayerStats( xmlData ));
  }

  def getPlayerStats( aid : Int) : Option[PlayerStats] = {
    val xmlData = XML.load(XMLRequester + "?f=player_stats&opt=aid&aid[]=" + aid)
    if( xmlData.child.exists( _.label == "player_stats") )
      Some( new PlayerStats(xmlData))
    else
      None
  }
}
