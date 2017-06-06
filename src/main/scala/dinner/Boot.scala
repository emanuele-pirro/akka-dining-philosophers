package dinner

import akka.actor._

//TODO: logging
object Boot extends App {

    case object Invite

    val system = ActorSystem("ActorSystem")
    val table = system.actorOf(Props[Table], name = "table")

    table ! Invite

    //system.terminate()
}
