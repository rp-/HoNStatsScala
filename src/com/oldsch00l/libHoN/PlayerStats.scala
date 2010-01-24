package com.oldsch00l.libHoN

class PlayerStats( xmlData: scala.xml.Elem) {
	def attribute( name: String) = {
	  xmlData \\ "stat" filter attributeValueEquals( name);
	}
 
 	def attributeValueEquals(value: String)(node: scala.xml.Node) = {
	     node.attributes.exists(_.value == value)
	}
}
