package libHoN;

import de.downgra.scarg._

class Configuration(m: ValueMap) extends ConfigMap(m) {
  val statstype = ("statstype", "ranked").as[String]
  val limit = ("limit", 3).as[Int]
  val command = ("command", "player").as[String]
  val nicks = ("nicks").asList[String]
}

case class CmdParser() extends ArgumentParser(new Configuration(_)) with DefaultHelpViewer {
  override val programName = Some("HoNStats")

  !"-s" | "--statstype" |^ "statstype" |* "ranked" |% "stats type [" + HoNStats.StatTypes.mkString(",") + "]" |> "statstype"
  !"-l" | "--limit" |^ "limit" |* 3 |% "limit output list size" |> "limit"
  +"command" |% "command [" + HoNStats.commands.mkString(",") + "]" |> "command"
  +"nicks" |% "nicks..." |*> "nicks"
}

object HoNStats extends App {
  val StatTypes = List("ranked", "public", "casual")
  val commands = List("player", "matches")

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")

  try {
    CmdParser().parse(args) match {
      case Right(c) =>
        {
          if (!StatTypes.contains(c.statstype)) {
            System.err.println("Stats type: " + c.statstype + " unknown.")
            System.exit(2)
          }

          c.command match {
            case "player" => {
              val players = StatsFactory.getPlayerStatsByNick(c.nicks)

              val sHdOutput = "%-10s %-5s %-5s %-4s %-4s %-5s %4s %s"
              val sPlOutput = "%-10s %-5d %4d/%4d/%4d %5.2f  %4d %d"
              c.statstype match {
                case "ranked" =>
                  println(sHdOutput.format("Nick", "MMR", "K", "D", "A", "KDR", "MGP", "AID"))
                  players.foreach(p =>
                    println(sPlOutput.format(
                      p.attribute(PlayerAttr.NICKNAME),
                      p.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
                      p.attribute(PlayerAttr.RANK_HEROKILLS).toInt,
                      p.attribute(PlayerAttr.RANK_DEATHS).toInt,
                      p.attribute(PlayerAttr.RANK_HEROASSISTS).toInt,
                      (p.attribute(PlayerAttr.RANK_HEROKILLS).toFloat / p.attribute(PlayerAttr.RANK_DEATHS).toFloat),
                      p.attribute(PlayerAttr.RANK_GAMES_PLAYED).toInt,
                      p.getAID.toInt)))
                case "public" =>
                  println(sHdOutput.format("Nick", "MMR", "K", "D", "A", "KDR", "GP", "AID"))

                  players.foreach(p =>
                    println(sPlOutput.format(
                      p.attribute(PlayerAttr.NICKNAME),
                      p.attribute(PlayerAttr.SKILL).toFloat.toInt,
                      p.attribute(PlayerAttr.HEROKILLS).toInt,
                      p.attribute(PlayerAttr.DEATHS).toInt,
                      p.attribute(PlayerAttr.HEROASSISTS).toInt,
                      (p.attribute(PlayerAttr.HEROKILLS).toFloat / p.attribute(PlayerAttr.DEATHS).toFloat),
                      p.attribute(PlayerAttr.GAMES_PLAYED).toInt,
                      p.getAID.toInt)))
                case "casual" =>
                  println(sHdOutput.format("Nick", "MMR", "K", "D", "A", "KDR", "CGP", "AID"))

                  players.foreach(p =>
                    println(sPlOutput.format(
                      p.attribute(PlayerAttr.NICKNAME),
                      p.attribute(PlayerAttr.CS_AMM_TEAM_RATING).toFloat.toInt,
                      p.attribute(PlayerAttr.CS_HEROKILLS).toInt,
                      p.attribute(PlayerAttr.CS_DEATHS).toInt,
                      p.attribute(PlayerAttr.CS_HEROASSISTS).toInt,
                      (p.attribute(PlayerAttr.CS_HEROKILLS).toFloat / p.attribute(PlayerAttr.CS_DEATHS).toFloat),
                      p.attribute(PlayerAttr.CS_GAMES_PLAYED).toInt,
                      p.getAID.toInt)))
              }
            }
            case "matches" => {
              val players = StatsFactory.getPlayerStatsByNick(c.nicks)

              for (player <- players) {
                val matches = player.getPlayedMatches(c.statstype, c.limit)

                println(player.attribute(PlayerAttr.NICKNAME))
                val showmatches = matches.reverse.take(c.limit)
                println(" %-9s %-5s %-16s  %2s %2s %2s  %4s %s %s %3s/%2s %s".format(
                  "MID", "GD", "Date", "K", "D", "A", "Hero", "W/L", "Wards", "CK", "CD", "GPM"))
                for (outmatch <- showmatches) {
                  val game_mins: Int = outmatch.getMatchStatAsInt(MatchAttr.TIME_PLAYED) / 60
                  val gpm = if (game_mins > 0) outmatch.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0
                  println(" %-9d %5s %-16s  %2d/%2d/%2d  %-4s %-3s %5s %3d/%2d %3d".format(
                    outmatch.getMatchID,
                    outmatch.getGameDuration,
                    dateFormat.format(outmatch.getLocalMatchDateTime),
                    outmatch.getPlayerMatchStatAsInt(player.getAID, "herokills"),
                    outmatch.getPlayerMatchStatAsInt(player.getAID, "deaths"),
                    outmatch.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
                    HeroAttr.getNick(outmatch.getPlayerMatchStat(player.getAID, "hero_id").toInt),
                    if (outmatch.playerWon(player.getAID)) "W" else "L",
                    outmatch.getPlayerMatchStat(player.getAID, MatchPlayerAttr.WARDS),
                    outmatch.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + outmatch.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
                    outmatch.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
                    gpm))
                }
              }
            }
            case x => {
              println("Unknown command: '" + x + "'")
              println("Allowed commands: " + commands.mkString(","))
            }
          }
        }
      case Left(xs) =>
    }
  } finally {
    StatsFactory.dispose
  }
}
