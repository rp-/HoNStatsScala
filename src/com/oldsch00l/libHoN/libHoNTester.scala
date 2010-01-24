package com.oldsch00l.libHoN;

import scala.xml._;

object Tester {
	def main(args : Array[String]) : Unit = {
		val xmlErpe = XML.load( "http://xml.heroesofnewerth.com/xml_requester.php?f=player_stats&opt=nick&nick[]=Erpe");
		println( xmlErpe);
		val erpe = new PlayerStats( xmlErpe)
		val wins = erpe.attribute( "acc_wins");
  
		println( wins.text);
  	}
}
