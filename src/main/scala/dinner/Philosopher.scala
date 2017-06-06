package dinner

import Philosopher._
import akka.actor.{Actor, ActorRef, Props}
import dinner.Fork.Release
import scala.concurrent.duration._

object Philosopher {

  def apply(forkLeft: ActorRef, forkRight: ActorRef): Props = Props(new Philosopher(forkLeft, forkRight))

  case object Eat
  case object Think

  trait PhilosopherStatus
  case object THINKING extends PhilosopherStatus
  case object EATING extends PhilosopherStatus

}

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