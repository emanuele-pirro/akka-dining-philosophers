package dinner

import akka.actor._

object Boot extends App {

    case object Invite

    val system = ActorSystem("ActorSystem")
    val table = system.actorOf(Props[Table], name = "table")

    table ! Invite
}
