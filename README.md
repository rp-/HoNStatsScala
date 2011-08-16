# HoNStats

HoNStats is a console application written in Scala and
therefore running on the JVM, to retrieve and show player and match
statistics of the game [Heroes of Newerth](http://www.heroesofnewerth.com).

## Usage

   usage: HoNStats [options] command items

   options:
      -s statstype, --statstype statstype   stats type [ranked,public,casual]
      -l limit, --limit limit               limit output list size
      command                               command [player,matches,match]
      items                                 items(nicks or matchids...

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
