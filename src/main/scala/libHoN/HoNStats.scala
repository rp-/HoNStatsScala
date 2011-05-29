package libHoN;

import scopt._

object HoNStats extends App {

  try {
    var nicks: List[String] = Nil
    val parser = new OptionParser("HoNStats") {
      arglist("nicks...", "nicknames of players to retrieve stats",
        { v: String => nicks = (v :: nicks) })
    }
    parser.parse(args)

    val players = StatsFactory.getPlayerStatsByNick(nicks)

    players.foreach(p =>
      println("%s: MMR-> %d".format(
        p.attribute(PlayerAttr.NICKNAME),
        p.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt)))
    /*
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
    */
  } finally {
    StatsFactory.dispose
  }
}
