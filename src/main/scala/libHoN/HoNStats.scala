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

  @Parameter(names = Array("-q", "--quiet"), description = "Tries to minimize output lines for errors/warnings")
  var quiet: Boolean = false
}

@Parameters(separators = "=", commandDescription = "Show player stats")
object CommandPlayer {
  @Parameter(required = true, description = "Nicknames to show stats")
  var nicks: java.util.List[String] = null
}

@Parameters(separators = "=", commandDescription = "Show matches of player")
object CommandMatches {
  @Parameter(required = true, description = "Nicknames")
  var nicks: java.util.List[String] = null
}

@Parameters(separators = "=", commandDescription = "Show stats for a match")
object CommandMatch {
  @Parameter(required = true, description = "Matchid's to show matches")
  var matchids: java.util.List[String] = null
}

@Parameters(separators = "=", commandDescription = "Show played heroes for a player")
object CommandPlayerHeroes {
  @Parameter(names = Array("-b", "--sort-by"), description = "Sort by [use,kdr,k,d,a,kpg,dpg,apg]")
  var sortBy: String = "use"

  @Parameter(required = true, description = "Nicknames")
  var nicks: java.util.List[String] = null
}

object HoNStats extends App {
  Log.level = Log.Level.WARNING

  lazy val dateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm")
  val outBuffer = new StringBuilder()

  // jcommander has an ambiguous constructor for scala
  // so we have to use reflection to create it
  val mkJC = classOf[JCommander].getConstructors.filter(_.getParameterTypes.length==1)
  val jc = mkJC.head.newInstance(CommandMain).asInstanceOf[JCommander]
  jc.setProgramName("HoNStats")
  jc.addCommand("player", CommandPlayer)
  jc.addCommand("matches", CommandMatches)
  jc.addCommand("match", CommandMatch)
  jc.addCommand("player-heroes", CommandPlayerHeroes);

  def printHelp() = {
    if(CommandMain.quiet)
      println("Misusage of HoNStats see https://github.com/rp-/HoNStats for details")
    else
     jc.usage()
  }

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
      case "player-heroes" => {
        outputPlayerHeroes(CommandPlayerHeroes.nicks.toList)
      }
      case null => {
        printHelp
      }
    }
  } catch {
    case e: ParameterException => {
      printHelp
      System.exit(1)
    }
    case e => {
      if (CommandMain.debug)
        e.printStackTrace()
      else
        println("HoNStats couldn't perform your request")
      System.exit(1)
    }
  } finally {
    StatsFactory.dispose
  }

  // finally print the output
  if(!outBuffer.isEmpty)
    println(outBuffer)

  def outputPlayer(nicknames: List[String]) = {
    val players = StatsFactory.getPlayerStatsByNick(nicknames)

    val sHdOutput = "%-10s %-5s %-5s %-4s %-4s %-3s %-4s  %-4s %4s %2s\n"
    val sPlOutput = "%-10s %-5d %4d/%4d/%4d %4.1f %4.1f %5.2f %4d %2.0f\n"
    CommandMain.statstype match {
      case "ranked" =>
        outBuffer.append(sHdOutput.format("Nick", "MMR", "K", "D", "A", "W/G", "CD", "KDR", "MGP", "W%"))
        players.foreach(p =>
          outBuffer.append(sPlOutput.format(
            p.NickName,
            p.RankedSR.toInt,
            p.RankedKills,
            p.RankedDeaths,
            p.RankedAssists,
            p.RankedWards.toFloat / p.gamesplayed(CommandMain.statstype), // Wards per game
            p.RankedDenies.toFloat / p.gamesplayed(CommandMain.statstype), // Creep denies
            PlayerAttr.calcRatio(p.RankedKills, p.RankedDeaths), // KDR
            p.gamesplayed(CommandMain.statstype), //MGP
            p.RankedWins.toFloat / p.gamesplayed(CommandMain.statstype) * 100 // W%
            )
          )
        )
      case "public" =>
        outBuffer.append(sHdOutput.format("Nick", "MMR", "K", "D", "A", "W/G", "CD", "KDR", "GP", "W%"))

        players.foreach(p =>
          outBuffer.append(sPlOutput.format(
            p.NickName,
            p.PublicSR.toInt,
            p.PublicKills,
            p.PublicDeaths,
            p.PublicAssists,
            p.PublicWards.toFloat / p.gamesplayed(CommandMain.statstype), // Wards per game
            p.PublicDenies.toFloat / p.gamesplayed(CommandMain.statstype), // Creep denies
            PlayerAttr.calcRatio(p.PublicKills, p.PublicDeaths), // KDR
            p.gamesplayed(CommandMain.statstype), // GP
            p.PublicWins.toFloat / p.gamesplayed(CommandMain.statstype) * 100 // W%
            )
          )
        )
      case "casual" =>
        outBuffer.append(sHdOutput.format("Nick", "MMR", "K", "D", "A", "W/G", "CD", "KDR", "CGP", "W%"))

        players.foreach(p =>
          outBuffer.append(sPlOutput.format(
            p.NickName,
            p.CasualSR.toInt,
            p.CasualKills,
            p.CasualDeaths,
            p.CasualAssists,
            p.CasualWards.toFloat / p.gamesplayed(CommandMain.statstype), // Wards per game
            p.CasualDenies.toFloat / p.gamesplayed(CommandMain.statstype), // Creep denies
            PlayerAttr.calcRatio(p.CasualKills, p.CasualDeaths), // KDR
            p.gamesplayed(CommandMain.statstype), // CGP
            p.CasualWins.toFloat / p.gamesplayed(CommandMain.statstype) * 100 // W%
            )
          )
        )
    }
  }

  def outputMatches(nicknames: List[String]) = {
    val players = StatsFactory.getPlayerStatsByNick(nicknames)

    for (player <- players) {
      val showmatches = player.getPlayedMatches(CommandMain.statstype, CommandMain.limit)

      outBuffer.append(player.NickName)
      outBuffer.append(" (")
      outBuffer.append(CommandMain.statstype)
      outBuffer.append(")\n")

      outBuffer.append(" %-9s %-5s %-16s  %2s %2s %2s  %4s %s %s %3s/%2s %s\n".format(
        "MID", "GD", "Date", "K", "D", "A", "Hero", "W/L", "Wards", "CK", "CD", "GPM"))
      for (outmatch <- showmatches.reverse) {
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

      outBuffer.append("%-19s %-4s %2s %2s %2s %2s %3s %2s %2s %3s %4s  %-19s %-4s %2s %2s %2s %2s %3s %2s %2s %3s %4s\n".format(
          sLegion, "Hero", "LV", "K", "D", "A", "CK", "CD", " W", "GPM", "GL2D", sHellbourne, "Hero", "LV", "K", "D", "A", "CK", "CD", " W", "GPM", "GL2D"))

      val legionPlayers = StatsFactory.getPlayerStatsByAID(game.getLegionPlayers)
      val legionStrings = for (player <- legionPlayers) yield
        "%-4d %-14s %-4s %2d %2d %2d %2d %3d %2d %2d %3d %4d  ".format(
            player.attrAsFloat(PlayerAttr.RANK_AMM_TEAM_RATING).toInt,
            player.NickName,
            HeroAttr.getNick(game.getPlayerMatchStatAsInt(player.getAID, "hero_id")),
            game.getPlayerMatchStatAsInt(player.getAID, "level"),
            game.getPlayerMatchStatAsInt(player.getAID, "herokills"),
            game.getPlayerMatchStatAsInt(player.getAID, "deaths"),
            game.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.WARDS),
            if (game_mins > 0) game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLD) / game_mins else 0,
            game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.GOLDLOST2DEATH))

      val hellPlayers = StatsFactory.getPlayerStatsByAID(game.getHellbournePlayers)
      val hellStrings = for (player <- hellPlayers) yield
        "%-4d %-14s %-4s %2d %2d %2d %2d %3d %2d %2d %3d %4d\n".format(
          player.attrAsFloat(PlayerAttr.RANK_AMM_TEAM_RATING).toInt,
          player.NickName,
          HeroAttr.getNick(game.getPlayerMatchStatAsInt(player.getAID, "hero_id")),
          game.getPlayerMatchStatAsInt(player.getAID, "level"),
          game.getPlayerMatchStatAsInt(player.getAID, "herokills"),
          game.getPlayerMatchStatAsInt(player.getAID, "deaths"),
          game.getPlayerMatchStatAsInt(player.getAID, "heroassists"),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.TEAMCREEPKILLS) + game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.NEUTRALCREEPKILLS),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.DENIES),
          game.getPlayerMatchStatAsInt(player.getAID, MatchPlayerAttr.WARDS),
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

  def outputPlayerHeroes(nicknames: List[String]) = {
    val players = StatsFactory.getPlayerStatsByNick(nicknames)

    for (player <- players) {
      val playedHeros = player.getPlayedHeros(CommandMain.statstype)

      def sortHeroes(h1: PlayerHeroStats, h2: PlayerHeroStats) : Boolean = {
        CommandPlayerHeroes.sortBy match {
          case "use" => return h1.used > h2.used
          case "kdr" => return PlayerAttr.calcRatio(h1.kills, h1.deaths) > PlayerAttr.calcRatio(h2.kills, h2.deaths)
          case "k" => return h1.kills > h2.kills
          case "d" => return h1.deaths > h2.deaths
          case "a" => return h1.assists > h2.assists
          case "kpg" => return PlayerAttr.calcRatio(h1.kills, h1.used) > PlayerAttr.calcRatio(h2.kills, h2.used)
          case "dpg" => return PlayerAttr.calcRatio(h1.deaths, h1.used) > PlayerAttr.calcRatio(h2.deaths, h2.used)
          case "apg" => return PlayerAttr.calcRatio(h1.assists, h1.used) > PlayerAttr.calcRatio(h2.assists, h2.used)
          case x => throw new RuntimeException("sort mode not supported.")
        }
      }

      val matches = player.getPlayedMatchesCount(CommandMain.statstype)
      val sortedHeros = playedHeros.sortWith((h1, h2) => sortHeroes(h1, h2))

      outBuffer.append(player.NickName)
      outBuffer.append(" (")
      outBuffer.append(CommandMain.statstype)
      outBuffer.append(")\n")
      outBuffer.append("%-20s %-3s %-2s %3s  %3s  %3s    %-3s %-2s %-2s %5s %5s %5s\n".
        format("Hero", "Use", " %", "K", "D", "A", "KDR", " W", " L", "  KPG", "  DPG", "  APG"))
      for (hero <- if (CommandMain.limit > 0) sortedHeros.take(CommandMain.limit) else sortedHeros) {
        outBuffer.append("%-20s %3d %2d %4d %4d %4d %5.2f %2d %2d %5.2f %5.2f %5.2f\n".format(
          HeroAttr.IDMap(hero.HeroID),
          hero.used,
          ((hero.used.toFloat / matches.toFloat) * 100).toInt,
          hero.kills,
          hero.deaths,
          hero.assists,
          PlayerAttr.calcRatio(hero.kills, hero.deaths),
          hero.wins,
          hero.loses,
          PlayerAttr.calcRatio(hero.kills, hero.used),
          PlayerAttr.calcRatio(hero.deaths, hero.used),
          PlayerAttr.calcRatio(hero.assists, hero.used))
        )
      }
    }
  }
}
