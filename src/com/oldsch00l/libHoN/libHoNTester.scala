package com.oldsch00l.libHoN;

object Tester {
  def main(args : Array[String]) : Unit = {
    val erpe = StatsFactory.getPlayerStats( 342)
    if( erpe != None) {
      val wins = erpe.get.attribute( PlayerAttr.WINS);
      val aid = erpe.get.getAID;

      println( erpe.get );
      println( wins );
      println( erpe.get.attribute( PlayerAttr.GAMES_PLAYED) );
      println( aid )
    }
  }
}
