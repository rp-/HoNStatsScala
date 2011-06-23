package oldsch00l

object Log {
  
	object Level extends Enumeration {
	  type Level = Value
	  val DEBUG, INFO, WARNING, ERROR = Value
	}
  	import Level._
  	
	var level = Level.WARNING
	def setLevel(lvl: Level) = level = lvl
	
	def log(level: Level, msg: String, os : Any*) = {
  	  if( this.level <= level ) println( msg.format(os.map(_.asInstanceOf[AnyRef]) : _* ) )
  	}
	  
	def debug(msg: String, os : Any*) = log(Level.DEBUG, msg, os)
	def info(msg: String, os : Any*) = log(Level.INFO, msg, os)
	def warn(msg: String, os : Any*) = log(Level.WARNING, msg, os)
	def error(msg: String, os : Any*) = log(Level.ERROR, msg, os)
}