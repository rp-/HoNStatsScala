package libHoN;

import scopt._

object HoNStats extends App {

  try {
    var honargs: List[String] = Nil
    var limit: Int = 1
    val commands = List("player", "match")
    val parser = new OptionParser("HoNStats") {
      //arg("command", "command one of [" + commands.mkString(",") + "]", { v: String => command = v })
      intOpt("l", "limit", "limit output list size", { v: Int => limit = v })
      arglist("command nicks...", "command[" + commands.mkString(",") + "], nicknames of players to retrieve stats",
        { v: String => honargs = (v :: honargs) })
    }
    if (parser.parse(args)) {
      val command = honargs.reverse.head
      val nicks = honargs.reverse.tail
      command match {
        case "player" => {
          val players = StatsFactory.getPlayerStatsByNick(nicks)

          println("%-10s %-5s %-14s %-4s".format("Nick", "MMR", "KDA", "KDR"))
          players.foreach(p =>
            println("%-10s %-5d %-4d/%-4d/%-4d %f".format(
              p.attribute(PlayerAttr.NICKNAME),
              p.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
              p.attribute(PlayerAttr.RANK_HEROKILLS).toInt,
              p.attribute(PlayerAttr.RANK_DEATHS).toInt,
              p.attribute(PlayerAttr.RANK_HEROASSISTS).toInt,
              (p.attribute(PlayerAttr.RANK_HEROKILLS).toFloat / p.attribute(PlayerAttr.RANK_DEATHS).toFloat))))
        }
        case "match" => {
          val players = StatsFactory.getPlayerStatsByNick(nicks)

          for (player <- players) {
            val matches = player.getPlayedMatches(limit)

            println(player.attribute(PlayerAttr.NICKNAME))
            val showmatches = matches.reverse.take(limit)
            for (outmatch <- showmatches) {
              println(" * mid: %d; KDA(%d/%d/%d)".format(
                outmatch.getMatchID,
                outmatch.getPlayerMatchStat(player.getAID, "herokills").toInt,
                outmatch.getPlayerMatchStat(player.getAID, "deaths").toInt,
                outmatch.getPlayerMatchStat(player.getAID, "heroassists").toInt))
            }
          }
        }
        case x => {
          println("Unknown command: '" + x + "'")
          println("Allowed commands: " + commands.mkString(","))
        }
      }
    }
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
