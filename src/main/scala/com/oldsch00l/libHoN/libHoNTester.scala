package com.oldsch00l.libHoN;

object Tester extends App {
  //val erpe = StatsFactory.getPlayerStatsByAid( (2500 to 2600).toList)
  val pllist = StatsFactory.getPlayerStatsByNick(List("Erpe", "Sandla"));

  val erpe: PlayerStats = pllist(0)
  val marc: PlayerStats = pllist(1)
  //  val matches = erpe.getPlayedMatches
  //println(matches.length)
  val rpTime = System.currentTimeMillis
  val rpmatches = erpe.getPlayedMatches
  println("Erpe fetch(" + rpmatches.size + ") time: " + (System.currentTimeMillis - rpTime))

  val marcTime = System.currentTimeMillis
  val marcmatches = marc.getPlayedMatches
  println("Marc fetch(" + marcmatches.size + ") time: " + (System.currentTimeMillis - marcTime))

  pllist.foreach(p => println(p.attribute(PlayerAttr.NICKNAME) + "(" + p.getAID + ") = MMR: " + p.attribute(PlayerAttr.RANK_AMM_TEAM_RATING) + " played: " + p.getPlayedMatches.size))
  println("list size: " + pllist.length)

  /*    if( erpe != Nil) {
      val wins = erpe(0).attribute( PlayerAttr.WINS);
      val aid = erpe(0).getAID;

      println( erpe(0) );
      println( wins );
      println( erpe(0).attribute( PlayerAttr.GAMES_PLAYED) );
      println( aid )
    }*/
  StatsFactory.dispose
}
