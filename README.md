# HoNStats

HoNStats is a console application written in Scala and
therefore running on the JVM, to retrieve and show player and match
statistics of the game [Heroes of Newerth](http://www.heroesofnewerth.com).

## Build

You need [sbt](https://github.com/harrah/xsbt/wiki) to build HoNStats.
If you have sbt installed and in your PATH just run:

    sbt proguard
    
And sbt will create a ``target`` folder with a file HoNStats.VERSION.min.jar in it.
You can execute this file with java like:

    java -jar HoNStats.VERSION.min.jar
    

## Usage

    Usage: HoNStats [options] [command] [command options]
      Options:
        -d, --debug       Show debug output
                          Default: false
        -f, --fetch       Don't use cache
                          Default: false
        -l, --limit       Maximum output of items
                          Default: 5
        -q, --quiet       Tries to minimize output lines for errors/warnings
                          Default: false
        -s, --statstype   StatsType to show: [ranked,public,casual]
                          Default: ranked
      Commands:
        player      Show player stats
          Usage: player [options] Nicknames to show stats
        matches      Show matches of player
          Usage: matches [options] Nicknames
        match      Show stats for a match
          Usage: match [options] Matchid's to show matches
        player-heroes      Show played heroes for a player
          Usage: player-heroes [options] Nicknames
      Options:
              -b, --sort-by   Sort by [use,kdr,k,d,a,kpg,dpg,apg]
                              Default: use

## Example output

Command:

    HoNStats player Maliken

Output:

    Nick       MMR   K     D    A    KDR    MGP AID
    Maliken    1671   691/ 599/1029  1.15   142 6

Command:

    HoNStats matches erpe

Output:

    Erpe
     MID       GD    Date               K  D  A  Hero W/L Wards  CK/CD GPM
     56896158  40:52 2011-08-16 16:10  10/ 7/10  Plag W       8  69/33 220
     53400788  30:41 2011-07-22 17:09   0/ 2/ 6  Hamm W       0  98/15 280
     51763720  47:39 2011-07-07 19:12   2/ 9/11  Nymp W       3  71/ 6 180
     
Match Command:

    HoNStats match 59650952
    
Output:

    Match 59650952 -- 2011-09-04 19:33 - GD: 48:50
    Legion(W)           Hero  K  D  A  CK CD GPM GL2D  Hellbourne          Hero  K  D  A  CK CD GPM GL2D
    1647 sandla         Arac  6  6  9 177  9 363 1337  1635 jepakki        Scou 15  7  8 177  3 289 3176
    1628 thrux          Bloo 13  7 15 221 19 346 3030  1513 Smokeflow      Silh  6 11  9 154 15 214 3398
    1601 Lujo           Krak  9  4 25 120 11 314 1110  1532 Killha         Alun  2 12 14  73  1 149 2952
    1583 llcsoordy      Corr 16  8 16 127  3 305 2970  1597 toph           Hamm  2  8  7 190  7 234 2394
    1631 Neiolol        Witc  4  6 20  17  1 188 1253  1662 BloodMan       Legi  6 10  3 228  0 280 2658