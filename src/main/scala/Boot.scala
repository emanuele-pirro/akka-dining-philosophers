import akka.actor._

import scala.concurrent.duration._

//TODO: logging
object Boot extends App {

    val system = ActorSystem("ActorSystem")
    val table = system.actorOf(Props[Table], name = "table")

    table ! Invite

    //system.terminate()
}

class Table extends Actor {

    private val name = self.path.name
    var forks: Array[ActorRef] = Array.ofDim[ActorRef](5)

    override def receive: Receive = {

        case Invite =>

            // create the forks
            for (i <- 0 to 4) {
                forks(i) = context.actorOf(Props[Fork], name = s"fork$i")
            }

            //create the philosophers and assign their forks
            for (i <- 0 to 4) {
                val forkLeft = forks(i)
                val forkRight = forks((i+1)%5)
                context.actorOf(Philosopher(forkLeft, forkRight), name = s"philosopher$i")
            }

            // select only the philosophers and make them think
            context.children.filter(c => c.path.name.startsWith("p"))
                .foreach(p => p ! Think)

            println(s"[$name] dinner begun")
    }
}

object Philosopher {
    def apply(forkLeft: ActorRef, forkRight: ActorRef): Props = Props(new Philosopher(forkLeft, forkRight))
}

trait PhilosopherStatus
case object THINKING extends PhilosopherStatus
case object EATING extends PhilosopherStatus

class Philosopher(forkLeft: ActorRef, forkRight: ActorRef) extends Actor {

    import context.dispatcher

    private val name = self.path.name
    private val scheduler = context.system.scheduler
    private var status: PhilosopherStatus = THINKING

    private val leftForkName = forkLeft.path.name
    private val rightForkName = forkRight.path.name

    override def preStart(): Unit = {
        super.preStart()
        println(s"[$name] created. Will be using forks $leftForkName and $rightForkName.")
    }

    override def receive: Receive = {
        case Think =>
            println(s"[$name] releasing forks $leftForkName and $rightForkName. Thinking.")

            // release forks
            forkLeft ! Release
            forkRight ! Release

            // think a bit and eat when you're hungry
            val t = 3 + new scala.util.Random().nextInt(6)
            scheduler.scheduleOnce(t seconds, self, Eat)

            status = THINKING

        case Eat =>
            println(s"[$name] taking forks $leftForkName and $rightForkName. Eating.")

            // tries to get the forks


            // eat a bit and think when you've done
            val t = 2 + new scala.util.Random().nextInt(5)
            scheduler.scheduleOnce(t seconds, self, Think)

            status = EATING
    }
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

case object Invite
case object Eat
case object Think

case object Use
case object Release
