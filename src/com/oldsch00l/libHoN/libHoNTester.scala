package com.oldsch00l.libHoN;

import scala.xml._;

object Tester {
	def main(args : Array[String]) : Unit = {
		val erpe = StatsFactory.getPlayerStats( "erpe")
		val wins = erpe.attribute( "acc_wins");
  
		println( erpe );
  	}
}
