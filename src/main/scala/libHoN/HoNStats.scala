package libHoN;

import de.downgra.scarg._
import oldsch00l.Log;

class Configuration(m: ValueMap) extends ConfigMap(m) {
  val statstype = ("statstype", "ranked").as[String]
  val limit = ("limit", 3).as[Int]
  val command = ("command", "player").as[String]
  val items = ("items").asList[String]
}

case class CmdParser() extends ArgumentParser(new Configuration(_)) with DefaultHelpViewer {
  override val programName = Some("HoNStats")

  !"-s" | "--statstype" |^ "statstype" |* "ranked" |% "stats type [" + HoNStats.StatTypes.mkString(",") + "]" |> "statstype"
  !"-l" | "--limit" |^ "limit" |* 3 |% "limit output list size" |> "limit"
  +"command" |% "command [" + HoNStats.commands.mkString(",") + "]" |> "command"
  +"items" |% "items(nicks or matchids..." |*> "items"
}

object HoNStats extends App {
  Log.level = Log.Level.INFO
  val StatTypes = List("ranked", "public", "casual")
  val commands = List("player", "matches", "match")

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
              outputPlayer(c)
            }
            case "matches" => {
              outputMatches(c)
            }
            case "match" => {
              outputMatch(c)
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

  def outputPlayer(config: Configuration) = {
    val players = StatsFactory.getPlayerStatsByNick(config.items)

    val sHdOutput = "%-10s %-5s %-5s %-4s %-4s %-5s %4s %s"
    val sPlOutput = "%-10s %-5d %4d/%4d/%4d %5.2f  %4d %d"
    config.statstype match {
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

  def outputMatches(config: Configuration) = {
    val players = StatsFactory.getPlayerStatsByNickCached(config.items)

    for (player <- players) {
      val matches = player.getPlayedMatches(config.statstype, config.limit)

      println(player.attribute(PlayerAttr.NICKNAME))
      val showmatches = matches.reverse.take(config.limit)
      println(" %-9s %-5s %-16s  %2s %2s %2s  %4s %s %s %3s/%2s %s".format(
        "MID", "GD", "Date", "K", "D", "A", "Hero", "W/L", "Wards", "CK", "CD", "GPM"))
      for (outmatch <- showmatches) {
        Log.debug(outmatch.getMatchID.toString)
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

  def outputMatch(config: Configuration) = {
    val matches = StatsFactory.getMatchStatsByMatchId(for(matchid <- config.items) yield matchid.toInt)

    for (game <- matches) {
      println("Match " + game.getMatchID)
      val winTeam = game.getWinningTeam()
      val sLegion = if(winTeam == 1) "Legion(W)" else "Legion"
      val sHellbourne = if(winTeam == 2) "Hellbourne(W)" else "Hellbourne"
      val game_mins: Int = game.getMatchStatAsInt(MatchAttr.TIME_PLAYED) / 60

      println("%-19s %-4s %2s %2s %2s %3s %2s %3s %4s  %-19s %-4s %2s %2s %2s %3s %2s %3s %4s".format(
          sLegion, "Hero", "K", "D", "A", "CK", "CD", "GPM", "GL2D", sHellbourne, "Hero", "K", "D", "A", "CK", "CD", "GPM", "GL2D"))

      val legionPlayers = StatsFactory.getPlayerStatsByAidCached(game.getLegionPlayers)
      val legionStrings = for (player <- legionPlayers) yield
        "%-4d %-14s %-4s %2d %2d %2d %3d %2d %3d %4d  ".format(
            player.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
            game.getPlayerMatchStat(player.getAID, "nickname"),
            HeroAttr.getNick(game.getPlayerMatchStatAsInt(player.getAID, "hero_id")),
            game.getPlayerMatchStatAsInt(player.getAID, "herokills"),
            game.getPlayerMatchStatAsInt(player.getAID, "deaths"),
            game.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
            if (game_mins > 0) game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0,
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLDLOST2DEATH))

      val hellPlayers = StatsFactory.getPlayerStatsByAidCached(game.getHellbournePlayers)
      val hellStrings = for (player <- hellPlayers) yield
        "%-4d %-14s %-4s %2d %2d %2d %3d %2d %3d %4d".format(
          player.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
          game.getPlayerMatchStat(player.getAID, "nickname"),
          HeroAttr.getNick(game.getPlayerMatchStatAsInt(player.getAID, "hero_id")),
          game.getPlayerMatchStatAsInt(player.getAID, "herokills"),
          game.getPlayerMatchStatAsInt(player.getAID, "deaths"),
          game.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
          if (game_mins > 0) game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0,
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLDLOST2DEATH))


      val size = scala.Math.max(legionStrings.size, hellStrings.size)
      for (i <- 0 until size) {
        val hellLine = if(i < hellStrings.size) hellStrings(i) else ""
        if(i < legionStrings.size) {
          println(legionStrings(i) + hellLine)
        } else {
          println("%-35s".format(" ") + hellLine)
        }
      }
      println("--")
    }
  }
}
