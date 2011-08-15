# HoNStats

HoNStats is a console application written in Scala and
therefore running on the JVM, to retrieve and show player and match
statistics of the game [Heroes of Newerth](http://www.heroesofnewerth.com).

## Usage

    HoNStats [options] command nicks

    options:
      -s statstype, --statstype statstype   stats type [ranked,public,casual]
      -l limit, --limit limit               limit output list size
      command                               command [player,matches]
      nicks                                 nicks...

