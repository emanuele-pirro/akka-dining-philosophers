package dinner

import akka.actor.{Actor, ActorRef}
import dinner.Fork.{Release, Use}

object Fork {
  case object Use
  case object Release
}

class Fork extends Actor {

  private val name = self.path.name
  var usedBy : Option[ActorRef] = None

  override def preStart(): Unit = {
    super.preStart()
    println(s"[$name] created")
  }

  override def receive: Receive = {
    case Use =>
      if (usedBy.isEmpty)
        usedBy = Some(sender)
    case Release =>
      if (usedBy.exists(p => p.equals(p)))
        usedBy = None
  }

}