package com.oldsch00l.libHoN

class PlayerStats( xmlData: scala.xml.Node) {
  def attribute( name: String) : String = {
    (xmlData \ "stat").filter( attributeNameValueEquals( name)).text
  }
 
  def attributeNameValueEquals(value: String)(node: scala.xml.Node) = {
    (node \ "@name").toString == value
  }

  def getAID : String = xmlData.attribute( "aid").get.text;

  override def toString =   xmlData.toString
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
  val BUILDINGDMG= "acc_bdmg";
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
}
