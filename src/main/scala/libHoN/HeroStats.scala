package libHoN

class HeroStats {

}

object HeroAttr {
  
  def getNick(hid : Int) : String = {
    hid match {
      case 7 => "BS"
      case 15 => "SB"
      case 16 => "BH"
      case 18 => "TB"
      case 24 => "SS"
      case 26 => "DL"
      case 29 => "WB"
      case 42 => "MadM"
      case 104 => "Hag"
      case 109 => "SR"
      case 121 => "FA"
      case x => IDMap.getOrElse(x, hid + "|Unknown").substring(0,4)
    }
  }
  
  val IDMap = Map( 
      2 -> "Armadon",
      3 -> "Behemoth",
      4 -> "Chronos",
      5 -> "Defiler",
      6 -> "Devourer",
      7 -> "Blacksmith",
      8 -> "Slither",
      9 -> "Electrician",
      10 -> "Nymphora",
      12 -> "Glacius",
      13 -> "Hammerstorm",
      14 -> "Night Hound",
      15 -> "Swiftblade",
      16 -> "Blood Hunter",
      17 -> "Kraken",
      18 -> "Thunderbringer",
      20 -> "Moon Queen",
      21 -> "Pollywog Priest",
      22 -> "Pebbles",
      24 -> "Soulstealer",
      25 -> "Keeper of Forest",
      26 -> "The Dark Lady",
      43 -> "Demented Shaman",
      27 -> "Voodoo Jester",
      29 -> "War Beast",
      30 -> "Wildsoul",
      31 -> "Zephyr",
      34 -> "Pharao",
      35 -> "Tempest",
      36 -> "Ophelia",
      37 -> "Magebane",
      38 -> "Legionnaire",
      39 -> "Predator",
      40 -> "Accursed",
      41 -> "Nomad",
      42 -> "The Madman",
      44 -> "Scout",
      89 -> "Jereziah",
      90 -> "Torturer",
      91 -> "Puppet Master",
      92 -> "Arachna",
      93 -> "Hellbringer",
      94 -> "Pyromancer",
      95 -> "Pestilence",
      96 -> "Maliken",
      102 -> "Andromeda",
      103 -> "Valkyrie",
      104 -> "Wretched Hag",
      105 -> "Succubus",
      106 -> "Magmus",
      108 -> "Plague Rider",
      109 -> "Soul Reaper",
      110 -> "Pandamonium",
      115 -> "Vindicator",
      114 -> "Corrupted Disciple",
      116 -> "Sand Wraith",
      117 -> "Rampage",
      120 -> "Witch Slayer",
      121 -> "Forsaken Archer",
      122 -> "Engineer",
      123 -> "Deadwood",
      124 -> "Chipper",
      125 -> "Bubbles",
      126 -> "Fade",
      127 -> "Balphagor",
      128 -> "Gauntlet",
      160 -> "Tundra",
      161 -> "Gladiator",
      162 -> "Doctor Replusor",
      170 -> "Tremble",
      163 -> "Flint Beastwood",
      164 -> "Bombardier",
      165 -> "Moraxus",
      166 -> "Myrmidon",
      167 -> "Dampeer",
      168 -> "Empath",
      169 -> "Aluna",
      185 -> "Silhouette",
      187 -> "Flux",
      188 -> "Martyr",
      192 -> "Amun-Ra",
      194 -> "Parasite",
      195 -> "Emerald Warden",
      196 -> "Revenant",
      197 -> "Monkey King",
      201 -> "Drunken Master",
      202 -> "Master of Arms",
      203 -> "Rhapsody"
      )
}
