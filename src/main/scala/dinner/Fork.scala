package dinner

import akka.actor.{Actor, ActorLogging, ActorRef}
import dinner.Fork.{ForkNotAvailable, ForkTaken, Release, Use}

object Fork {
    case class Use(side: Int)
    case object Release

    case class ForkTaken(side: Int)
    case class ForkNotAvailable(side: Int)
}

class Fork extends Actor with ActorLogging {

    private val name = self.path.name
    var owner : Option[ActorRef] = None

    override def preStart(): Unit = {
        super.preStart()
        log.debug(s"[$name] created")
    }

    override def receive: Receive = {
        case u: Use =>
            val senderName = sender.path.name
            log.debug(s"[$name] trying to be used by $senderName")
            if (owner isEmpty) {
                owner = Some(sender)
                sender ! ForkTaken(u.side)
            } else {
                sender ! ForkNotAvailable(u.side)
            }
        case Release =>
            val senderName = sender.path.name
            log.debug(s"[$name] being released by $senderName")
            if (owner.exists(p => p.equals(sender)))
                owner = None
    }

}