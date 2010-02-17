package com.oldsch00l.libHoN;

object Tester {
  def main(args : Array[String]) : Unit = {
    //val erpe = StatsFactory.getPlayerStatsByAid( (2500 to 2600).toList)
    val erpe = StatsFactory.getPlayerStatsByNick( List("Erpe", "Sandla"))
    erpe.foreach( p => println( p.attribute(PlayerAttr.NICKNAME) + "(" + p.getAID + ") = " + p.attribute( PlayerAttr.GAMES_PLAYED)))
    println( "list size: " + erpe.length)

/*    if( erpe != Nil) {
      val wins = erpe(0).attribute( PlayerAttr.WINS);
      val aid = erpe(0).getAID;

      println( erpe(0) );
      println( wins );
      println( erpe(0).attribute( PlayerAttr.GAMES_PLAYED) );
      println( aid )
    }*/
  }
}
