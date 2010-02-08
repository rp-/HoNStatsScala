package com.oldsch00l.libHoN

class PlayerStats( xmlData: scala.xml.Elem) {
  def attribute( name: String) : String = {
    (xmlData \ "player_stats" \ "stat").filter( attributeValueEquals( name)).text
  }
 
  def attributeValueEquals(value: String)(node: scala.xml.Node) = {
     node.attributes.exists(_.value == value)
  }

  def getAID : String = (xmlData \ "player_stats" \ "@aid").text;

  override def toString =   xmlData.toString
}
