package dinner

import Philosopher._
import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import dinner.Fork.{ForkNotAvailable, ForkTaken, Release, Use}

import scala.concurrent.duration._
import scala.collection.mutable

object Philosopher {
    def apply(forkLeft: ActorRef, forkRight: ActorRef): Props = Props(new Philosopher(forkLeft, forkRight))

    case object Think
    case object TakeForks
    case object Eat

    trait PhilosopherStatus
    case object THINKING extends PhilosopherStatus
    case object EATING extends PhilosopherStatus
}

class Philosopher(forkLeft: ActorRef, forkRight: ActorRef) extends Actor with ActorLogging {

    import context.dispatcher

    private val name = self.path.name
    private val scheduler = context.system.scheduler
    private var status: PhilosopherStatus = THINKING
    private val ownedForks = mutable.HashMap(1 -> false, -1 -> false)

    override def preStart(): Unit = {
        super.preStart()
        val leftForkName = forkLeft.path.name
        val rightForkName = forkRight.path.name
        log.debug(s"[$name] created. Will be using forks $leftForkName and $rightForkName.")
    }

    override def receive: Receive = {
        case Think =>
            log.info(s"[$name] releasing forks and thinking.")

            // release forks
            resetOwnedForks()
            forkLeft ! Release
            forkRight ! Release

            // think a bit and eat when you're hungry
            val t = 3 + new scala.util.Random().nextInt(6)
            scheduler.scheduleOnce(t seconds, self, TakeForks)

            status = THINKING

        case TakeForks =>
            log.info(s"[$name] trying to take his forks.")

            // tries to get the left fork
            resetOwnedForks()
            forkLeft ! Use(1)
            forkRight ! Use(-1)
            context.become(waitingForForksAvailability)

        case Eat =>
            log.info(s"[$name] EATING.")

            val t = 2 + new scala.util.Random().nextInt(5)
            scheduler.scheduleOnce(t seconds, self, Think)
            status = EATING
    }


    def waitingForForksAvailability: Receive = {
        case f: ForkTaken =>
            val side = f.side
            ownedForks.put(side, true)
            log.debug(s"[$name] Fork $side taken.")
            if (ownedForks(-side)) {
                context.become(receive)
                self ! Eat
            }

        case ForkNotAvailable =>
            log.debug(s"[$name] gives up on taking forks.")
            context.become(receive)
            self ! Think
    }

    private def resetOwnedForks() = {
        ownedForks.put(1, false)
        ownedForks.put(-1, false)
    }
}