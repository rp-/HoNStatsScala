package com.oldsch00l.libHoN;

object Tester extends App {
    //val erpe = StatsFactory.getPlayerStatsByAid( (2500 to 2600).toList)
    val pllist = StatsFactory.getPlayerStatsByNick( List("Erpe", "Sandla"));

    val erpe : PlayerStats = pllist(0);
    val matches = erpe.getPlayedMatches
    println(matches.length)
    pllist.foreach( p => println( p.attribute(PlayerAttr.NICKNAME) + "(" + p.getAID + ") = " + p.attribute( PlayerAttr.GAMES_PLAYED)))
    println( "list size: " + pllist.length)

/*    if( erpe != Nil) {
      val wins = erpe(0).attribute( PlayerAttr.WINS);
      val aid = erpe(0).getAID;

      println( erpe(0) );
      println( wins );
      println( erpe(0).attribute( PlayerAttr.GAMES_PLAYED) );
      println( aid )
    }*/
}
