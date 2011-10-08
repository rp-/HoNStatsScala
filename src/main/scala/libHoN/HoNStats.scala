package libHoN;

import oldsch00l.Log
import scala.collection.JavaConversions._
import com.beust.jcommander.{JCommander, Parameter, Parameters, ParameterException}

@Parameters(separators = "=")
object CommandMain {
  @Parameter(names = Array("-l", "--limit"), description = "Maximum output of items")
  var limit: Int = 5

  @Parameter(names = Array("-s", "--statstype"), description = "StatsType to show: [ranked,public,casual]")
  var statstype: String = "ranked"

  @Parameter(names = Array("-d", "--debug"), description = "Show debug output")
  var debug: Boolean = false

  @Parameter(names = Array("-f", "--fetch"), description = "Don't use cache")
  var fetch: Boolean = false
}

@Parameters(separators = "=", commandDescription = "Show player stats")
object CommandPlayer {
  @Parameter(description = "Nicknames to show stats")
  var nicks: java.util.List[String] = null
}

@Parameters(separators = "=", commandDescription = "Show matches of player")
object CommandMatches {
  @Parameter(description = "Nicknames")
  var nicks: java.util.List[String] = null
}

@Parameters(separators = "=", commandDescription = "Show stats for a match")
object CommandMatch {
  @Parameter(description = "Matchid's to show matches")
  var matchids: java.util.List[String] = null
}

object HoNStats extends App {
  Log.level = Log.Level.INFO

  val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
  val outBuffer = new StringBuilder()

  // jcommander has an ambiguous constructor for scala
  // so we have to use reflection to create it
  val mkJC = classOf[JCommander].getConstructors.filter(_.getParameterTypes.length==1)
  val jc = mkJC.head.newInstance(CommandMain).asInstanceOf[JCommander]
  jc.addCommand("player", CommandPlayer)
  jc.addCommand("matches", CommandMatches)
  jc.addCommand("match", CommandMatch)

  try {
    jc.parse(args.toArray: _*)
	  jc.getParsedCommand() match {
	    case "player" => {
	      outputPlayer(CommandPlayer.nicks.toList)
	    }
	    case "matches" => {
	      outputMatches(CommandMatches.nicks.toList)
	    }
	    case "match" => {
	      outputMatch(CommandMatch.matchids.toList)
	    }
	    case null => {
	      print(jc.usage())
	    }
	  }
  } catch {
    case e:ParameterException => {
      print(jc.usage())
    }
    case e => {
      if(CommandMain.debug)
        e.printStackTrace()
      else
        println("HoNStats couldn't perform your request")
        System.exit(1)
    }
  } finally {
    StatsFactory.dispose
  }

  // finally print the output
  println(outBuffer)

  def outputPlayer(nicknames: List[String]) = {
    val players = StatsFactory.getPlayerStatsByNick(nicknames)

    val sHdOutput = "%-10s %-5s %-5s %-4s %-4s %-5s %4s %s\n"
    val sPlOutput = "%-10s %-5d %4d/%4d/%4d %5.2f  %4d %d\n"
    CommandMain.statstype match {
      case "ranked" =>
        outBuffer.append(sHdOutput.format("Nick", "MMR", "K", "D", "A", "KDR", "MGP", "AID"))
        players.foreach(p =>
          outBuffer.append(sPlOutput.format(
            p.attribute(PlayerAttr.NICKNAME),
            p.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
            p.attribute(PlayerAttr.RANK_HEROKILLS).toInt,
            p.attribute(PlayerAttr.RANK_DEATHS).toInt,
            p.attribute(PlayerAttr.RANK_HEROASSISTS).toInt,
            (p.attribute(PlayerAttr.RANK_HEROKILLS).toFloat / p.attribute(PlayerAttr.RANK_DEATHS).toFloat),
            p.attribute(PlayerAttr.RANK_GAMES_PLAYED).toInt,
            p.getAID.toInt)))
      case "public" =>
        outBuffer.append(sHdOutput.format("Nick", "MMR", "K", "D", "A", "KDR", "GP", "AID"))

        players.foreach(p =>
          outBuffer.append(sPlOutput.format(
            p.attribute(PlayerAttr.NICKNAME),
            p.attribute(PlayerAttr.SKILL).toFloat.toInt,
            p.attribute(PlayerAttr.HEROKILLS).toInt,
            p.attribute(PlayerAttr.DEATHS).toInt,
            p.attribute(PlayerAttr.HEROASSISTS).toInt,
            (p.attribute(PlayerAttr.HEROKILLS).toFloat / p.attribute(PlayerAttr.DEATHS).toFloat),
            p.attribute(PlayerAttr.GAMES_PLAYED).toInt,
            p.getAID.toInt)))
      case "casual" =>
        outBuffer.append(sHdOutput.format("Nick", "MMR", "K", "D", "A", "KDR", "CGP", "AID"))

        players.foreach(p =>
          outBuffer.append(sPlOutput.format(
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

  def outputMatches(nicknames: List[String]) = {
    val players = StatsFactory.getPlayerStatsByNickCached(nicknames)

    for (player <- players) {
      val showmatches = player.getPlayedMatches(CommandMain.statstype, CommandMain.limit)

      outBuffer.append(player.attribute(PlayerAttr.NICKNAME) + "\n")

      outBuffer.append(" %-9s %-5s %-16s  %2s %2s %2s  %4s %s %s %3s/%2s %s\n".format(
        "MID", "GD", "Date", "K", "D", "A", "Hero", "W/L", "Wards", "CK", "CD", "GPM"))
      for (outmatch <- showmatches) {
        Log.debug(outmatch.getMatchID.toString)
        val game_mins: Int = outmatch.getMatchStatAsInt(MatchAttr.TIME_PLAYED) / 60
        val gpm = if (game_mins > 0) outmatch.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0
        outBuffer.append(" %-9d %5s %-16s  %2d/%2d/%2d  %-4s %-3s %5s %3d/%2d %3d\n".format(
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

  def outputMatch(matchids: List[String]) = {
    val matches = StatsFactory.getMatchStatsByMatchId(for(matchid <- matchids) yield matchid.toInt)

    for (game <- matches) {
      outBuffer.append("Match %d -- %s - GD: %s\n".format(game.getMatchID, dateFormat.format(game.getLocalMatchDateTime), game.getGameDuration()))

      val winTeam = game.getWinningTeam()
      val sLegion = if(winTeam == 1) "Legion(W)" else "Legion"
      val sHellbourne = if(winTeam == 2) "Hellbourne(W)" else "Hellbourne"
      val game_mins: Int = game.getMatchStatAsInt(MatchAttr.TIME_PLAYED) / 60

      outBuffer.append("%-19s %-4s %2s %2s %2s %2s %3s %2s %3s %4s  %-19s %-4s %2s %2s %2s %2s %3s %2s %3s %4s\n".format(
          sLegion, "Hero", "LV", "K", "D", "A", "CK", "CD", "GPM", "GL2D", sHellbourne, "Hero", "LV", "K", "D", "A", "CK", "CD", "GPM", "GL2D"))

      val legionPlayers = if (CommandMain.fetch)
          StatsFactory.getPlayerStatsByAid(game.getLegionPlayers)
        else
          StatsFactory.getPlayerStatsByAidCached(game.getLegionPlayers)

      val legionStrings = for (player <- legionPlayers) yield
        "%-4d %-14s %-4s %2d %2d %2d %2d %3d %2d %3d %4d  ".format(
            player.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
            game.getPlayerMatchStat(player.getAID, "nickname"),
            HeroAttr.getNick(game.getPlayerMatchStatAsInt(player.getAID, "hero_id")),
            game.getPlayerMatchStatAsInt(player.getAID, "level"),
            game.getPlayerMatchStatAsInt(player.getAID, "herokills"),
            game.getPlayerMatchStatAsInt(player.getAID, "deaths"),
            game.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
            if (game_mins > 0) game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0,
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLDLOST2DEATH))

      val hellPlayers = if (CommandMain.fetch)
          StatsFactory.getPlayerStatsByAid(game.getHellbournePlayers)
        else
          StatsFactory.getPlayerStatsByAidCached(game.getHellbournePlayers)
      val hellStrings = for (player <- hellPlayers) yield
        "%-4d %-14s %-4s %2d %2d %2d %2d %3d %2d %3d %4d\n".format(
          player.attribute(PlayerAttr.RANK_AMM_TEAM_RATING).toFloat.toInt,
          game.getPlayerMatchStat(player.getAID, "nickname"),
          HeroAttr.getNick(game.getPlayerMatchStatAsInt(player.getAID, "hero_id")),
          game.getPlayerMatchStatAsInt(player.getAID, "level"),
          game.getPlayerMatchStatAsInt(player.getAID, "herokills"),
          game.getPlayerMatchStatAsInt(player.getAID, "deaths"),
          game.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
          if (game_mins > 0) game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0,
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLDLOST2DEATH))


      val size = scala.math.max(legionStrings.size, hellStrings.size)
      for (i <- 0 until size) {
        val hellLine = if(i < hellStrings.size) hellStrings(i) else ""
        if(i < legionStrings.size) {
          outBuffer.append(legionStrings(i) + hellLine)
        } else {
          outBuffer.append("%-35s".format(" ") + hellLine)
        }
      }
      outBuffer.append("--\n")
    }
  }
}
