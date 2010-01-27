package com.oldsch00l.libHoN

import scala.xml._;

object StatsFactory {
	val XMLRequester = "http://xml.heroesofnewerth.com/xml_requester.php"

	def getPlayerStats( nick : String) : PlayerStats = {
		val xmlData = XML.load( XMLRequester + "?f=player_stats&opt=nick&nick[]=" + nick);
		if (xmlData.exists( _ == "error"))
			null
		else
			new PlayerStats( xmlData );
	}

	def getPlayerStats( aid : Int) : PlayerStats =
		new PlayerStats(XML.load( XMLRequester + "?f=player_stats&opt=aid&aid[]=" + aid));

}
