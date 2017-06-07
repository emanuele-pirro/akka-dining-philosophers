package dinner

import Boot.Invite
import akka.actor.{Actor, ActorLogging, Props}
import dinner.Philosopher.Think

class Table extends Actor with ActorLogging {

    override def receive: Receive = {

        case Invite =>
            log.debug("Inviting Philosophers.")

            // create forks and philosophers
            val forks = (0 to 4).map(i => context.actorOf(Props[Fork], s"fork_$i"))
            Stream.continually(forks).flatten.sliding(2, 1).map {
                case (l #:: r #:: _) => l -> r
            }.take(5).toList.foreach(f => {
                // every philosopher will have his own forks refs assigned
                val i = f._1.path.name.last
                context.actorOf(Philosopher(f._1, f._2), name = s"philosopher_$i")
            })

            // select only the philosophers and make them think
            context.children.filter(c => c.path.name.startsWith("p")).foreach(p => p ! Think)

    }
}