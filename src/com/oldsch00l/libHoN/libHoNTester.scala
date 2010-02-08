package com.oldsch00l.libHoN;

object Tester {
	def main(args : Array[String]) : Unit = {
		val erpe = StatsFactory.getPlayerStats( "erpe")
		val wins = erpe.attribute( StatsFactory.PlayerAttr.WINS);
        val aid = erpe.getAID;

        println( erpe );
		println( wins );
        println( erpe.attribute( StatsFactory.PlayerAttr.GAMES_PLAYED) );
        println( aid )
  	}
}
