package libHoN

import scala.xml._;

class PlayerStats(playerData: scala.xml.Node) {
  def attribute(name: String): String = {
    (playerData \ "stat").filter(attributeNameValueEquals(name)).text
  }

  def attributeNameValueEquals(value: String)(node: scala.xml.Node) = {
    (node \ "@name").toString == value
  }

  def getAID: String = playerData.attribute("aid").get.text;

  def getPlayedMatches(maxMatches: Int = 0): List[MatchStats] = {
    val xmlData = XML.load(StatsFactory.XMLRequester + "?f=ranked_history&opt=aid&aid[]=" + getAID)
    assert(xmlData != Nil)
    val mids = (for { id <- (xmlData \\ "id") } yield id.text.toInt).toList

    val matches = StatsFactory.getMatchStatsByMatchId(if (maxMatches > 0) mids.reverse.take(maxMatches) else mids)
    //println(matches.size + ":" + mids.size)
    //assert(matches.size == mids.size)
    matches
  }

  override def toString = playerData.toString
}

object PlayerAttr {
  val NICKNAME = "nickname";
  val GAMES_PLAYED = "acc_games_played";
  val WINS = "acc_wins";
  val LOSSES = "acc_losses";
  val CONCEDES = "acc_concedes";
  val CONCEDEVOTES = "acc_concedevotes";
  val BUYBACKS = "acc_buybacks";
  val DISCOS = "acc_discos";
  val KICKED = "acc_kicked";
  val SKILL = "acc_pub_skill";
  val PUB_COUNT = "acc_pub_count";
  val AMM_SOLO_RATING = "acc_amm_solo_rating";
  val AMM_SOLO_COUNT = "acc_amm_solo_count";
  val AMM_TEAM_RATING = "acc_amm_team_rating";
  val AMM_TEAM_COUNT = "acc_amm_team_count";
  val AVG_SCORE = "acc_avg_score";
  val HEROKILLS = "acc_herokills";
  val HERODMG = "acc_herodmg";
  val HEROEXP = "acc_heroexp";
  val HEROKILLSGOLD = "acc_herokillsgold";
  val HEROASSISTS = "acc_heroassists";
  val DEATHS = "acc_deaths";
  val GOLDLOST2DEATH = "acc_goldlost2death";
  val SECS_DEAD = "acc_secs_dead";
  val TEAMCREEPKILLS = "acc_teamcreepkills";
  val TEAMCREEPDMG = "acc_teamcreepdmg";
  val TEAMCREEPEXP = "acc_teamcreepexp";
  val TEAMCREEPGOLD = "acc_teamcreepgold";
  val NEUTRALCREEPKILLS = "acc_neutralcreepkills";
  val NEUTRALCREEPDMG = "acc_neutralcreepdmg";
  val NEUTRALCREEPEXP = "acc_neutralcreepexp";
  val NEUTRALCREEPGOLD = "acc_neutralcreepgold";
  val BUILDINGDMG = "acc_bdmg";
  val BUILDINGDMGEXP = "acc_bdmgexp";
  val RAZED = "acc_razed";
  val BUILDINGGOLD = "acc_bgold";
  val DENIES = "acc_denies";
  val DENIED_EXP = "acc_exp_denied";
  val GOLD = "acc_gold";
  val GOLD_SPENT = "acc_gold_spent";
  val EXP = "acc_exp";
  val ACTIONS = "acc_actions";
  val SECS = "acc_secs";
  val CONSUMABLES = "acc_consumables";
  val WARDS = "acc_wards";
  val EM_PLAYED = "acc_em_played";
  val AR = "AR";
  val AREM = "AREM";
  val AP = "AP";
  val APEM = "APEM";
  val LEVEL = "level";
  val TOTAL_DISCOS = "total_discos"
  val TOTAL_POSSIBLE_DISCOS = "total_possible_discos"
  val RANK_GAMES_PLAYED = "rnk_games_played"
  val RANK_WINS = "rnk_wins"
  val RANK_LOSSES = "rnk_losses"
  val RANK_CONCEDES = "rnk_concedes"
  val RANK_CONCEDEVOTES = "rnk_concedevotes"
  val RANK_BUYBACKS = "rnk_buybacks"
  val RANK_DISCOS = "rnk_discos"
  val RANK_KICKED = "rnk_kicked"
  val RANK_AMM_SOLO_RATING = "rnk_amm_solo_rating"
  val RANK_AMM_SOLO_COUNT = "rnk_amm_solo_count"
  val RANK_AMM_SOLO_CONF = "rnk_amm_solo_conf"
  val RANK_AMM_SOLO_PROV = "rnk_amm_solo_prov"
  val RANK_AMM_SOLO_PSET = "rnk_amm_solo_pset"
  val RANK_AMM_TEAM_RATING = "rnk_amm_team_rating"
  val RANK_AMM_TEAM_COUNT = "rnk_amm_team_count"
  val RANK_AMM_TEAM_CONF = "rnk_amm_team_conf"
  val RANK_AMM_TEAM_PROV = "rnk_amm_team_prov"
  val RANK_AMM_TEAM_PSET = "rnk_amm_team_pset"
  val RANK_HEROKILLS = "rnk_herokills"
  val RANK_HERODMG = "rnk_herodmg"
  val RANK_HEROEXP = "rnk_heroexp"
  val RANK_HEROKILLSGOLD = "rnk_herokillsgold"
  val RANK_HEROASSISTS = "rnk_heroassists"
  val RANK_DEATHS = "rnk_deaths"
  val RANK_GOLDLOST2DEATH = "rnk_goldlost2death"
  val RANK_SECS_DEAD = "rnk_secs_dead"
  val RANK_TEAMCREEPKILLS = "rnk_teamcreepkills"
  val RANK_TEAMCREEPDMG = "rnk_teamcreepdmg"
  val RANK_TEAMCREEPEXP = "rnk_teamcreepexp"
  val RANK_TEAMCREEPGOLD = "rnk_teamcreepgold"
  val RANK_NEUTRALCREEPKILLS = "rnk_neutralcreepkills"
  val RANK_NEUTRALCREEPDMG = "rnk_neutralcreepdmg"
  val RANK_NEUTRALCREEPEXP = "rnk_neutralcreepexp"
  val RANK_NEUTRALCREEPGOLD = "rnk_neutralcreepgold"
  val RANK_BDMG = "rnk_bdmg"
  val RANK_BDMGEXP = "rnk_bdmgexp"
  val RANK_RAZED = "rnk_razed"
  val RANK_BGOLD = "rnk_bgold"
  val RANK_DENIES = "rnk_denies"
  val RANK_EXP_DENIED = "rnk_exp_denied"
  val RANK_GOLD = "rnk_gold"
  val RANK_GOLD_SPENT = "rnk_gold_spent"
  val RANK_EXP = "rnk_exp"
  val RANK_ACTIONS = "rnk_actions"
  val RANK_SECS = "rnk_secs"
  val RANK_CONSUMABLES = "rnk_consumables"
  val RANK_WARDS = "rnk_wards"
  val RANK_EM_PLAYED = "rnk_em_played"
  val RANK_LEVEL = "rnk_level"
  val RANK_LEVEL_EXP = "rnk_level_exp"
  val RANK_MIN_EXP = "rnk_min_exp"
  val RANK_MAX_EXP = "rnk_max_exp"
  val RANK_TIME_EARNING_EXP = "rnk_time_earning_exp"
  val RANK_BLOODLUST = "rnk_bloodlust"
  val RANK_DOUBLEKILL = "rnk_doublekill"
  val RANK_TRIPLEKILL = "rnk_triplekill"
  val RANK_QUADKILL = "rnk_quadkill"
  val RANK_ANHIHILATION = "rnk_annihilation"
  val RANK_KILLSERIES3 = "rnk_ks3"
  val RANK_KILLSERIES4 = "rnk_ks4"
  val RANK_KILLSERIES5 = "rnk_ks5"
  val RANK_KILLSERIES6 = "rnk_ks6"
  val RANK_KILLSERIES7 = "rnk_ks7"
  val RANK_KILLSERIES8 = "rnk_ks8"
  val RANK_KILLSERIES9 = "rnk_ks9"
  val RANK_KILLSERIES10 = "rnk_ks10"
  val RANK_KILLSERIES15 = "rnk_ks15"
  val RANK_SMACKDOWN = "rnk_smackdown"
  val RANK_HUMILIATION = "rnk_humiliation"
  val RANK_NEMESIS = "rnk_nemesis"
  val RANK_RETRIBUTION = "rnk_retribution"
  val CS_GAMES_PLAYED = "cs_games_played"
  val CS_WINS = "cs_wins"
  val CS_LOSSES = "cs_losses"
  val CS_CONCEDES = "cs_concedes"
  val CS_CONCEDEVOTES = "cs_concedevotes"
  val CS_BUYBACKS = "cs_buybacks"
  val CS_DISCOS = "cs_discos"
  val CS_KICKED = "cs_kicked"
  val CS_AMM_TEAM_RATING = "cs_amm_team_rating"
  val CS_AMM_TEAM_COUNT = "cs_amm_team_count"
  val CS_AMM_TEAM_CONF = "cs_amm_team_conf"
  val CS_AMM_TEAM_PROV = "cs_amm_team_prov"
  val CS_AMM_TEAM_PSET = "cs_amm_team_pset"
  val CS_HEROKILLS = "cs_herokills"
  val CS_HERODMG = "cs_herodmg"
  val CS_HEROEXP = "cs_heroexp"
  val CS_HEROKILLSGOLD = "cs_herokillsgold"
  val CS_HEROASSISTS = "cs_heroassists"
  val CS_DEATHS = "cs_deaths"
  val CS_GOLDLOST2DEATH = "cs_goldlost2death"
  val CS_SECS_DEAD = "cs_secs_dead"
  val CS_TEAMCREEPKILLS = "cs_teamcreepkills"
  val CS_TEAMCREEPDMG = "cs_teamcreepdmg"
  val CS_TEAMCREEPEXP = "cs_teamcreepexp"
  val CS_TEAMCREEPGOLD = "cs_teamcreepgold"
  val CS_NEUTRALCREEPKILLS = "cs_neutralcreepkills"
  val CS_NEUTRALCREEPDMG = "cs_neutralcreepdmg"
  val CS_NEUTRALCREEPEXP = "cs_neutralcreepexp"
  val CS_NEUTRALCREEPGOLD = "cs_neutralcreepgold"
  val CS_BDMG = "cs_bdmg"
  val CS_BDMGEXP = "cs_bdmgexp"
  val CS_RAZED = "cs_razed"
  val CS_BGOLD = "cs_bgold"
  val CS_DENIES = "cs_denies"
  val CS_EXP_DENIED = "cs_exp_denied"
  val CS_GOLD = "cs_gold"
  val CS_GOLD_SPENT = "cs_gold_spent"
  val CS_EXP = "cs_exp"
  val CS_ACTIONS = "cs_actions"
  val CS_SECS = "cs_secs"
  val CS_CONSUMABLES = "cs_consumables"
  val CS_WARDS = "cs_wards"
  val CS_EM_PLAYED = "cs_em_played"
  val CS_LEVEL = "cs_level"
  val CS_LEVEL_EXP = "cs_level_exp"
  val CS_MIN_EXP = "cs_min_exp"
  val CS_MAX_EXP = "cs_max_exp"
  val CS_TIME_EARNING_EXP = "cs_time_earning_exp"
  val CS_BLOODLUST = "cs_bloodlust"
  val CS_DOUBLEKILL = "cs_doublekill"
  val CS_TRIPLEKILL = "cs_triplekill"
  val CS_QUADKILL = "cs_quadkill"
  val CS_ANHIHILATION = "cs_annihilation"
  val CS_KILLSERIES3 = "cs_ks3"
  val CS_KILLSERIES4 = "cs_ks4"
  val CS_KILLSERIES5 = "cs_ks5"
  val CS_KILLSERIES6 = "cs_ks6"
  val CS_KILLSERIES7 = "cs_ks7"
  val CS_KILLSERIES8 = "cs_ks8"
  val CS_KILLSERIES9 = "cs_ks9"
  val CS_KILLSERIES10 = "cs_ks10"
  val CS_KILLSERIES15 = "cs_ks15"
  val CS_SMACKDOWN = "cs_smackdown"
  val CS_HUMILIATION = "cs_humiliation"
  val CS_NEMESIS = "cs_nemesis"
  val CS_RETRIBUTION = "cs_retribution"
}
