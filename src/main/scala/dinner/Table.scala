package dinner

import Boot.Invite

import akka.actor.{Actor, ActorRef, Props}

import dinner.Philosopher.Think

class Table extends Actor {

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

  }
}